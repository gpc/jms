package grails.plugin.jms.listener.adapter

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

class PersistenceContextAwareListenerAdapter extends LoggingListenerAdapter {
    
    def persistenceInterceptor

    // Needed to workaround groovy bug with call to super in LoggingListenerAdapter
    void onMessage(Message message) {
        super.onMessage(message)
    }

    // Needed to workaround groovy bug with call to super in LoggingListenerAdapter
    void onMessage(Message message, Session session) {
        super.onMessage(message, session)
    }
    
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