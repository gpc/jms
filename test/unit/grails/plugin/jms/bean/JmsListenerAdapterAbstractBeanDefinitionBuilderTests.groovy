package grails.plugin.jms.bean
import grails.spring.BeanBuilder

class JmsListenerAdapterAbstractBeanDefinitionBuilderTests extends GroovyTestCase {
    
    def bb
    
    void setUp() {
        bb = new BeanBuilder()
    }
    
    void testCreate() {
        def bdb = new JmsListenerAdapterAbstractBeanDefinitionBuilder('example', [:])
        bdb.build(bb)
        assertTrue(bb.getBeanDefinition(bdb.name).'abstract')
    }
    
}
