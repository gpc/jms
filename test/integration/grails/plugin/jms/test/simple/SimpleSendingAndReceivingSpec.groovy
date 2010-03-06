package grails.plugin.jms.test.simple

import grails.plugin.spock.*

class SimpleSendingAndReceivingSpec extends IntegrationSpec {

    def simpleSendingService
    def simpleReceivingService
    
    void testQueue() {
        when:
        simpleSendingService.sendToQueue("a")
        then:
        simpleReceivingService.message == "a"
    }

    void testSubscriber() {
        when:
        simpleSendingService.sendToTopic("a")
        then:
        simpleReceivingService.message == "a"
    }

}