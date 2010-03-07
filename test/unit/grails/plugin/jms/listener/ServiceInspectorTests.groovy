package grails.plugin.jms.listener

class ServiceInspectorTests extends GroovyTestCase {

    def serviceInspector = new ServiceInspector()
    
    void testExposesJms() {
        assertTrue(serviceInspector.exposesJms(DoesExposeJms))
        assertFalse(serviceInspector.exposesJms(DoesntExposeJms))
    }
    
    void testIsSingleton() {
        assertTrue(serviceInspector.isSingleton(ImplicitSingleton))
        assertTrue(serviceInspector.isSingleton(ExplicitSingleton))
        assertFalse(serviceInspector.isSingleton(NonSingleton))
    }
        
    void testHasServiceListenerMethod() {
        assertTrue(serviceInspector.hasServiceListenerMethod(HasServiceListenerMethod))
        assertFalse(serviceInspector.hasServiceListenerMethod(HasNoServiceListener))
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