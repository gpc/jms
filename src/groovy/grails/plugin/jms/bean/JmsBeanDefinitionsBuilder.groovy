package grails.plugin.jms.bean

class JmsBeanDefinitionsBuilder {

    static mappings = [
        templates: JmsTemplateBeanDefinitionBuilder,
        containers: JmsListenerContainerAbstractBeanDefinitionBuilder,
        adapters: JmsListenerAdapterAbstractBeanDefinitionBuilder,
        converters: JmsMessageConverterBeanDefinitionBuilder
    ]
    
    final beans
    
    JmsBeanDefinitionsBuilder(beans) {
        this.beans = beans
    }
    
    def build(beanBuilder) {
        mappings.each { key, builderClazz ->
            beans[key].each { name, definition ->
                builderClazz.newInstance(name, definition).build(beanBuilder)
            }
        }
    }
    
    def removeFrom(context) {
        mappings.each { key, builderClazz ->
            beans[key].each { name, definition ->
                builderClazz.newInstance(name, definition).removeFrom(context)
            }
        }
    }

}