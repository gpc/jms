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
package grails.plugin.jms

import grails.plugin.jms.bean.JmsBeanDefinitionsBuilder
import grails.plugin.jms.listener.ListenerConfigFactory
import grails.plugin.jms.listener.ServiceInspector
import grails.plugins.Plugin
import grails.util.Environment
import groovy.util.logging.Commons
import org.grails.core.artefact.ServiceArtefactHandler

@Commons
class JmsGrailsPlugin extends Plugin {

    def author = "Grails Plugin Collective"
    def authorEmail = "grails.plugin.collective@gmail.com"
    def title = "JMS integration for Grails"
    def grailsVersion = "3.0 > *"

    def documentation = "http://grails.org/gpc/jms"

    def issueManagement = [system: "GitHub", url: "https://github.com/gpc/jms/issues"]
    def scm = [url: "https://github.com/gpc/jms"]

    def loadAfter = ['services', 'controllers', 'dataSource', 'hibernate', 'hibernate4']
    def observe = ['services', 'controllers']

    def listenerConfigs = [:]
    def serviceInspector = new ServiceInspector()
    def listenerConfigFactory = new ListenerConfigFactory()
    def jmsConfigHash
    def jmsConfig
    boolean isDisabled = false

    def getDefaultConfig() {
        String env = Environment.current.name
        ConfigSlurper configSlurper = new ConfigSlurper(env)
        return configSlurper.parse(DefaultJmsBeans)
    }

    def getListenerConfigs(serviceClass, grailsApplication) {
        log.debug("inspecting '${serviceClass.name}' for JMS listeners")
        serviceInspector.getListenerConfigs(serviceClass, listenerConfigFactory, grailsApplication)
    }

    def registerListenerConfig(listenerConfig, beanBuilder) {
        def queueOrTopic = (listenerConfig.topic) ? "TOPIC" : "QUEUE"
        log.info "registering listener for '${listenerConfig.listenerMethodName}' of service '${listenerConfig.serviceBeanPrefix}' to ${queueOrTopic} '${listenerConfig.destinationName}'"
        listenerConfig.register(beanBuilder)
    }

    Closure doWithSpring() {
        { ->
            def tempJmsConfig
            if (grailsApplication.config.jms) {
                println 'jms {} config if deprecated, please use grails.plugin.jms'
                tempJmsConfig = grailsApplication.config.jms
            } else if (grailsApplication.config.grails.plugins.jms) {
                println 'grails.plugins.jms {} config if deprecated, please use grails.plugin.jms'
                tempJmsConfig = grailsApplication.config.grails.plugins.jms
            } else {
                tempJmsConfig = grailsApplication.config.grails.plugin.jms
            }

            def defaultConfig = getDefaultConfig()

            if (tempJmsConfig) {
                jmsConfig = defaultConfig.merge(tempJmsConfig as ConfigObject)
            } else {
                jmsConfig = defaultConfig
            }

            // We have to take a hash now because a config object
            // will dynamically create nested maps as needed
            jmsConfigHash = jmsConfig.hashCode()

            log.debug("merged config: $jmsConfig")
            if (jmsConfig.disabled) {
                isDisabled = true
                log.warn("not registering listeners because JMS is disabled")
                return
            }

            new JmsBeanDefinitionsBuilder(jmsConfig).build(delegate)

            grailsApplication.serviceClasses?.each { service ->
                def serviceClass = service.getClazz()
                def serviceClassListenerConfigs = getListenerConfigs(serviceClass, grailsApplication)
                if (serviceClassListenerConfigs) {
                    serviceClassListenerConfigs.each {
                        registerListenerConfig(it, delegate)
                    }
                    listenerConfigs[serviceClass.name] = serviceClassListenerConfigs
                }
            }
        }
    }

    void doWithApplicationContext() {
        listenerConfigs.each { serviceClassName, serviceClassListenerConfigs ->
            serviceClassListenerConfigs.each {
                startListenerContainer(it, applicationContext)
            }
        }
        //Fetch and set the asyncReceiverExecutor
        try {
            def asyncReceiverExecutor = applicationContext.getBean('jmsAsyncReceiverExecutor')
            if (asyncReceiverExecutor) {
                log.info "A jmsAsyncReceiverExecutor was detected in the grailsApplication Context and therefore will be set in the JmsService."
                applicationContext.getBean('jmsService').asyncReceiverExecutor = asyncReceiverExecutor
            }
        }
        catch (e) {
            log.debug "No jmsAsyncReceiverExecutor was detected in the grailsApplication Context."
        }
    }

