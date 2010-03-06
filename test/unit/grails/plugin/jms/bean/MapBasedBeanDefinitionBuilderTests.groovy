package grails.plugin.jms.bean
import grails.spring.BeanBuilder

class MapBasedBeanDefinitionBuilderTests extends GroovyTestCase {

    def bb
    
    void setUp() {
        bb = new BeanBuilder()
    }
    
    void testCreate() {
        
        def s1Value = "s1Value"
        def s2Value = "s2Value"
        def i1Value = 1
        
        bb.parent(MapBasedBeanDefinitionBuilderTestsTestBean) { 
            it.'abstract' = true 
            s2 = s2Value
        }
        
        bb.s1(String, s1Value)

        def definition = [
            meta: [
                parentBean: "parent"
            ],
            clazz: MapBasedBeanDefinitionBuilderTestsTestBean,
            s1Bean: "s1",
            i1: i1Value
        ]
        
        def jbd = new MapBasedBeanDefinitionBuilder("test", definition)
        jbd.build(bb)
        
        def bd = bb.getBeanDefinition("test")
        assertNotNull(bd)
        
        assertEquals(MapBasedBeanDefinitionBuilderTestsTestBean.name, bd.beanClassName)
        assertEquals("parent", bd.parentName)
        
        def ac = bb.createApplicationContext()
        
        def testBean = ac.getBean('test')
        assertNotNull(testBean)
        
        assertEquals(s1Value, testBean.s1)
        assertEquals(s2Value, testBean.s2)
        assertEquals(i1Value, testBean.i1)
    }

}

class MapBasedBeanDefinitionBuilderTestsTestBean {

    String s1
    String s2
    Integer i1

}