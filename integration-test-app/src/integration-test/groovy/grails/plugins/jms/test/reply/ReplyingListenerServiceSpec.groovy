package grails.plugins.jms.test.reply

import grails.test.mixin.integration.Integration
import spock.lang.*
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*

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
