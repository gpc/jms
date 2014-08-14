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

import java.lang.annotation.Annotation

import grails.plugin.jms.Queue
import grails.plugin.jms.Subscriber

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils

class ServiceInspector {

    static final String SERVICE_LISTENER_METHOD = "onMessage"
    static final String EXPOSES_SPECIFIER = "exposes"
    static final String EXPOSE_SPECIFIER = "expose"
    static final String EXPOSES_JMS_SPECIFIER = "jms"

    static final Log LOG = LogFactory.getLog(this)

    List<ListenerConfig> getListenerConfigs(Class service, ListenerConfigFactory listenerConfigFactory, GrailsApplication grailsApplication) {
        if (!exposesJms(service)) return []

        List<ListenerConfig> listenerConfigs = []

        listenerConfigs << getServiceListenerConfig(service, listenerConfigFactory, grailsApplication)
        service.methods.findAll { !it.synthetic }.each {
            listenerConfigs << getServiceMethodListenerConfig(service, it, listenerConfigFactory, grailsApplication)
        }

        listenerConfigs.findAll { it != null }
    }

    ListenerConfig getServiceListenerConfig(Class service, ListenerConfigFactory listenerConfigFactory, GrailsApplication grailsApplication) {
        def hasServiceListenerMethod = hasServiceListenerMethod(service)
        if (hasServiceListenerMethod) {
            ListenerConfig listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
            listenerConfig.with {
                serviceListener = true
                listenerMethodName = SERVICE_LISTENER_METHOD
                explicitDestinationName = GrailsClassUtils.getStaticPropertyValue(service, "destination")
                topic = GrailsClassUtils.getStaticPropertyValue(service, "isTopic") ?: false
                messageSelector = GrailsClassUtils.getStaticPropertyValue(service, "selector")
                containerParent = GrailsClassUtils.getStaticPropertyValue(service, "container") ?: "standard"
                adapterParent = GrailsClassUtils.getStaticPropertyValue(service, "adapter") ?: "standard"
            }
            return listenerConfig
        }
    }

    boolean hasServiceListenerMethod(Class service) {
        service.metaClass.methods.find { it.name == SERVICE_LISTENER_METHOD && it.parameterTypes.size() == 1 } != null
    }
	
    boolean exposesJms(Class service) {
        GrailsClassUtils.getStaticPropertyValue(service, EXPOSES_SPECIFIER)?.
            contains(EXPOSES_JMS_SPECIFIER) ||
                GrailsClassUtils.getStaticPropertyValue(service, EXPOSE_SPECIFIER)?.
                    contains(EXPOSES_JMS_SPECIFIER)
    }

    boolean isSingleton(Class service) {
        def scope = GrailsClassUtils.getStaticPropertyValue(service, 'scope')
        (scope == null || scope == "singleton")
    }

    ListenerConfig getServiceMethodListenerConfig(Class service, method, ListenerConfigFactory listenerConfigFactory, GrailsApplication grailsApplication) {
        def subscriberAnnotation = method.getAnnotation(Subscriber)
        def queueAnnotation = method.getAnnotation(Queue)

        if (subscriberAnnotation) {
            getServiceMethodSubscriberListenerConfig(service, method, subscriberAnnotation, listenerConfigFactory, grailsApplication)
        }
        else if (queueAnnotation) {
            getServiceMethodQueueListenerConfig(service, method, queueAnnotation, listenerConfigFactory, grailsApplication)
        }
    }
	
    ListenerConfig getServiceMethodSubscriberListenerConfig(Class service, method, Annotation annotation, ListenerConfigFactory listenerConfigFactory, GrailsApplication grailsApplication) {
        ListenerConfig listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
        listenerConfig.with {
            topic = true
            listenerMethodName = method.name
            explicitDestinationName = resolveDestinationName(annotation.topic(), grailsApplication)
            messageSelector = annotation.selector()
            containerParent = annotation.container()
            adapterParent = annotation.adapter()
        }
        listenerConfig
    }
	
    ListenerConfig getServiceMethodQueueListenerConfig(Class service, method, Annotation annotation, ListenerConfigFactory listenerConfigFactory, GrailsApplication grailsApplication) {
        ListenerConfig listenerConfig = listenerConfigFactory.getListenerConfig(service, grailsApplication)
        listenerConfig.with {
            topic = false
            listenerMethodName = method.name
            explicitDestinationName = resolveDestinationName(annotation.name(), grailsApplication)
            messageSelector = annotation.selector()
            containerParent = annotation.container()
            adapterParent = annotation.adapter()
        }
        listenerConfig
    }
	
    String resolveDestinationName(final String name, GrailsApplication grailsApplication) {
        String resolvedName = name
        if ( resolvedName =~ /^\$/ ) {
            final List<String> pathTokens = resolvedName.substring(1).tokenize('.').reverse()
            def node = grailsApplication.config?.jms?.destinations
            while( node && node instanceof Map && pathTokens.size() ) {
                node = node[pathTokens.pop()]
            }
            if ( node && pathTokens.empty ) {
                resolvedName = node
                LOG.info "key '$name' resolved to destination '$resolvedName'." +
                    "The name '$resolvedName' will be used as the destination."
            }
            else {
                throw new IllegalArgumentException(
                    "The destination key '$name' is not available in the 'jms.destinations' configuration space." +
                    "Please define such key or remove the prefix '\$' from the name.")
            }
        }
        resolvedName
    }
}
