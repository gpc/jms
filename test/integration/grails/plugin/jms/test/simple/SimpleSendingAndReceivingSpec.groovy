package grails.plugin.jms.test.simple

import grails.plugin.spock.*
import spock.lang.Unroll

class SimpleSendingAndReceivingSpec extends IntegrationSpec {

    def simpleSendingService
    def simpleReceivingService
    
    def "receive with queue"() {
        when:
        simpleSendingService.sendToQueue("a")
        then:
        simpleReceivingService.message == "a"
    }

    def "receive with topic"() {
        when:
        simpleSendingService.sendToTopic("a")
        then:
        simpleReceivingService.message == "a"
    }

    @Unroll("Send message to #type with name #name")
    def "Should be able to send to a named destination"() {
        given:
        String message = 'A message'
        when:
        simpleSendingService."sendToGiven$type"(name, message )
        then:
        simpleReceivingService.message == message
        where:
        type    | name
        'Topic' | 'namedTopic'
        'Queue' | 'namedQueue'
        'Topic' | 'conf.named.topic'
        'Queue' | 'conf.named.queue'
    }

}