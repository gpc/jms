package grails.plugin.jms

import grails.plugin.jms.JmsService
import grails.util.Holders
import org.grails.config.PropertySourcesConfig
import spock.lang.Specification
import spock.lang.Unroll

class JmsServiceConfSpec extends Specification {

    def jmsService = new JmsService()

    @Unroll('the calculated receiver timeout should follow rule [#rule]')
    def 'receiveTimeout'() {
        given: 'a configuration'
        Holders.config = new PropertySourcesConfig(new ConfigSlurper().parse(aconfig).flatten())
        //the configuration is provided through the grailsApplication
        jmsService.grailsApplication = [config: Holders.config]

        and: 'a given jmsTemplate that has a timeout'
        def jmsTemplate = [receiveTimeout: aJmsTemplateTimeout]

        when: 'ask which timeout we should use'
        long timeout = jmsService.calculatedReceiverTimeout(aCallReceiveTimeout, jmsTemplate)

        then:
        timeout == expected

        where:
        rule                                     | aCallReceiveTimeout | aconfig                | aJmsTemplateTimeout | expected
        'explicit timeout above all'             | 5                   | 'jms.receiveTimeout=3' | 7                   | 5
        'template above conf if not 0'           | null                | 'jms.receiveTimeout=3' | 7                   | 7
        'conf above template if template is 0'   | null                | 'jms.receiveTimeout=3' | 0                   | 3
        'use default if conf and template are 0' | null                | 'jms.receiveTimeout=0' | 0                   | JmsService.DEFAULT_RECEIVER_TIMEOUT_MILLIS
        'use default if none provided'           | null                | ''                     | 0                   | JmsService.DEFAULT_RECEIVER_TIMEOUT_MILLIS
    }

    @Unroll('the JmsService should #action if the jms config is set to [#config]')
    def 'enable-disable'() {
        given:
        Holders.config = new PropertySourcesConfig(new ConfigSlurper().parse(config).flatten())

        when:
        jmsService.grailsApplication = [config: Holders.config]

        then:
        jmsService.disabled == expected

        where:
        action               | config               | expected
        'default to enabled' | ''                   | false
        'be disabled'        | 'jms.disabled=true'  | true
        'be enabled'         | 'jms.disabled=false' | false
    }

}
