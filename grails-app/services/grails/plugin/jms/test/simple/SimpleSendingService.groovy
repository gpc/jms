package grails.plugin.jms.test.simple

class SimpleSendingService {

    def sendToQueue(msg, template = null) {
        sendJMSMessage(service: 'simpleReceiving', method: 'queue', msg, template)
    }
    
    def sendToTopic(msg) {
        sendJMSMessage(topic: 'simpleTopic', msg)
    }

}