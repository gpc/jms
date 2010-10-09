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
                persistenceInterceptor.flush()
                persistenceInterceptor.destroy()
            }
        }
    }
}