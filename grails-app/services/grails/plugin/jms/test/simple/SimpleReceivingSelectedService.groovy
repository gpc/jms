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
package grails.plugin.jms.test.simple

import grails.plugin.jms.test.TestListeningServiceSupport
import java.util.concurrent.CyclicBarrier

class SimpleReceivingSelectedService extends TestListeningServiceSupport {

    public static final RECEIVING_QUEUE = "simpleReceivingSelectedQueue"

    public static final RECEIVING_TOPIC = "simpleReceivingSelectedTopic"

    static final long DEFAULT_TIMEOUT = 100l

    def callback = null

    def receiveSelectedFromQueue(selector, timeout = DEFAULT_TIMEOUT, template = null) {
        def msg = receiveSelectedJMSMessage(queue: RECEIVING_QUEUE, selector, timeout, template)
        log.info "queue messaged received with selector ${selector} : $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    def receiveSelectedFromTopic(selector, timeout = DEFAULT_TIMEOUT, template = null) {
        def msg = receiveSelectedJMSMessage(topic: RECEIVING_TOPIC, selector, timeout, template)
        log.info "topic messaged received with selector ${selector} : $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    def receiveSelectedAsyncFromQueue(CyclicBarrier barrier, selector, timeout = DEFAULT_TIMEOUT, template = null) {
        def future = receiveSelectedAsyncJMSMessage(queue: RECEIVING_QUEUE, selector, timeout, template)
        log.info "future obtained from queue with selector ${selector} : $future"
        log.debug "awaiting for barrier..."
        int index = barrier.await()
        log.debug "barrier triggered $index proceeding..."
        def msg = future.get()
        log.info "queue messaged received with selector ${selector} : $msg"
        putMessage(msg)
        callback?.call(msg)

    }

    def receiveSelectedAsyncFromTopic(CyclicBarrier barrier, selector, timeout = DEFAULT_TIMEOUT, template = null) {
        def future = receiveSelectedAsyncJMSMessage(topic: RECEIVING_TOPIC, selector, timeout, template)
        log.info "future obtained from topic with selector ${selector} : $future"
        log.debug "awaiting for barrier..."
        barrier.await()
        def msg = future.get()
        log.info "topic messaged received with selector ${selector} : $msg"
        putMessage(msg)
        callback?.call(msg)
    }
}