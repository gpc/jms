/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import grails.util.GrailsUtil
import grails.plugin.jms.listener.ServiceInspector
import grails.plugin.jms.listener.ListenerConfigFactory
import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler

import groovy.util.ConfigSlurper
import grails.plugin.jms.bean.JmsBeanDefinitionsBuilder

import org.apache.commons.logging.LogFactory

class JmsGrailsPlugin {
    
    static LOG = LogFactory.getLog('grails.plugin.jms.JmsGrailsPlugin')
    
    def version = "1.0"
    def author = "Grails Plugin Collective"
    def authorEmail = "grails.plugin.collective@gmail.com"
    def title = "JMS integration for Grails"
    def grailsVersion = "1.2.0 > *"
    
    def loadAfter = ['services', 'controllers']
    def observe = ['services', 'controllers']

    def pluginExcludes = [
        "conf/spring/**",
        "**/grails/plugin/jms/test/**"
    ]
    
    def listenerConfigs = [:]
    def serviceInspector = new ServiceInspector()
    def listenerConfigFactory = new ListenerConfigFactory()
    def jmsConfigHash = null
    def jmsConfig = null
    
    def getDefaultConfig() {
        new ConfigSlurper(GrailsUtil.environment).parse(DefaultJmsBeans)
    }
    
    def getListenerConfigs(serviceClass, application) {
        LOG.debug("inspecting '${serviceClass.name}' for JMS listeners")
        serviceInspector.getListenerConfigs(serviceClass, listenerConfigFactory, application)
    }
    
    def registerListenerConfig(listenerConfig, beanBuilder) {
        def queueOrTopic = (listenerConfig.topic) ? "TOPIC" : "QUEUE"
        LOG.info "registering listener for '${listenerConfig.listenerMethodName}' of service '${listenerConfig.serviceBeanPrefix}' to ${queueOrTopic} '${listenerConfig.destinationName}'"
        listenerConfig.register(beanBuilder)
        
    }
    def doWithSpring = {
        jmsConfig = defaultConfig.merge(application.config.jms)

        // We have to take a hash now because a config object
        // will dynamically create nested maps as needed
        jmsConfigHash = jmsConfig.hashCode()
        
        LOG.debug("merged config: $jmsConfig")
        if (jmsConfig.disabled) {
            isDisabled = true
            LOG.warn("not registering listeners because JMS is disabled")
            return
        }
        
        new JmsBeanDefinitionsBuilder(jmsConfig).build(delegate)
        
        application.serviceClasses?.each { service ->
            def serviceClass = service.getClazz()
            def serviceClassListenerConfigs = getListenerConfigs(serviceClass, application)
            if (serviceClassListenerConfigs) {
                serviceClassListenerConfigs.each {
                    registerListenerConfig(it, delegate)
                }
                listenerConfigs[serviceClass.name] = serviceClassListenerConfigs
            }
        }
    }
    
    def doWithApplicationContext = { applicationContext ->
        listenerConfigs.each { serviceClassName, serviceClassListenerConfigs ->
            serviceClassListenerConfigs.each {
                startListenerContainer(it, applicationContext)
            }
        }
    }
    
    def sendJMSMessage2 = { jmsService, destination, message ->
        jmsService.send(destination, message)
    }
    def sendJMSMessage3 = { jmsService, destination, message, postProcessor ->
        jmsService.send(destination, message, postProcessor)
    }
    def sendJMSMessage4 = { jmsService, destination, message, jmsTemplateBeanName, postProcessor ->
        jmsService.send(destination, message, jmsTemplateBeanName, postProcessor)
    }
    def sendQueueJMSMessage2 = { jmsService, destination, message ->
        jmsService.send(queue: destination, message)
    }
    def sendQueueJMSMessage3 = { jmsService, destination, message, postProcessor ->
        jmsService.send(queue: destination, message, postProcessor)
    }
    def sendQueueJMSMessage4 = { jmsService, destination, message, jmsTemplateBeanName, postProcessor ->
        jmsService.send(queue: destination, message, jmsTemplateBeanName, postProcessor)
    }
    def sendTopicJMSMessage2 = { jmsService, destination, message ->
        jmsService.send(topic: destination, message)
    }
    def sendTopicJMSMessage3 = { jmsService, destination, message, postProcessor ->
        jmsService.send(topic: destination, message, postProcessor)
    }
    def sendTopicJMSMessage4 = { jmsService, destination, message, jmsTemplateBeanName, postProcessor ->
        jmsService.send(topic: destination, message, jmsTemplateBeanName, postProcessor)
    }
    
    def addSendMethodsToClass(jmsService, clazz) {
        [
            sendJMSMessage: "sendJMSMessage", 
            sendQueueJMSMessage: "sendQueueJMSMessage", 
            sendTopicJMSMessage: "sendTopicJMSMessage",
            sendPubSubJMSMessage: "sendTopicJMSMessage"
        ].each { m, i ->
            2.upto(4) { n ->
                clazz.metaClass."$m" << this."$i$n".curry(jmsService)
            } 
        }
    }

