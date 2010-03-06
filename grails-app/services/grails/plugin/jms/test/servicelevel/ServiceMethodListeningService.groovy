package grails.plugin.jms.test.servicelevel

import java.util.concurrent.LinkedBlockingQueue
import grails.plugin.jms.test.TestListeningServiceSupport

class ServiceMethodListeningService extends TestListeningServiceSupport {

    static exposes = ['jms']
    
    def onMessage(msg) {
        putMessage(msg)
    }

}