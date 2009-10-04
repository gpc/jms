import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.SimpleMessageConverter
import org.springframework.jms.listener.DefaultMessageListenerContainer
import grails.jms.listener.ServiceListenerAdapter

jms {
    templates {
        standard {
            clazz = JmsTemplate
            connectionFactoryBean = "jmsConnectionFactory"
            messageConverter = new SimpleMessageConverter()
        }
    }
    containers {
        standard {
            clazz = DefaultMessageListenerContainer
            concurrentConsumers = 1
            subscriptionDurable = false
            autoStartup = false
            connectionFactoryBean = "jmsConnectionFactory"
            messageSelector = null
        }
    }
    adapters {
        standard {
            clazz = ServiceListenerAdapter
            messageConverter = new SimpleMessageConverter()
        }
    }
}