import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.SimpleMessageConverter
import org.springframework.jms.listener.DefaultMessageListenerContainer
import grails.plugin.jms.listener.ServiceListenerAdapter

templates {
    standard {
        connectionFactoryBean = "jmsConnectionFactory"
        messageConverter = new SimpleMessageConverter()
    }
}
containers {
    standard {
        concurrentConsumers = 1
        subscriptionDurable = false
        autoStartup = false
        connectionFactoryBean = "jmsConnectionFactory"
        messageSelector = null
    }
}
adapters {
    standard {
        messageConverter = new SimpleMessageConverter()
    }
}