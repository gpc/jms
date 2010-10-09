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
package grails.plugin.jms.test

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

abstract class TestListeningServiceSupport {

    static DEFAULT_QUEUE = 'default'
    static DEFAULT_WAIT_SECONDS = 5
    
    private messageQueues = [:]

    synchronized getMessageQueue(queueName = DEFAULT_QUEUE) {
        def messageQueue = messageQueues[queueName]
        if (messageQueue == null) {
            messageQueue = new LinkedBlockingQueue()
            messageQueues[queueName] = messageQueue
        }
        messageQueue
    }
    
    protected void putMessage(msg, queueName = DEFAULT_QUEUE) {
        getMessageQueue(queueName) << msg
    }
    
    def getMessage(waitSeconds = DEFAULT_WAIT_SECONDS, queueName = DEFAULT_QUEUE) {
        getMessageQueue(queueName).poll(waitSeconds, TimeUnit.SECONDS)
    }

}