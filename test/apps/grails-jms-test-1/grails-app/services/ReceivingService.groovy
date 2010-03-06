import grails.plugin.jms.*

import java.util.concurrent.LinkedBlockingQueue

class ReceivingService {

    static exposes = ['jms']
    
    def queueReceived = new LinkedBlockingQueue()
    def subscriberReceived = new LinkedBlockingQueue()
    
    @Queue
    def queue(msg) {
        log.info "queue received: $msg"
        queueReceived << msg
    }
    
    @Subscriber
    def subscriber(msg) {
        log.info "subscriber received: $msg"
        subscriberReceived << msg
    }

}