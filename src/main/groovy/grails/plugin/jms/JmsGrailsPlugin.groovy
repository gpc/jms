package grails.plugin.jms

import grails.plugin.jms.bean.DefaultJmsBeans
import grails.plugin.jms.bean.JmsBeanDefinitionsBuilder
import grails.plugin.jms.listener.ListenerConfigFactory
import grails.plugin.jms.listener.ServiceInspector
import grails.plugins.Plugin
import grails.util.Environment
import groovy.util.logging.Commons
import org.springframework.jms.support.converter.SimpleMessageConverter

@Commons
class JmsGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "5.2.0 > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Jms" // Headline display name of the plugin
    def author = "Jeff Brown"
    def authorEmail = "brownj@ociweb.com"
    def description = '''\
JMS integration for Grails.
'''
    def profiles = ['web']

    def documentation = "http://grails.org/plugin/jms"

    def license = "APACHE"

    def developers = [[name: "Weiqi Gao", email: "gaow@ociweb.com"],
                      [name: "Søren Berg Glasius", email: "soeren+jmsplugin@glasius.dk"]    ]

    def issueManagement = [system: "GitHub", url: "https://github.com/gpc/jms/issues"]

    def scm = [url: "https://github.com/gpc/jms"]

    def loadAfter = ['services', 'controllers', 'domainClass', 'dataSource', 'hibernate', 'hibernate4', 'hibernate5']

    def serviceInspector = new ServiceInspector()
    def listenerConfigs = [:]
    def listenerConfigFactory = new ListenerConfigFactory()

    Closure doWithSpring() {
        { ->

            def jmsConfig = getJmsConfigurationWithDefaults()

            log.debug("merged config: $jmsConfig")
            if (jmsConfig.disabled) {
                log.warn("not registering listeners because JMS is disabled")
                return
            }

            new JmsBeanDefinitionsBuilder(jmsConfig).build(delegate)

            // TODO
            standardJmsMessageConverter SimpleMessageConverter

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

    def getJmsConfigurationWithDefaults() {
        if(config.jms) {
            ConfigObject jmsConfig = new ConfigObject()
            jmsConfig.putAll(config.jms)

            return getDefaultConfig().merge(jmsConfig)
        }
        else {
            return getDefaultConfig()
        }
    }

    def getDefaultConfig() {
        new ConfigSlurper(Environment.current.name).parse(DefaultJmsBeans)
    }

    def getListenerConfigs(serviceClass, application) {
        log.debug("inspecting '${serviceClass.name}' for JMS listeners")
        serviceInspector.getListenerConfigs(serviceClass, listenerConfigFactory, application)
    }

    def registerListenerConfig(listenerConfig, beanBuilder) {
        def queueOrTopic = (listenerConfig.topic) ? "TOPIC" : "QUEUE"
        log.info "registering listener for '${listenerConfig.listenerMethodName}' of service '${listenerConfig.serviceBeanPrefix}' to ${queueOrTopic} '${listenerConfig.destinationName}'"
        listenerConfig.register(beanBuilder)
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
                log.info "A jmsAsyncReceiverExecutor was detected in the Application Context and therefore will be set in the JmsService."
                applicationContext.getBean('jmsService').asyncReceiverExecutor = asyncReceiverExecutor
            }
        }
        catch (e) {
            log.debug "No jmsAsyncReceiverExecutor was detected in the Application Context."
        }

    }


    def startListenerContainer(listenerConfig, applicationContext) {
        def listenerContainer = applicationContext.getBean(listenerConfig.listenerContainerBeanName)

        if (listenerContainer.isAutoStartup()) {
            listenerContainer.start()
        }
    }
}
