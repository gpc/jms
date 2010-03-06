package grails.plugin.jms.test.config

import javax.jms.Message
import grails.plugin.jms.test.TestListeningServiceSupport

class OtherListenerService extends TestListeningServiceSupport {

    static exposes = ['jms']
    static listenerAdapter = "other"
    static listenerContainer = "other"
    
    def onMessage(msg) {
        putMessage(msg instanceof Message)
    }

}