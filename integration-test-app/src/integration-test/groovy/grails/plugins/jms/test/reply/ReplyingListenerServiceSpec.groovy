package grails.plugins.jms.test.reply

import grails.test.mixin.integration.Integration
import spock.lang.Specification

@Integration
class ReplyingListenerServiceSpec extends Specification {

    def jmsService
    def replyingListenerService

    void testIt() {
        when:
        jmsService.send(service: 'replyingListener', method: 'initial', 1) {
            it.JMSReplyTo = createDestination(service: 'replyingListener', method: 'reply')
            it
        }
        then:
        replyingListenerService.message == 1
    }
}
