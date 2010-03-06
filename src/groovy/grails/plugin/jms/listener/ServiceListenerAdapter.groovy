package grails.plugin.jms.listener

import org.springframework.jms.listener.adapter.MessageListenerAdapter
import javax.jms.JMSException

class ServiceListenerAdapter extends MessageListenerAdapter {

    def listenerIsClosure
    def persistenceInterceptor
    
    protected Object invokeListenerMethod(String methodName, Object[] arguments) throws JMSException {
        try {
          persistenceInterceptor.init()
            
            if (listenerIsClosure) {
                try {
                    this.delegate."$methodName".call(arguments)
                } catch(Exception e) {
                    // swallow
                    null
                }
            } else {
                super.invokeListenerMethod(methodName, arguments)
            }
            
        } finally {
          persistenceInterceptor.destroy()
        }
    }
}