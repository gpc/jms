package grails.plugin.jms.listener

import grails.plugin.spock.UnitSpec
import org.codehaus.groovy.grails.support.MockApplicationContext
import spock.lang.Unroll

class ServiceInspectorSpec extends UnitSpec {

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

    @Unroll("destination[#key] resolves to expected value[#expected] with match[#match] over conf. [#configuration]")
    def 'able to resolve destination names through configuration'() {
        given:
        def applicationContext = new MockApplicationContext()
        applicationContext.metaClass.config = new ConfigSlurper().parse(configuration)

        when:
        String obtained = serviceInspector.resolveDestinationName(key, applicationContext)

        then:
        expected == obtained

        where:
        key              | expected           | match | configuration
        'queueKey'       | 'my.service.queue' | true  | "queueKey='my.service.queue'"
        'a.queue.key'    | 'my.service.queue' | true  | "a.queue.key='my.service.queue'"
        '.a.queue.key.'  | 'my.service.queue' | true  | "a.queue.key='my.service.queue'"
        'a.partial.path' | 'a.partial.path'   | false | "a.partial='not a valid value'"
        'no.match'       | 'no.match'         | false | "just.another.entry='something'"
        'empty.conf'     | 'empty.conf'       | false | ""
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