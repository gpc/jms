package grails.plugin.jms.test.reply
import spock.lang.*
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*

@TestMixin(IntegrationTestMixin)
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
