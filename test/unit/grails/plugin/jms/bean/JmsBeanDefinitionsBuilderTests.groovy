package grails.plugin.jms.bean
import grails.spring.BeanBuilder

class JmsBeanDefinitionsBuilderTests extends GroovyTestCase {

    void testIt() {

        def bb = new BeanBuilder(this.class.classLoader)
            
        def beans = [
            converters: [standard: [:]],
            templates: [
                standard: [
                    meta: ['abstract': true]
                ]
            ],
            containers: [
                standard: [:]
            ],
            adapters: [
                standard: [:]
            ]
        ]
        
        new JmsBeanDefinitionsBuilder(beans).build(bb)
        
        JmsBeanDefinitionsBuilder.mappings.each { key, builderClazz ->
            assertNotNull(bb.getBeanDefinition('standard' + builderClazz.nameSuffix))
        }
        
    }
}