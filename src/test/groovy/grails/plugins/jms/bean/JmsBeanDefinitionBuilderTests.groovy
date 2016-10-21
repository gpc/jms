package grails.plugins.jms.bean

import grails.spring.BeanBuilder

class JmsBeanDefinitionBuilderTests extends GroovyTestCase {

    private bb = new BeanBuilder()

    void testCreate() {
        def nameBase = "example"

        def bdb = new JmsBeanDefinitionBuilderTestsTestImpl(nameBase, [a: "a"])

        assertEquals(nameBase + JmsBeanDefinitionBuilderTestsTestImpl.nameSuffix, bdb.name)
        assertEquals(JmsBeanDefinitionBuilderTestsTestImpl.defaultClazz, bdb.clazz)
    }

    void testExplicitClass() {
        def nameBase = "example"

        def bdb = new JmsBeanDefinitionBuilderTestsTestImpl(nameBase, [a: "a", clazz: String])

        assertEquals(String, bdb.getClazz())
    }
}

class JmsBeanDefinitionBuilderTestsTestImpl extends JmsBeanDefinitionBuilder {

    JmsBeanDefinitionBuilderTestsTestImpl(name, definition) {
        super(name, definition)
    }

    static final String nameSuffix = "Suffix"
    static final Class defaultClazz = [:].getClass()
}
