import org.springframework.jms.support.converter.SimpleMessageConverter

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
        persistenceInterceptorBean = 'persistenceInterceptor'
    }
}