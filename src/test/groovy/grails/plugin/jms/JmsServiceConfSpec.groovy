package grails.plugin.jms

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class JmsServiceConfSpec extends Specification implements ServiceUnitTest<JmsService>  {

    @Unroll("the calculated receiver timeout should follow rule [#rule]")
    def "receiveTimeout"() {

        given: "a configuration"
            config.jms = [receiveTimeout: configReceiveTimeout]

        and: "a given jmsTemplate that has a timeout"
            def jmsTemplate = [receiveTimeout: aJmsTemplateTimeout]

        when: "ask which timeout we should use"
            long timeout = service.calculatedReceiverTimeout(aCallReceiveTimeout, jmsTemplate)

        then:
            timeout == expected

        where:
            rule                                        | aCallReceiveTimeout | configReceiveTimeout | aJmsTemplateTimeout | expected
            "explicit timeout above all"                | 5                   | 3                    | 7                   |  5
            "template above conf if not 0"              | null                | 3                    | 7                   |  7
            "conf above template if template is 0"      | null                | 3                    | 0                   |  3
            "use default if conf and template are 0"    | null                | 0                    | 0                   |  JmsService.DEFAULT_RECEIVER_TIMEOUT_MILLIS
            "use default if none provided"              | null                | null                 | 0                   |  JmsService.DEFAULT_RECEIVER_TIMEOUT_MILLIS
    }

    @Unroll("the JmsService should #action if the jms config is set to [#config]")
    def "enable-disable"() {
        when:
            config.jms = [disabled: disabledConfig]

        then:
            service.disabled == expected

        where:
            action              | disabledConfig | expected
            "default to enabled"| null           | false
            "be disabled"       | true           | true
            "be enabled"        | false          | false
    }
}
