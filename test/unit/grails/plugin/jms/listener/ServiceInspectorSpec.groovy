package grails.plugin.jms.listener

import grails.plugin.spock.UnitSpec

import org.codehaus.groovy.grails.support.MockApplicationContext

import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

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

    @ConfineMetaClassChanges([MockApplicationContext])
    @Unroll("key [#key] resolves to expected value [#expected] with conf [#configuration]")
    def 'able to resolve destination names through configuration'() {
        given:
        def applicationContext = getApplicationContext(configuration)

        when:
        String obtained = serviceInspector.resolveDestinationName(key, applicationContext)

        then:
        expected == obtained

        where:
        key              | expected           | configuration
        'baseCase'       | 'baseCase'         | ""
        '$queueKey'      | 'my.service.queue' | "jms.destinations.queueKey='my.service.queue'"
        '$a.queue.key'   | 'my.service.queue' | "jms.destinations.a.queue.key='my.service.queue'"
        '$.a.queue.key.' | 'my.service.queue' | "jms.destinations.a.queue.key='my.service.queue'"
    }

    @ConfineMetaClassChanges([MockApplicationContext])
    def 'fail if we are unable to resolve destination names'() {
        given:
        def applicationContext = getApplicationContext("just.another.entry='something'")

        when:
        serviceInspector.resolveDestinationName('$no.match', applicationContext)

        then:
        thrown(IllegalArgumentException)
    }

    def getApplicationContext(String configuration){
        def applicationContext = new MockApplicationContext()
        applicationContext.metaClass.config = new ConfigSlurper().parse(configuration)
        applicationContext
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
