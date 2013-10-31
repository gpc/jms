package grails.plugin.jms.test.config

import grails.plugin.spock.IntegrationSpec

class OtherListenerServiceSpec extends IntegrationSpec {

    def jmsService
    def otherListenerService

    void testIt() {
        when:
        jmsService.send(service: 'otherListener', "a", "other")
        then:
        otherListenerService.message
    }
}
