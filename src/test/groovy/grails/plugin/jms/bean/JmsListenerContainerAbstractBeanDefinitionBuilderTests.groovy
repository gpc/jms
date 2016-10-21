package grails.plugin.jms.bean

import grails.spring.BeanBuilder

class JmsListenerContainerAbstractBeanDefinitionBuilderTests extends GroovyTestCase {

    private bb = new BeanBuilder()

    void testCreate() {
        def bdb = new JmsListenerContainerAbstractBeanDefinitionBuilder('example', [:])
        bdb.build(bb)
        assertTrue(bb.getBeanDefinition(bdb.name).'abstract')
    }
}
