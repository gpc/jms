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
import org.apache.commons.lang.StringUtils
import org.springframework.jms.listener.DefaultMessageListenerContainer
import grails.plugin.jms.bean.*

class ListenerConfig {
        
    static final SERVICE_BEAN_SUFFIX = "Service"
    
    static final DEFAULT_CONNECTION_FACTORY_BEAN_NAME = "jmsConnectionFactory"
    
    def grailsApplication
    
    boolean topic = false
    def listenerMethodName = null
    def messageSelector = null
    def explicitDestinationName = null
    def serviceListener = false
    def serviceBeanName
    def containerParent
    def adapterParent
    
    def getServiceBeanPrefix() {
        serviceBeanName - SERVICE_BEAN_SUFFIX
    }
    
    def getBeanPrefix() {
        if (serviceListener) {
            this.serviceBeanPrefix
        } else {
            this.serviceBeanPrefix + StringUtils.capitalize(listenerMethodName)
        }
    }
    
    def getListenerAdapterBeanName() {
        this.beanPrefix + "JmsListenerAdapter"
    }
    
    def getListenerContainerBeanName() {
        this.beanPrefix + "JmsListenerContainer"
    }
    
    def getDestinationName() {
        if (explicitDestinationName) {
            explicitDestinationName
        } else {
            if (serviceListener) {
                this.appName + "." + this.serviceBeanPrefix
            } else if (topic) {
                listenerMethodName
            } else {
                this.appName + "." + this.serviceBeanPrefix + "." + listenerMethodName
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
            "${this.listenerAdapterBeanName}" {
                it.parent = ref(adapterParent + JmsListenerAdapterAbstractBeanDefinitionBuilder.nameSuffix)
                it.'abstract' = false
                delegate.delegate = ref(serviceBeanName)
                defaultListenerMethod = listenerMethodName
            }
        }
    }
    
    def registerListenerContainer(beanBuilder) {
        beanBuilder.with {
            "${this.listenerContainerBeanName}"() {
                it.parent = ref(containerParent + JmsListenerContainerAbstractBeanDefinitionBuilder.nameSuffix)
                it.'abstract' = false
                it.destroyMethod = "destroy"
                
                destinationName = this.destinationName
                
                pubSubDomain = this.topic
                if (messageSelector) {
                    messageSelector = messageSelector
                }
                
                messageListener = ref(this.listenerAdapterBeanName)
            }
        }
    }
    
    def removeBeansFromContext(ctx) {
        [listenerAdapterBeanName,listenerContainerBeanName].each {
            ctx.removeBeanDefinition(it)
        }
    }
}