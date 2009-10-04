import org.springframework.jms.core.MessagePostProcessor
import javax.jms.Destination
import javax.jms.Topic
import grails.jms.listener.GrailsMessagePostProcessor

class JmsService {

    static final DEFAULT_JMS_TEMPLATE_BEAN_NAME = "standardJmsTemplate"
    
    def grailsApplication
    
    def send(destination, message) {
        send(destination, message, null)
    }
    
    def send(destination, message, postProcessor) {
        send(destination, message, null, postProcessor)
    }
    
    def send(destination, message, jmsTemplateBeanName, postProcessor) {
        
        jmsTemplateBeanName = jmsTemplateBeanName ?: DEFAULT_JMS_TEMPLATE_BEAN_NAME
        def jmsTemplate = grailsApplication.mainContext.getBean(jmsTemplateBeanName)
        if (jmsTemplate == null) {
            throw new Error("Could not find bean with name '${jmsTemplateBeanName}' to use as a JmsTemplate")
        }
        
        def isTopic
        if (destination instanceof Destination) {
            isTopic = destination instanceof Topic
        } else {
            def destinationMap = convertToDestinationMap(destination)
            isTopic = destinationMap.containsKey("topic")
            jmsTemplate.pubSubDomain = isTopic
            destination = (isTopic) ? destinationMap.topic : destinationMap.queue
        }
        
        if (log.infoEnabled) {
            def topicOrQueue = (isTopic) ? "topic" : "queue"
            def logMsg = "Sending JMS message '$message' to $topicOrQueue '$destination'"
            if (jmsTemplateBeanName != DEFAULT_JMS_TEMPLATE_BEAN_NAME)
                logMsg += " using template '$jmsTemplateBeanName'"
            log.info(logMsg)
        }

        if (postProcessor) {
            jmsTemplate.convertAndSend(destination, message, new GrailsMessagePostProcessor(jmsService: this, jmsTemplate: jmsTemplate, processor: postProcessor))
        } else {
            jmsTemplate.convertAndSend(destination, message)
        }
        
    }

    def convertToDestinationMap(destination) {
        
        if (destination == null) {
            [queue: null]
        } else if (destination instanceof String) {
            [queue: destination]
        } else if (destination instanceof Map) {
            if (destination.queue) {
                [queue: destination.queue]
            } else if (destination.topic) {
                [topic: destination.topic]
            } else {
                def parts = []
                if (destination.app) {
                    parts << destination.app
                } else {
                    parts << grailsApplication.metadata['app.name']
                }
                if (destination.service) {
                    parts << destination.service
                    if (destination.method) {
                        parts << destination.method
                    }
                }
                [queue: (parts) ? parts.join('.') : null]
            }
        } else {
            [queue: destination.toString()]
        }
    }
}