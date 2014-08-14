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
import java.util.List;

import grails.plugin.jms.bean.JmsBeanDefinitionsBuilder
import grails.plugin.jms.listener.ListenerConfigFactory
import grails.plugin.jms.listener.ServiceInspector
import grails.plugin.jms.listener.ListenerConfig
import grails.util.Environment;
import grails.util.GrailsUtil

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler

import grails.plugin.jms.JmsUtils
import org.codehaus.groovy.grails.commons.GrailsApplication

class JmsGrailsPlugin {

    static LOG = LogFactory.getLog('grails.plugin.jms.JmsGrailsPlugin')

    def version = "1.3-SNAPSHOT"
    def author = "Grails Plugin Collective"
    def authorEmail = "grails.plugin.collective@gmail.com"
    def title = "JMS integration for Grails"
    def grailsVersion = "1.2.0 > *"

    def loadAfter = ['services', 'controllers']
    def observe = ['services', 'controllers']

    def pluginExcludes = [
        "src/docs/**",
        "**/grails/plugin/jms/test/**"
    ]

    ServiceInspector serviceInspector = new ServiceInspector()
    ListenerConfigFactory listenerConfigFactory = new ListenerConfigFactory()


    List<ListenerConfig> getListenerConfigs(Class serviceClass, GrailsApplication application) {
        LOG.debug("inspecting '${serviceClass.name}' for JMS listeners")
        return serviceInspector.getListenerConfigs(serviceClass, listenerConfigFactory, application)
    }

    def registerListenerConfig(ListenerConfig listenerConfig, Closure beanBuilder) {
        def queueOrTopic = (listenerConfig.topic) ? "TOPIC" : "QUEUE"
        LOG.info "registering listener for '${listenerConfig.listenerMethodName}' of service '${listenerConfig.serviceBeanPrefix}' to ${queueOrTopic} '${listenerConfig.destinationName}'"
        listenerConfig.register(beanBuilder)
    }

    def doWithSpring = {
        JmsUtils.application = application

        if (JmsUtils.jmsConfig.disabled) {
            isDisabled = true
            LOG.warn("not registering listeners because JMS is disabled")
            return
        }

        new JmsBeanDefinitionsBuilder(JmsUtils.jmsConfig).build(delegate)

        application.serviceClasses?.each { service ->
           
            def serviceClass = service.getClazz()
				
            List<ListenerConfig> serviceClassListenerConfigs = getListenerConfigs(serviceClass, application)
            serviceClassListenerConfigs?.each {ListenerConfig listenerConfig->
                registerListenerConfig(listenerConfig, delegate)
            }
            JmsUtils.listenerConfigs[serviceClass.name] = serviceClassListenerConfigs
              
        }
    }

    def doWithApplicationContext = { applicationContext ->
        JmsUtils.listenerConfigs.each { serviceClassName, serviceClassListenerConfigs ->
            serviceClassListenerConfigs.each {
                startListenerContainer(it, applicationContext)
            }
        }
        //Fetch and set the asyncReceiverExecutor
        try {
            def asyncReceiverExecutor = applicationContext.getBean('jmsAsyncReceiverExecutor')
            if (asyncReceiverExecutor) {
                LOG.info "A jmsAsyncReceiverExecutor was detected in the Application Context and therefore will be set in the JmsService."
                applicationContext.getBean('jmsService').asyncReceiverExecutor = asyncReceiverExecutor
            }
        }
        catch (e) {
            LOG.debug "No jmsAsyncReceiverExecutor was detected in the Application Context."
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
        [sendJMSMessage: "sendJMSMessage",
         sendQueueJMSMessage: "sendQueueJMSMessage",
         sendTopicJMSMessage: "sendTopicJMSMessage",
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
        [receiveSelectedJMSMessage: "receiveSelectedJMSMessage",
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

    def doWithDynamicMethods = { ctx ->
        addServiceMethods(application)
    }

    def onChange = { event ->
        if (!event.source || !event.ctx) {
            return
        }

        def jmsService = event.ctx.getBean('jmsService')

        if (application.isControllerClass(event.source)) {
            addServiceMethodsToClass(jmsService, event.source)
            return
        }

        if (!application.isServiceClass(event.source)) {
            return
        }

        if (event.source.name.endsWith(".JmsService")) {
            return
        }

        if (JmsUtils.jmsConfig.disabled) {
            LOG.warn("not inspecting $event.source for listener changes because JMS is disabled in config")
        }
        else {
            boolean isNew = event.application.getServiceClass(event.source?.name) == null
            def serviceClass = application.addArtefact(ServiceArtefactHandler.TYPE, event.source).clazz

            if (!isNew) {
                JmsUtils.listenerConfigs.remove(serviceClass.name).each { unregisterListener(it, event.ctx) }
            }

            def serviceListenerConfigs = getListenerConfigs(serviceClass, application)
            if (serviceListenerConfigs) {
                JmsUtils.listenerConfigs[serviceClass.name] = serviceListenerConfigs
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

    def onConfigChange = { event ->
       def previousJmsConfig = JmsUtils.jmsConfig

        if (JmsUtils.setNewConfig(event.source.jms)) {
            return
        }

    
        LOG.warn("tearing down all JMS listeners/templates due to config change")

        // Remove the listeners
        JmsUtils.listenerConfigs.keySet().toList().each {
            JmsUtils.listenerConfigs.remove(it).each { unregisterListener(it, event.ctx) }
        }

        // Remove the templates and abstract definitions from config
        new JmsBeanDefinitionsBuilder(previousJmsConfig).removeFrom(event.ctx)

        if (jmsConfig.disabled) {
            LOG.warn("NOT re-registering listeners/templates because JMS is disabled after config change")
        }
        else {
            LOG.warn("re-registering listeners/templates after config change")

            // Find all of the listeners
            application.serviceClasses.each { serviceClassClass ->
                def serviceClass = serviceClassClass.clazz
                def serviceListenerConfigs = getListenerConfigs(serviceClass, application)
                if (serviceListenerConfigs) {
                    JmsUtils.listenerConfigs[serviceClass.name] = serviceListenerConfigs
                }
            }

            def newBeans = beans {
                def builder = delegate

                new JmsBeanDefinitionsBuilder(JmsUtils.jmsConfig).build(builder)

                JmsUtils.listenerConfigs.each { name, serviceListenerConfigs ->
                    serviceListenerConfigs.each { listenerConfig ->
                        registerListenerConfig(listenerConfig, builder)
                    }
                }
            }

            newBeans.beanDefinitions.each { n, d ->
                event.ctx.registerBeanDefinition(n, d)
            }

            JmsUtils.listenerConfigs.each { name, serviceListenerConfigs ->
                serviceListenerConfigs.each { listenerConfig ->
                    startListenerContainer(listenerConfig, event.ctx)
                }
            }
        }

        // We need to trigger a reload of the jmsService so it gets any new beans
        def jmsServiceClass = application.classLoader.reloadClass(application.mainContext.jmsService.getClass().name)
        application.mainContext.pluginManager.informOfClassChange(jmsServiceClass)

        // This also means we need to add new versions of the send methods
        addServiceMethods(application)
    }

    def addServiceMethods(application) {
        def jmsService = application.mainContext.jmsService
        [application.controllerClasses, application.serviceClasses].each {
            it.each {
                if (it.clazz.name != "JmsService") {
                    addServiceMethodsToClass(jmsService, it.clazz)
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
