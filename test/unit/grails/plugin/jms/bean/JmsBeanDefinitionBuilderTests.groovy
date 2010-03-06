package grails.plugin.jms.bean
import grails.spring.BeanBuilder

class JmsBeanDefinitionBuilderTests extends GroovyTestCase {
    
    def bb
    
    void setUp() {
        bb = new BeanBuilder()
    }
    
    void testCreate() {
        def nameBase = "example"
        
        def bdb = new JmsBeanDefinitionBuilderTestsTestImpl(nameBase, [a: "a"])
        
        assertEquals(nameBase + JmsBeanDefinitionBuilderTestsTestImpl.nameSuffix, bdb.name)
        assertEquals(JmsBeanDefinitionBuilderTestsTestImpl.defaultClazz, bdb.clazz)
    }
    
    void testExplicitClass() {
        def nameBase = "example"
        
        def bdb = new JmsBeanDefinitionBuilderTestsTestImpl(nameBase, [a: "a", clazz: String])
        
        assertEquals(String, bdb.clazz)
    }
    
}

class JmsBeanDefinitionBuilderTestsTestImpl extends JmsBeanDefinitionBuilder {

    JmsBeanDefinitionBuilderTestsTestImpl(name, definition) {
        super(name, definition)
    }
    
    final static nameSuffix = "Suffix"
    final static defaultClazz = [:].getClass()

}