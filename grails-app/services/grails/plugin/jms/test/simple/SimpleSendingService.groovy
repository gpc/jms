package grails.plugin.jms.test.simple

class SimpleSendingService {

    def sendToQueue(msg) {
        sendJMSMessage(service: 'simpleReceiving', method: 'queue', msg)
    }
    
    def sendToTopic(msg) {
        sendJMSMessage(topic: 'simpleTopic', msg)
    }

}