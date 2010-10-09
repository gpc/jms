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

import grails.plugin.jms.Queue
import grails.plugin.jms.Subscriber
import grails.plugin.jms.test.TestListeningServiceSupport

class SimpleReceivingService extends TestListeningServiceSupport {

    static exposes = ['jms']
    
    def callback = null
    
    @Queue
    def queue(msg) {
        log.info "queue received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }
    
    @Subscriber
    def simpleTopic(msg) {
        log.info "subscriber received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    @Queue(container = "transacted")
    def transactionalQueue(msg) {
        log.info "transactional queue received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }
}