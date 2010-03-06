package grails.plugin.jms.bean
import grails.plugin.jms.listener.adapter.PersistenceContextAwareListenerAdapter

class JmsListenerAdapterAbstractBeanDefinitionBuilder extends JmsBeanDefinitionBuilder {

    final static nameSuffix = "JmsListenerAdapter"
    final static defaultClazz = PersistenceContextAwareListenerAdapter
    
    JmsListenerAdapterAbstractBeanDefinitionBuilder(name, definition) {
        super(name, definition)
    }
    
    def getMeta() {
        (super.getMeta() ?: [:]) + ['abstract': true]
    }

}