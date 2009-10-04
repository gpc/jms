package grails.jms.bean
import grails.spring.BeanBuilder

class JmsTemplateBeanDefinitionBuilderTests extends GroovyTestCase {
    
    def bb
    
    void setUp() {
        bb = new BeanBuilder()
    }
    
    void testCreate() {
        def nameBase = "example"
        
        def jtbd = new JmsTemplateBeanDefinitionBuilder(nameBase, [meta: ['abstract': true]])
        jtbd.build(bb)
        def ajtbd = bb.getBeanDefinition(nameBase + JmsTemplateBeanDefinitionBuilder.nameSuffix)
        
        assertNotNull(ajtbd)
        assertEquals(JmsTemplateBeanDefinitionBuilder.defaultClazz.name, ajtbd.beanClassName)
    }
    
}