package grails.plugin.jms.listener

import spock.lang.*
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class ServiceInspectorSpec extends Specification {

    def serviceInspector = new ServiceInspector()

    def 'know if a service is exposes through Jms'() {
        expect:
        serviceInspector.exposesJms(DoesExposeJms)
        !serviceInspector.exposesJms(DoesntExposeJms)
    }

    def 'know if a Service is a Singleton'() {
        expect:
        serviceInspector.isSingleton(ImplicitSingleton)
        serviceInspector.isSingleton(ExplicitSingleton)
        !serviceInspector.isSingleton(NonSingleton)
    }

    def 'know if a Service has a Listener Method'() {
        expect:
        serviceInspector.hasServiceListenerMethod(HasServiceListenerMethod)
        !serviceInspector.hasServiceListenerMethod(HasNoServiceListener)
    }

    @Unroll("key [#key] resolves to expected value [#expected] with conf [#configuration]")
    def 'able to resolve destination names through configuration'() {
        given:
        replaceGrailsApplicationConfig(configuration)

        when:
        String obtained = serviceInspector.resolveDestinationName(key, grailsApplication)

        then:
        expected == obtained

        where:
        key              | expected           | configuration
        'baseCase'       | 'baseCase'         | ""
        '$queueKey'      | 'my.service.queue' | "jms.destinations.queueKey='my.service.queue'"
        '$a.queue.key'   | 'my.service.queue' | "jms.destinations.a.queue.key='my.service.queue'"
        '$.a.queue.key.' | 'my.service.queue' | "jms.destinations.a.queue.key='my.service.queue'"
    }

    def 'fail if we are unable to resolve destination names'() {
        given:
            replaceGrailsApplicationConfig("just.another.entry='something'")

        when:
        serviceInspector.resolveDestinationName('$no.match', grailsApplication)

        then:
        thrown(IllegalArgumentException)
    }

    void replaceGrailsApplicationConfig(String configuration){
        grailsApplication.config = new ConfigSlurper().parse(configuration)
    }

}

class DoesExposeJms {
    static exposes = ["blah", "jms"]
}
class DoesntExposeJms {}

class ImplicitSingleton {}

class ExplicitSingleton {
    static scope = "singleton"
}
class NonSingleton {
    static scope = "session"
}
class DefaultServiceListenerName {}

class HasServiceListenerMethod {
    def onMessage(msg) {}
}
class HasNoServiceListener {}
