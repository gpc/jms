package grails.plugin.jms.bean

class MapBasedBeanDefinitionBuilder {

    private name
    private definition
    
    MapBasedBeanDefinitionBuilder(name, Map definition) {
        this.name = name
        this.definition = definition
    }

    def getName() {
        name
    }
    
    def getClazz() {
        definition.clazz
    }
    
    def getMeta() {
        definition.meta
    }    

    def getProperties() {
        def properties = definition.clone()
        properties.remove('clazz')
        properties.remove('meta')
        properties
    }

    def build(beanBuilder) {
        beanBuilder.with {
            "${this.getName()}"(clazz) { metaBean ->
                def bean = delegate
                
                this.meta.each { k,v ->
                    this.set(k, v, metaBean, beanBuilder)
                }
                this.properties.each { k,v ->
                    this.set(k, v, bean, beanBuilder)
                }
            }
        }
    }
        
    protected set(name, value, recipient, beanBuilder) {
        if (name.endsWith('Bean')) {
            recipient."${name.substring(0, name.size() - 4)}" = beanBuilder.ref(value)
        } else {
            recipient."$name" = value
        }
    }
}