package grails.plugin.jms.bean
import grails.spring.BeanBuilder
import org.springframework.jms.listener.DefaultMessageListenerContainer

class JmsListenerContainerAbstractBeanDefinitionBuilderTests extends GroovyTestCase {
    
    def bb
    
    void setUp() {
        bb = new BeanBuilder()
    }
    
    void testCreate() {
        def bdb = new JmsListenerContainerAbstractBeanDefinitionBuilder('example', [:])
        bdb.build(bb)
        assertTrue(bb.getBeanDefinition(bdb.name).'abstract')
    }
    
}
