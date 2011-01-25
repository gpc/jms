import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory

/**
 * @todo Establish kahaDB's directory (e.g. directory="activemq-data") so that it lives in target.
 */
beans = {
    jmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'vm://localhost'
        }
    }
    
    otherJmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'vm://localhost'
        }
    }
}