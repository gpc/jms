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

import grails.plugin.jms.bean.JmsListenerAdapterAbstractBeanDefinitionBuilder
import grails.plugin.jms.bean.JmsListenerContainerAbstractBeanDefinitionBuilder

import org.apache.commons.lang.StringUtils

class ListenerConfig {

    static final String SERVICE_BEAN_SUFFIX = "Service"

    static final String DEFAULT_CONNECTION_FACTORY_BEAN_NAME = "jmsConnectionFactory"

    def grailsApplication

    boolean topic = false
    def listenerMethodName
    def messageSelector
    def explicitDestinationName
    def serviceListener = false
    def serviceBeanName
    def containerParent
    def adapterParent

    def getServiceBeanPrefix() {
        serviceBeanName - SERVICE_BEAN_SUFFIX
    }

    def getBeanPrefix() {
        if (serviceListener) {
            serviceBeanPrefix
        }
        else {
            serviceBeanPrefix + StringUtils.capitalize(listenerMethodName)
        }
    }

    def getListenerAdapterBeanName() {
        beanPrefix + "JmsListenerAdapter"
    }

    def getListenerContainerBeanName() {
        beanPrefix + "JmsListenerContainer"
    }

    def getDestinationName() {
        if (explicitDestinationName) {
            explicitDestinationName
        }
        else {
            if (serviceListener) {
                appName + "." + serviceBeanPrefix
            }
            else if (topic) {
                listenerMethodName
            }
            else {
                appName + "." + serviceBeanPrefix + "." + listenerMethodName
            }
        }
    }

    def getAppName() {
        grailsApplication.metadata['app.name']
    }

    def register(beanBuilder) {
        registerListenerAdapter(beanBuilder)
        registerListenerContainer(beanBuilder)
    }

    def registerListenerAdapter(beanBuilder) {
        beanBuilder.with {
            "${listenerAdapterBeanName}" {
                it.parent = ref(adapterParent + JmsListenerAdapterAbstractBeanDefinitionBuilder.nameSuffix)
                it.'abstract' = false
                delegate.delegate = ref(serviceBeanName)
                defaultListenerMethod = listenerMethodName
            }
        }
    }

    def registerListenerContainer(beanBuilder) {
        beanBuilder.with {
            "${listenerContainerBeanName}"() {
                it.parent = ref(containerParent + JmsListenerContainerAbstractBeanDefinitionBuilder.nameSuffix)
                it.'abstract' = false
                it.destroyMethod = "destroy"

                destinationName = this.destinationName

                pubSubDomain = topic
                if (messageSelector) {
                    messageSelector = messageSelector
                }

                messageListener = ref(listenerAdapterBeanName)
            }
        }
    }

    def removeBeansFromContext(ctx) {
        [listenerAdapterBeanName,listenerContainerBeanName].each {
            ctx.removeBeanDefinition(it)
        }
    }
}
