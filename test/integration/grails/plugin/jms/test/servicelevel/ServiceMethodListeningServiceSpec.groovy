package grails.plugin.jms.test.servicelevel

import grails.plugin.spock.*

class ServiceMethodListeningServiceSpec extends IntegrationSpec {

    def jmsService
    def serviceMethodListeningService
    
    void testIt() {
        when:
        jmsService.send(service: 'serviceMethodListening', "a")
        then:
        serviceMethodListeningService.message == "a"
    }

}