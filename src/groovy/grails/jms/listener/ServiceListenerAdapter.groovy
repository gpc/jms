package grails.jms.listener

import org.springframework.jms.listener.adapter.MessageListenerAdapter
import javax.jms.JMSException

class ServiceListenerAdapter extends MessageListenerAdapter {
	def listenerIsClosure

	protected Object invokeListenerMethod(String methodName, Object[] arguments) throws JMSException {
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
	}
}