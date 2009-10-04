package grails.jms.bean
import org.springframework.jms.core.JmsTemplate

class JmsTemplateBeanDefinitionBuilder extends JmsBeanDefinitionBuilder {
    
    final static nameSuffix = "JmsTemplate"
    final static defaultClazz = JmsTemplate
    
    JmsTemplateBeanDefinitionBuilder(name, definition) {
        super(name, definition)
    }
    
}