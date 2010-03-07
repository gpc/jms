package grails.plugin.jms.test.config

import javax.jms.Message
import grails.plugin.jms.test.TestListeningServiceSupport

class OtherListenerService extends TestListeningServiceSupport {

    static exposes = ['jms']
    static adapter = "other"
    static container = "other"
    
    def onMessage(msg) {
        putMessage(msg instanceof Message)
    }

}