package grails.plugin.jms.test.simple

class SimpleSendingService {

    def sendToQueue(msg, template = null) {
        sendJMSMessage(service: 'simpleReceiving', method: 'queue', msg, template)
    }

    def sendToTransactionalQueue(msg, template = null) {
        sendJMSMessage(service: 'simpleReceiving', method: 'transactionalQueue', msg, template)
    }
    
    def sendToTopic(msg) {
        sendJMSMessage(topic: 'simpleTopic', msg)
    }

}