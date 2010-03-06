package grails.plugin.jms.listener

import org.springframework.jms.listener.adapter.MessageListenerAdapter
import javax.jms.JMSException

class ServiceListenerAdapter extends MessageListenerAdapter {

    def persistenceInterceptor
    
    protected Object invokeListenerMethod(String methodName, Object[] arguments) throws JMSException {
        try {
            persistenceInterceptor.init()
        } finally {
            persistenceInterceptor.destroy()
        }
    }
}