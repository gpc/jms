package grails.plugin.jms.listener
import org.springframework.jms.core.MessagePostProcessor
import javax.jms.Message

class GrailsMessagePostProcessor implements MessagePostProcessor {

    def jmsTemplate
    def jmsService
    def processor

    def createDestination(destination) {
        def destinationMap = jmsService.convertToDestinationMap(destination)
        def session = jmsTemplate.createSession(jmsTemplate.createConnection())
        def destinationResolver = jmsTemplate.destinationResolver
        def isTopic = destinationMap.containsKey("topic") 
        def destinationString = (isTopic) ? destinationMap.topic : destinationMap.queue
        destinationResolver.resolveDestinationName(session, destinationString, isTopic)
    }
    
    Message postProcessMessage(Message message) {
        processor.delegate = this
        processor.resolveStrategy = Closure.DELEGATE_ONLY
        processor.call(message) ?: message
    }
}