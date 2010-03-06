package grails.plugin.jms.listener
import org.apache.commons.lang.StringUtils
import org.springframework.jms.listener.DefaultMessageListenerContainer
import grails.plugin.jms.bean.*

class ListenerConfig {
        
    static final SERVICE_BEAN_SUFFIX = "Service"
    
    static final DEFAULT_CONNECTION_FACTORY_BEAN_NAME = "jmsConnectionFactory"
    
    def grailsApplication
    
    boolean topic = false
    def concurrentConsumers = 1
    def subscriptionDurable = false
    def listenerMethodOrClosureName = null
    def listenerIsClosure = false    
    def messageSelector = null
    def durable = false
    def explicitDurableSubscriptionName = null
    def explicitClientId = null
    def explicitDestinationName = null
    def explicitConnectionFactoryBeanName
    def serviceListener = false
    def serviceBeanName
    def messageConverter = ""
    def containerParent
    def adapterParent
    
    def getServiceBeanPrefix() {
        serviceBeanName - SERVICE_BEAN_SUFFIX
    }
    
    def getBeanPrefix() {
        if (serviceListener) {
            this.serviceBeanPrefix
        } else {
            this.serviceBeanPrefix + StringUtils.capitalize(listenerMethodOrClosureName)
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
                listenerMethodOrClosureName
            } else {
                this.appName + "." + this.serviceBeanPrefix + "." + listenerMethodOrClosureName
            }
        }
    }
    
    def getDurableSubscriptionName() {
        explicitDurableSubscriptionName ?: this.serviceBeanPrefix + StringUtils.capitalize(listenerMethodOrClosureName)
    }

    def getClientId() {
        explicitClientId ?: this.appName
    }
    
    def getAppName() {
        grailsApplication.metadata['app.name']
    }
    
    def getConnectionFactoryBeanName() {
        explicitConnectionFactoryBeanName ?: DEFAULT_CONNECTION_FACTORY_BEAN_NAME
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
                defaultListenerMethod = listenerMethodOrClosureName
                if (this.messageConverter == null) {
                    messageConverter = null
                } else if (this.messageConverter != "") {
                    messageConverter = ref(this.messageConverter)
                }
            }
        }
    }
    
    def registerListenerContainer(beanBuilder) {
        beanBuilder.with {
            "${this.listenerContainerBeanName}"() {
                it.parent = ref(containerParent + JmsListenerContainerAbstractBeanDefinitionBuilder.nameSuffix)
                it.'abstract' = false
                it.destroyMethod = "destroy"
                
                concurrentConsumers = concurrentConsumers
                destinationName = this.destinationName
                
                pubSubDomain = this.topic
            
                if (this.topic && durable) {
                    subscriptionDurable = durable
                    durableSubscriptionName = this.durableSubscriptionName
                    clientId = this.clientId
                }
            
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