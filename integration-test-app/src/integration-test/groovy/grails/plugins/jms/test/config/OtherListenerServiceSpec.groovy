package grails.plugins.jms.test.config

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import spock.lang.*

@TestMixin(IntegrationTestMixin)
class OtherListenerServiceSpec extends Specification {

    def jmsService
    def otherListenerService

    void testIt() {
        when:
        jmsService.send(service: 'otherListener', "a", "other")
        then:
        otherListenerService.message
    }
}
