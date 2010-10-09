/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            log.debug("receiving message $message.JMSMessageID ($message.JMSDestination)")
        }
        super.onMessage(message)
        if (log.debugEnabled) {
            log.debug("received message $message.JMSMessageID ($message.JMSDestination)")
        }
    }

    void onMessage(Message message, Session session) {
        if (log.debugEnabled) {
            log.debug("receiving message (in session) $message.JMSMessageID ($message.JMSDestination)")
        }
        try {
            super.onMessage(message, session)
            if (log.debugEnabled) {
                log.debug("received message (in session) $message.JMSMessageID ($message.JMSDestination)")
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