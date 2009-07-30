package grails.jms.listener
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import grails.jms.*

class ServiceInspector {

    final static DEFAULT_SERVICE_LISTENER = "onMessage"
    final static CUSTOM_SERVICE_LISTENER_SPECIFIER = "listenerMethod"
    final static EXPOSES_SPECIFIER = "exposes"
    final static EXPOSES_JMS_SPECIFIER = "jms"
    
    def getListenerConfigs(service, listenerConfigFactory, grailsApplication) {
        if (!exposesJms(service)) return []
        
        def listenerConfigs = []
        
        listenerConfigs << getServiceListenerConfig(service, listenerConfigFactory, grailsApplication)
        service.methods.each {
            listenerConfigs << getServiceMethodListenerConfig(service, it, listenerConfigFactory, grailsApplication)
        }
        
        listenerConfigs.findAll { it != null }
    } 
    
    def getServiceListenerConfig(service, listenerConfigFactory, grailsApplication) {
        def hasServiceListenerMethod = hasServiceListenerMethod(service)
        if (hasServiceListenerMethod || hasServiceListenerClosure(service)) {
            def listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
            listenerConfig.with {
                serviceListener = true
                listenerMethodOrClosureName = getServiceListenerName(service)
                listenerIsClosure = !hasServiceListenerMethod
                
                concurrentConsumers = GrailsClassUtils.getStaticPropertyValue(service, "listenerCount") ?: 1
                explicitDestinationName = GrailsClassUtils.getStaticPropertyValue(service, "destination")
                topic = GrailsClassUtils.getStaticPropertyValue(service, "pubSub") ?: false
                messageSelector = GrailsClassUtils.getStaticPropertyValue(service, "messageSelector")
                durable = GrailsClassUtils.getStaticPropertyValue(service, "durable")
                explicitClientId = GrailsClassUtils.getStaticPropertyValue(service, "clientId")
            }
            listenerConfig
        } else {
            null
        }
    }
    
    def getServiceListenerName(service) {
        GrailsClassUtils.getStaticPropertyValue(service, CUSTOM_SERVICE_LISTENER_SPECIFIER) ?: DEFAULT_SERVICE_LISTENER
    }
    
    def hasServiceListenerMethod(service) {
        def serviceListenerName = getServiceListenerName(service)
        service.metaClass.methods.find { it.name == serviceListenerName && it.parameterTypes.size() == 1 } != null
    }
    
    def hasServiceListenerClosure(service) {
        def serviceListenerName = getServiceListenerName(service)
        service.metaClass.methods.find { it.name == GrailsClassUtils.getGetterName(serviceListenerName) && it.parameterTypes.size() == 0 } != null
    }
    
    def exposesJms(service) {
        GrailsClassUtils.getStaticPropertyValue(service, EXPOSES_SPECIFIER)?.contains(EXPOSES_JMS_SPECIFIER) == true
    }
    
    def isSingleton(service) {
        def scope = GrailsClassUtils.getStaticPropertyValue(service, 'scope')
        (scope == null || scope == "singleton")
    }
    
    def getServiceMethodListenerConfig(service, method, listenerConfigFactory, grailsApplication) {
        def subscriberAnnotation = method.getAnnotation(Subscriber)
        def queueAnnotation = method.getAnnotation(Queue)
        
        if (subscriberAnnotation) {
            getServiceMethodSubscriberListenerConfig(service, method, subscriberAnnotation, listenerConfigFactory, grailsApplication) 
        } else if (queueAnnotation) {
            getServiceMethodQueueListenerConfig(service, method, queueAnnotation, listenerConfigFactory, grailsApplication) 
        } else {
            null
        }
    }
    
    def getServiceMethodSubscriberListenerConfig(service, method, annotation, listenerConfigFactory, grailsApplication) {
        def listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
        listenerConfig.with {
            topic = true
            listenerMethodOrClosureName = method.name
            explicitDestinationName = annotation.topic()
            messageSelector = annotation.selector()
            durable = annotation.durable()
        }
        listenerConfig
    }
    
    def getServiceMethodQueueListenerConfig(service, method, annotation, listenerConfigFactory, grailsApplication) {
        def listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
        listenerConfig.with {
            topic = false
            listenerMethodOrClosureName = method.name
            explicitDestinationName = annotation.name()
            messageSelector = annotation.selector()
        }
        listenerConfig
    }
    
}