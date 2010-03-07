package grails.plugin.jms.test.reply

import grails.plugin.jms.Queue
import grails.plugin.jms.test.TestListeningServiceSupport

class ReplyingListenerService extends TestListeningServiceSupport {

    static exposes = ['jms']
    
    @Queue
    def initial(msg) {
        1
    }
    
    @Queue
    def reply(msg) {
        putMessage(msg)
    }

}