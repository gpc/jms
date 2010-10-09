package grails.plugin.jms.bean
import org.springframework.jms.support.converter.SimpleMessageConverter

class JmsMessageConverterBeanDefinitionBuilder extends JmsBeanDefinitionBuilder {
    
    final static nameSuffix = "JmsMessageConverter"
    final static defaultClazz = SimpleMessageConverter
    
    JmsMessageConverterBeanDefinitionBuilder(name, definition) {
        super(name, definition)
    }
    
}