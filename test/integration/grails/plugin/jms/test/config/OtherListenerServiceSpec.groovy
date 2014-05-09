package grails.plugin.jms.test.config


import spock.lang.*
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*

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
