package grails.plugins.jms.test.servicelevel

import grails.test.mixin.integration.Integration
import spock.lang.*

@Integration
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