    //Send*
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
        [sendJMSMessage      : "sendJMSMessage",
         sendQueueJMSMessage : "sendQueueJMSMessage",
         sendTopicJMSMessage : "sendTopicJMSMessage",
         sendPubSubJMSMessage: "sendTopicJMSMessage"
        ].each { m, i ->
            2.upto(4) { n ->
                clazz.metaClass."$m" << this."$i$n".curry(jmsService)
            }
        }
    }

    //ReceiveSelected*
    def receiveSelectedJMSMessage1 = { jmsService, destination, selector ->
        jmsService.receiveSelected(destination, selector)
    }
    def receiveSelectedJMSMessage2 = { jmsService, destination, selector, long timeout ->
        jmsService.receiveSelected(destination, selector, timeout)
    }
    def receiveSelectedJMSMessage3 = { jmsService, destination, selector, jmsTemplateBeanName ->
        jmsService.receiveSelected(destination, selector, jmsTemplateBeanName)
    }
    def receiveSelectedJMSMessage4 = { jmsService, destination, selector, long timeout, jmsTemplateBeanName ->
        jmsService.receiveSelected(destination, selector, timeout, jmsTemplateBeanName)
    }

    def receiveSelectedAsyncJMSMessage1 = { jmsService, destination, selector ->
        jmsService.receiveSelectedAsync(destination, selector)
    }
    def receiveSelectedAsyncJMSMessage2 = { jmsService, destination, selector, long timeout ->
        jmsService.receiveSelectedAsync(destination, selector, timeout)
    }
    def receiveSelectedAsyncJMSMessage3 = { jmsService, destination, selector, jmsTemplateBeanName ->
        jmsService.receiveSelectedAsync(destination, selector, jmsTemplateBeanName)
    }
    def receiveSelectedAsyncJMSMessage4 = { jmsService, destination, selector, timeout, jmsTemplateBeanName ->
        jmsService.receiveSelectedAsync(destination, selector, timeout, jmsTemplateBeanName)
    }

    def addReceiveSelectedToClass(jmsService, clazz) {
        [receiveSelectedJMSMessage     : "receiveSelectedJMSMessage",
         receiveSelectedAsyncJMSMessage: "receiveSelectedAsyncJMSMessage"
        ].each { m, i ->
            1.upto(4) { n ->
                clazz.metaClass."$m" << this."$i$n".curry(jmsService)
            }
        }
    }

    def addServiceMethodsToClass(jmsService, clazz) {
        addSendMethodsToClass(jmsService, clazz)
        addReceiveSelectedToClass(jmsService, clazz)
    }

    @Override
    void doWithDynamicMethods() {
        addServiceMethods(grailsApplication)
    }

    void onChange(Map<String, Object> event) {
        if (!event.source || !event.ctx) {
            return
        }

        def jmsService = event.ctx.getBean('jmsService')

        if (grailsApplication.isControllerClass(event.source)) {
            addServiceMethodsToClass(jmsService, event.source)
            return
        }

        if (!grailsApplication.isServiceClass(event.source)) {
            return
        }

        if (event.source.name.endsWith(".JmsService")) {
            return
        }

        if (jmsConfig.disabled) {
            log.warn("not inspecting $event.source for listener changes because JMS is disabled in config")
        } else {
            boolean isNew = event.grailsApplication.getServiceClass(event.source?.name) == null
            def serviceClass = grailsApplication.addArtefact(ServiceArtefactHandler.TYPE, event.source).clazz

            if (!isNew) {
                listenerConfigs.remove(serviceClass.name).each { unregisterListener(it, event.ctx) }
            }

            def serviceListenerConfigs = getListenerConfigs(serviceClass, grailsApplication)
            if (serviceListenerConfigs) {
                listenerConfigs[serviceClass.name] = serviceListenerConfigs
                def newBeans = beans {
                    serviceListenerConfigs.each { listenerConfig ->
                        registerListenerConfig(listenerConfig, delegate)
                    }
                }
                newBeans.beanDefinitions.each { n, d ->
                    event.ctx.registerBeanDefinition(n, d)
                }
                serviceListenerConfigs.each {
                    startListenerContainer(it, event.ctx)
                }
            }
        }

        addServiceMethodsToClass(jmsService, event.source)
    }

    void onConfigChange(Map<String, Object> event) {
        def newJmsConfig = defaultConfig.merge(event.source.jms)
        def newJmsConfigHash = newJmsConfig.hashCode()

        if (newJmsConfigHash == jmsConfigHash) {
            return
        }

        def previousJmsConfig = jmsConfig
        jmsConfig = newJmsConfig
        jmsConfigHash = newJmsConfigHash
        log.warn("tearing down all JMS listeners/templates due to config change")

        // Remove the listeners
        listenerConfigs.keySet().toList().each {
            listenerConfigs.remove(it).each { unregisterListener(it, event.ctx) }
        }

        // Remove the templates and abstract definitions from config
        new JmsBeanDefinitionsBuilder(previousJmsConfig).removeFrom(event.ctx)

        if (jmsConfig.disabled) {
            log.warn("NOT re-registering listeners/templates because JMS is disabled after config change")
        } else {
            log.warn("re-registering listeners/templates after config change")

            // Find all of the listeners
            grailsApplication.serviceClasses.each { serviceClassClass ->
                def serviceClass = serviceClassClass.clazz
                def serviceListenerConfigs = getListenerConfigs(serviceClass, grailsApplication)
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

            newBeans.beanDefinitions.each { n, d ->
                event.ctx.registerBeanDefinition(n, d)
            }

            listenerConfigs.each { name, serviceListenerConfigs ->
                serviceListenerConfigs.each { listenerConfig ->
                    startListenerContainer(listenerConfig, event.ctx)
                }
            }
        }

        // We need to trigger a reload of the jmsService so it gets any new beans
        def jmsServiceClass = grailsApplication.classLoader.reloadClass(grailsApplication.mainContext.jmsService.getClass().name)
        grailsApplication.mainContext.pluginManager.informOfClassChange(jmsServiceClass)

        // This also means we need to add new versions of the send methods
        addServiceMethods(grailsApplication)
    }

    def addServiceMethods(grailsApplication) {
        def jmsService = grailsApplication.mainContext.jmsService
        [grailsApplication.controllerClasses, grailsApplication.serviceClasses].each {
            it.each {
                if (it.clazz.name != "JmsService") {
                    addServiceMethodsToClass(jmsService, it.clazz)
                }
            }
        }
    }

    def unregisterListener(listenerConfig, appCtx) {
        log.info("removing JMS listener beans for ${listenerConfig.serviceBeanName}.${listenerConfig.listenerMethodName}")
        listenerConfig.removeBeansFromContext(appCtx)
    }

    def startListenerContainer(listenerConfig, grailsApplicationContext) {
        grailsApplicationContext.getBean(listenerConfig.listenerContainerBeanName).start()
    }
}
