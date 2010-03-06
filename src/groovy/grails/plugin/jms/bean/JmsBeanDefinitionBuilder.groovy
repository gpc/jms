package grails.plugin.jms.bean

abstract class JmsBeanDefinitionBuilder extends MapBasedBeanDefinitionBuilder {
    
    JmsBeanDefinitionBuilder(name, definition) {
        super(name, definition)
    }
    
    def getName() {
        super.getName() + this.class.nameSuffix
    }
    
    def getClazz() {
        super.getClazz() ?: this.class.defaultClazz
    }
    
    static getNameSuffix() {
        throw new IllegalStateException("${this} does not implement getNameSuffix()")
    }

    static getDefaultClazz() {
        throw new IllegalStateException("${this} does not implement getDefaultClazz()")
    }
    
}