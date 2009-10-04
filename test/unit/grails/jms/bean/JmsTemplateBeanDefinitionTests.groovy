package grails.jms.bean
import grails.spring.BeanBuilder

class JmsTemplateBeanDefinitionTests extends GroovyTestCase {
    
    def bb
    
    void setUp() {
        bb = new BeanBuilder()
    }
    
    void testCreate() {
        def nameBase = "example"
        
        def jtbd = new JmsTemplateBeanDefinition(nameBase, [meta: ['abstract': true]])
        jtbd.build(bb)
        def ajtbd = bb.getBeanDefinition(nameBase + JmsTemplateBeanDefinition.NAME_SUFFIX)
        
        assertNotNull(ajtbd)
        assertEquals(JmsTemplateBeanDefinition.DEFAULT_CLASS.name, ajtbd.beanClassName)
    }
    
}