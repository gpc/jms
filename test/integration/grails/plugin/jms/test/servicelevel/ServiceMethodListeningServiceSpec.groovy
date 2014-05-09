package grails.plugin.jms.test.servicelevel

import spock.lang.*
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*

@TestMixin(IntegrationTestMixin)
class ServiceMethodListeningServiceSpec extends Specification {

    def jmsService
    def serviceMethodListeningService

    void testIt() {
        when:
        jmsService.send(service: 'serviceMethodListening', "a")
        then:
        serviceMethodListeningService.message == "a"
    }
}
