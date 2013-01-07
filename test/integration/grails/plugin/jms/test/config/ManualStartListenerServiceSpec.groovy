package grails.plugin.jms.test.config

import grails.plugin.spock.IntegrationSpec

class ManualStartListenerServiceSpec extends IntegrationSpec {

    def jmsService
    def manualStartListenerService
    def manualStartListenerJmsListenerContainer

    def 'messages are not received by a listener with autoStartup equal to false'() {
        when: 'we send a message to an queue without a started listener'
        jmsService.send(service: 'manualStartListener', "a")

        then: 'we receive no messages'
        manualStartListenerService.message == null

        when: 'we manually start the listener'
        manualStartListenerJmsListenerContainer.start()

        then: 'wew receive the message'
        manualStartListenerService.message == 'a'
    }
}