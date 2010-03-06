class SendingService {

    def sendToQueue(msg) {
        sendJMSMessage(service: 'receiving', method: 'queue', msg)
    }
    
    def sendToTopic(msg) {
        sendJMSMessage(topic: 'subscriber', msg)
    }

}