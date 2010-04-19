package grails.plugin.jms.listener.adapter

import org.springframework.jms.listener.adapter.MessageListenerAdapter
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.InitializingBean
import org.apache.commons.lang.StringUtils
import javax.jms.Message
import javax.jms.Session

class LoggingListenerAdapter extends MessageListenerAdapter implements InitializingBean {

    protected log
    
    void afterPropertiesSet() {
        log = createLog()
    }
    
    void onMessage(Message message) {
        if (log.debugEnabled) {
            log.debug("receiving message $message.JMSMessageID ($message.destination)")
        }
        super.onMessage(message)
        if (log.debugEnabled) {
            log.debug("received message $message.JMSMessageID ($message.destination)")
        }
    }

    void onMessage(Message message, Session session) {
        if (log.debugEnabled) {
            log.debug("receiving message (in session) $message.JMSMessageID ($message.destination)")
        }
        try {
            super.onMessage(message, session)
            if (log.debugEnabled) {
                log.debug("received message (in session) $message.JMSMessageID ($message.destination)")
            }
        } catch (Throwable e) {
            handleListenerException(e)
            throw e
        }
    }
    
    protected void handleListenerException(Throwable ex) {
        if (log.errorEnabled) {
            log.error("Exception raised in message listener", ex)
        }
    }
    
    protected createLog() {
        LogFactory.getLog("${this.class.name}.${StringUtils.uncapitalize(delegate.class.name - 'Service')}.${defaultListenerMethod}".toString())
    }

}