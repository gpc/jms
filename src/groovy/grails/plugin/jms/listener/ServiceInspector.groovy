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
package grails.plugin.jms.listener
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import grails.plugin.jms.*

class ServiceInspector {

    final static SERVICE_LISTENER_METHOD = "onMessage"
    final static EXPOSES_SPECIFIER = "exposes"
    final static EXPOSE_SPECIFIER = "expose"
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
        if (hasServiceListenerMethod) {
            def listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
            listenerConfig.with {
                serviceListener = true
                listenerMethodName = SERVICE_LISTENER_METHOD
                
                explicitDestinationName = GrailsClassUtils.getStaticPropertyValue(service, "destination")
                topic = GrailsClassUtils.getStaticPropertyValue(service, "isTopic") ?: false
                messageSelector = GrailsClassUtils.getStaticPropertyValue(service, "selector")
                containerParent = GrailsClassUtils.getStaticPropertyValue(service, "container") ?: "standard"
                adapterParent = GrailsClassUtils.getStaticPropertyValue(service, "adapter") ?: "standard"
            }
            listenerConfig
        } else {
            null
        }
    }
        
    def hasServiceListenerMethod(service) {
        service.metaClass.methods.find { it.name == SERVICE_LISTENER_METHOD && it.parameterTypes.size() == 1 } != null
    }
    
    def exposesJms(service) {
        GrailsClassUtils.getStaticPropertyValue(service, EXPOSES_SPECIFIER)?.contains(EXPOSES_JMS_SPECIFIER) || GrailsClassUtils.getStaticPropertyValue(service, EXPOSE_SPECIFIER)?.contains(EXPOSES_JMS_SPECIFIER)
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
            listenerMethodName = method.name
            explicitDestinationName = annotation.topic()
            messageSelector = annotation.selector()
            containerParent = annotation.container()
            adapterParent = annotation.adapter()
        }
        listenerConfig
    }
    
    def getServiceMethodQueueListenerConfig(service, method, annotation, listenerConfigFactory, grailsApplication) {
        def listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
        listenerConfig.with {
            topic = false
            listenerMethodName = method.name
            explicitDestinationName = annotation.name()
            messageSelector = annotation.selector()
            containerParent = annotation.container()
            adapterParent = annotation.adapter()
        }
        listenerConfig
    }
    
}