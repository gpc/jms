package grails.plugin.jms.listener
import grails.spring.BeanBuilder

class ListenerConfigTests extends GroovyTestCase {

    def newListenerConfig(properties) {
        new ListenerConfig(properties)
    }
    
    def newListenerConfig() {
        newListenerConfig([:])
    }
    
    void testGetBeanPrefix() {
        assertEquals("personOnMessage", newListenerConfig(
            serviceBeanName: "personService", 
            listenerMethodName: "onMessage").beanPrefix
        )
        assertEquals("person", newListenerConfig(
            serviceListener: true,
            serviceBeanName: "personService", 
            listenerMethodName: "onMessage").beanPrefix
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
            listenerMethodName: "doSomething",
            topic: true
        )
        assertEquals("doSomething", lc2.destinationName)

        def lc3 = newListenerConfig(
            grailsApplication: mockGrailsApplication,
            serviceBeanName: "personService", 
            serviceListener: false,
            listenerMethodName: "doSomething",
            topic: false
        )
        assertEquals("app.person.doSomething", lc3.destinationName)
    }
}