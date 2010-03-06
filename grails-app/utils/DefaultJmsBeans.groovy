import org.springframework.jms.support.converter.SimpleMessageConverter
import org.springframework.jms.listener.DefaultMessageListenerContainer

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
        cacheLevel = DefaultMessageListenerContainer.CACHE_SESSION
    }
}
adapters {
    standard {
        messageConverter = new SimpleMessageConverter()
        persistenceInterceptorBean = 'persistenceInterceptor'
    }
}