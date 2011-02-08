import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory

beans = {
    xmlns amq:"http://activemq.apache.org/schema/core"

    amq.broker( xmlns:"http://activemq.apache.org/schema/core",
                brokerName:"localhost",
                dataDirectory:"target/activemq-data" ){

        amq.transportConnectors{
             amq.transportConnector(name:"vm", uri:"vm://localhost" )
        }
    }

    jmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'vm://localhost?create=false&waitForStart=100'
        }
    }

    otherJmsConnectionFactory(SingleConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'vm://localhost?create=false&waitForStart=100'
        }
    }
}