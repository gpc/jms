package grails.plugin.jms.test.simple

import grails.plugin.jms.Queue
import grails.plugin.jms.Subscriber
import grails.plugin.jms.test.TestListeningServiceSupport

class SimpleReceivingService extends TestListeningServiceSupport {

    static exposes = ['jms']
    
    @Queue
    def queue(msg) {
        log.info "queue received: $msg"
        putMessage(msg)
    }
    
    @Subscriber
    def simpleTopic(msg) {
        log.info "subscriber received: $msg"
        putMessage(msg)
    }

}