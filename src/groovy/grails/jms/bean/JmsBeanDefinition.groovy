package grails.jms.bean

class JmsBeanDefinition {

    private name
    private definition
    
    JmsBeanDefinition(name, Map definition) {
        this.name = name
        this.definition = definition
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

    def register(beanBuilder) {
        beanBuilder.with {
            "$name"(clazz) { metaBean ->
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
        
    private set(name, value, recipient, beanBuilder) {
        if (name.endsWith('Bean')) {
            recipient."${name.substring(0, name.size() - 4)}" = beanBuilder.ref(value)
        } else {
            recipient."$name" = value
        }
    }
}