    def doWithDynamicMethods = { ctx ->
        addSendMethods(application)
    }

    def onChange = { event ->
        if (event.source && event.ctx) {
            def jmsService = event.ctx.getBean('jmsService')
            
            if (application.isControllerClass(event.source)) {
                addSendMethodsToClass(jmsService, event.source)
            } else if (application.isServiceClass(event.source)) {
                if (event.source.name.endsWith(".JmsService")) {
                    return
                }
                if (jmsConfig.disabled) {
                    LOG.warn("not inspecting $event.source for listener changes because JMS is disabled in config")
                } else {
                    boolean isNew = event.application.getServiceClass(event.source?.name) == null
                    def serviceClass = application.addArtefact(ServiceArtefactHandler.TYPE, event.source).clazz

                    if (!isNew) {
                        listenerConfigs.remove(serviceClass.name).each { unregisterListener(it, event.ctx) }
                    }

                    def serviceListenerConfigs = getListenerConfigs(serviceClass, application)
                    if (serviceListenerConfigs) {
                        listenerConfigs[serviceClass.name] = serviceListenerConfigs
                        def newBeans = beans {
                            serviceListenerConfigs.each { listenerConfig ->
                                registerListenerConfig(listenerConfig, delegate)
                            }
                        }
                        newBeans.beanDefinitions.each { n,d ->
                            event.ctx.registerBeanDefinition(n, d)
                        }
                        serviceListenerConfigs.each {
                            startListenerContainer(it, event.ctx)
                        }
                    }
                }
                
                addSendMethodsToClass(jmsService, event.source)
            }
            
        }
    }

    def onConfigChange = { event ->
        def newJmsConfig = defaultConfig.merge(event.source.jms)
        def newJmsConfigHash = newJmsConfig.hashCode()
        
        if (newJmsConfigHash != jmsConfigHash) {
            def previousJmsConfig = jmsConfig
            jmsConfig = newJmsConfig
            jmsConfigHash = newJmsConfigHash
            LOG.warn("tearing down all JMS listeners/templates due to config change")
            
            // Remove the listeners
            listenerConfigs.keySet().toList().each {
                listenerConfigs.remove(it).each { unregisterListener(it, event.ctx) }
            }
            
            // Remove the templates and abstract definitions from config
            new JmsBeanDefinitionsBuilder(previousJmsConfig).removeFrom(event.ctx)
            
            if (jmsConfig.disabled) {
                LOG.warn("NOT re-registering listeners/templates because JMS is disabled after config change")
            } else {
                LOG.warn("re-registering listeners/templates after config change")
                
                // Find all of the listeners
                application.serviceClasses.each { serviceClassClass ->
                    def serviceClass = serviceClassClass.clazz
                    def serviceListenerConfigs = getListenerConfigs(serviceClass, application)
                    if (serviceListenerConfigs) {
                        listenerConfigs[serviceClass.name] = serviceListenerConfigs
                    }
                }

                def newBeans = beans {
                    def builder = delegate
                    
                    new JmsBeanDefinitionsBuilder(jmsConfig).build(builder)
                    
                    listenerConfigs.each { name, serviceListenerConfigs ->
                        serviceListenerConfigs.each { listenerConfig ->
                            registerListenerConfig(listenerConfig, builder)
                        }
                    }
                }

                newBeans.beanDefinitions.each { n,d ->
                    event.ctx.registerBeanDefinition(n, d)
                }

                listenerConfigs.each { name, serviceListenerConfigs ->
                    serviceListenerConfigs.each { listenerConfig ->
                        startListenerContainer(listenerConfig, event.ctx)
                    }
                }
            }
            
            // We need to trigger a reload of the jmsService so it gets any new beans
            def jmsServiceClass = application.classLoader.reloadClass(application.mainContext.jmsService.class.name)
            application.mainContext.pluginManager.informOfClassChange(jmsServiceClass)
            
            // This also means we need to add new versions of the send methods
            addSendMethods(application)
        }
    }
    
    def addSendMethods(application) {
        def jmsService = application.mainContext.jmsService
        [application.controllerClasses, application.serviceClasses].each {
            it.each {
                if (it.clazz.name != "JmsService") {
                    addSendMethodsToClass(jmsService, it.clazz)
                }
            }
        }
    }
    
    def unregisterListener(listenerConfig, appCtx) {
        LOG.info("removing JMS listener beans for ${listenerConfig.serviceBeanName}.${listenerConfig.listenerMethodName}")
        listenerConfig.removeBeansFromContext(appCtx)
    }
    
    def startListenerContainer(listenerConfig, applicationContext) {
        applicationContext.getBean(listenerConfig.listenerContainerBeanName).start()
    }
    
}
