package grails.plugin.jms.test.reply

import grails.plugin.spock.IntegrationSpec

class ReplyingListenerServiceSpec extends IntegrationSpec {

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
