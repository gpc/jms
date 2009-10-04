package grails.jms.bean
import org.springframework.jms.listener.DefaultMessageListenerContainer

class JmsListenerContainerAbstractBeanDefinitionBuilder extends JmsBeanDefinitionBuilder {
    
    final static nameSuffix = "JmsListenerContainer"
    final static defaultClazz = DefaultMessageListenerContainer
    
    JmsListenerContainerAbstractBeanDefinitionBuilder(name, definition) {
        super(name, definition)
    }
    
    def getMeta() {
        (super.getMeta() ?: [:]) + ['abstract': true]
    }
}