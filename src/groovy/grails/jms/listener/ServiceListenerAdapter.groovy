package grails.jms.listener

import org.springframework.jms.listener.adapter.MessageListenerAdapter
import javax.jms.JMSException

class ServiceListenerAdapter extends MessageListenerAdapter {
    def listenerIsClosure
}