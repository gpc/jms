package grails.plugin.jms.test.simple

import grails.plugin.jms.Queue
import grails.plugin.jms.Subscriber
import grails.plugin.jms.test.TestListeningServiceSupport

class SimpleReceivingService extends TestListeningServiceSupport {

    static exposes = ['jms']
    
    def callback = null
    
    @Queue
    def queue(msg) {
        log.info "queue received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }
    
    @Subscriber
    def simpleTopic(msg) {
        log.info "subscriber received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    @Queue(container = "transacted")
    def transactionalQueue(msg) {
        log.info "transactional queue received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }
}