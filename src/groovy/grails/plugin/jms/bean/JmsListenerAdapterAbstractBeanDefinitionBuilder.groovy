package grails.plugin.jms.bean
import org.springframework.jms.listener.adapter.MessageListenerAdapter

class JmsListenerAdapterAbstractBeanDefinitionBuilder extends JmsBeanDefinitionBuilder {

    final static nameSuffix = "JmsListenerAdapter"
    final static defaultClazz = MessageListenerAdapter
    
    JmsListenerAdapterAbstractBeanDefinitionBuilder(name, definition) {
        super(name, definition)
    }
    
    def getMeta() {
        (super.getMeta() ?: [:]) + ['abstract': true]
    }

}