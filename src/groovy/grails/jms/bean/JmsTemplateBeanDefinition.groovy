package grails.jms.bean
import org.springframework.jms.core.JmsTemplate

class JmsTemplateBeanDefinition extends MapBasedBeanDefinitionBuilder {
    
    final static NAME_SUFFIX = "JmsTemplate"
    final static DEFAULT_CLASS = JmsTemplate
    
    JmsTemplateBeanDefinition(name, definition) {
        super(name + NAME_SUFFIX, definition)
    }
    
    def getClazz() {
        super.getClazz() ?: DEFAULT_CLASS
    }
}