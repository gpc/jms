package grails.plugin.jms.test.simple

import grails.plugin.jms.*

import java.util.concurrent.LinkedBlockingQueue

class SimpleReceivingService {

    static exposes = ['jms']
    
    def queueReceived = new LinkedBlockingQueue()
    def subscriberReceived = new LinkedBlockingQueue()
    
    @Queue
    def queue(msg) {
        log.info "queue received: $msg"
        queueReceived << msg
    }
    
    @Subscriber
    def simpleTopic(msg) {
        log.info "subscriber received: $msg"
        subscriberReceived << msg
    }

}