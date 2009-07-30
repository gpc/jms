package grails.jms.listener
import grails.spring.BeanBuilder

class ListenerConfigTests extends GroovyTestCase {

    def newListenerConfig(properties) {
        new ListenerConfig(properties)
    }
    
    def newListenerConfig() {
        newListenerConfig([:])
    }
    
    void testGetBeanPrefix() {
        assertEquals("onMessagePerson", newListenerConfig(
            serviceBeanName: "personService", 
            listenerMethodOrClosureName: "onMessage").beanPrefix
        )
        assertEquals("person", newListenerConfig(
            serviceListener: true,
            serviceBeanName: "personService", 
            listenerMethodOrClosureName: "onMessage").beanPrefix
        )
    }
    
    void testGetDestinationName() {
        def mockGrailsApplication = new ConfigObject()
        mockGrailsApplication.metadata["app.name"] = "app"
        
        def lc1 = newListenerConfig(
            grailsApplication: mockGrailsApplication,
            serviceBeanName: "personService", 
            serviceListener: true
        )
        assertEquals("app.person", lc1.destinationName)
        
        def lc2 = newListenerConfig(
            serviceBeanName: "personService", 
            serviceListener: false,
            listenerMethodOrClosureName: "doSomething",
            topic: true
        )
        assertEquals("doSomething", lc2.destinationName)

        def lc3 = newListenerConfig(
            grailsApplication: mockGrailsApplication,
            serviceBeanName: "personService", 
            serviceListener: false,
            listenerMethodOrClosureName: "doSomething",
            topic: false
        )
        assertEquals("app.person.doSomething", lc3.destinationName)
    }
}