package grails.plugin.jms.listener.adapter

import org.springframework.jms.listener.adapter.MessageListenerAdapter
import javax.jms.JMSException
import org.apache.commons.logging.LogFactory

class PersistenceContextAwareListenerAdapter extends MessageListenerAdapter {

    static log = LogFactory.getLog(PersistenceContextAwareListenerAdapter)
    
    def persistenceInterceptor
    
    protected Object invokeListenerMethod(String methodName, Object[] arguments) throws JMSException {
        try {
            if (persistenceInterceptor) {
                log.debug("opening persistence context for listener $methodName of $delegate")
                persistenceInterceptor.init()
            } else {
                log.debug("no persistence interceptor for listener $methodName of $delegate")
            }
            super.invokeListenerMethod(methodName, *arguments)
        } finally {
            if (persistenceInterceptor) {
                log.debug("destroying persistence context for listener $methodName of $delegate")
                persistenceInterceptor.destroy()
            }
        }
    }
}