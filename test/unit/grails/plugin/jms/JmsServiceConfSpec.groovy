package grails.plugin.jms

import grails.plugin.spock.UnitSpec
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.jms.core.JmsTemplate


class JmsServiceConfSpec extends UnitSpec {

    def jmsService

    def setup() {
        jmsService = new JmsService()
    }

    void "The callReceiveTimeout argument should supersede in calculatedReceiverTimeout"() {
        when:
        mockConfig """
        jms.receiveTimeout=100l
        """
        jmsService.grailsApplication = [config: ConfigurationHolder.config]
        assert jmsService.grailsApplication.config.jms.receiveTimeout == 100l
        def jmsTemplate = [receiveTimeout: 1000l]
        then:
        jmsService.calculatedReceiverTimeout(5l, jmsTemplate) == 5l
    }

    void "The jmsTemplate#receiverTimeout value should be used if different than JmsTemplate#RECEIVE_TIMEOUT_INDEFINITE_WAIT"() {
        when:
        mockConfig """
        jms.receiveTimeout=100l
        """
        jmsService.grailsApplication = [config: ConfigurationHolder.config]
        assert jmsService.grailsApplication.config.jms.receiveTimeout == 100l
        def jmsTemplate = [receiveTimeout: 1000l]
        then:
        jmsService.calculatedReceiverTimeout(null, jmsTemplate) == 1000l
    }

    void "The jmsTemplate#receiverTimeout value shouldn't be used if eq JmsTemplate#RECEIVE_TIMEOUT_INDEFINITE_WAIT"() {
        when:
        mockConfig """
        jms.receiveTimeout=100l
        """
        jmsService.grailsApplication = [config: ConfigurationHolder.config]
        def jmsTemplate = [receiveTimeout: JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT]
        then:
        jmsService.calculatedReceiverTimeout(null, jmsTemplate) == 100l
    }

    void "The default receiverTimeout should be used if no valied value is specified"() {
        when:
        mockConfig """
        jms.receiveTimeout=org.springframework.core.jms.JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT
        """
        jmsService.grailsApplication = [config: ConfigurationHolder.config]
        def jmsTemplate = [receiveTimeout: JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT]
        then:
        jmsService.calculatedReceiverTimeout(null, jmsTemplate) == JmsService.DEFAULT_RECEIVER_TIMEOUT_MILLIS
    }

    void "The JmsService should be disabled if specified in the configuration"() {
        setup:
        mockConfig model.config
        when:
        jmsService.grailsApplication = [config: ConfigurationHolder.config]
        then:
        jmsService.disabled == model.expected
        where:
        model << [
                [config: "", expected: false],
                [config: "jms.disabled=true", expected: true],
                [config: "jms.disabled=false", expected: false]

        ]
    }
}