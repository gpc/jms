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
package grails.plugins.jms.test.simple

import grails.plugins.jms.Queue
import grails.plugin.jms.Subscriber
import grails.plugins.jms.test.TestListeningServiceSupport

class SimpleReceivingService extends TestListeningServiceSupport {

    static exposes = ['jms']

    def callback

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

    @Subscriber(topic='namedTopic')
    def namedTopic(msg) {
        log.info "namedTopic received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    @Queue(name='namedQueue')
    def namedQueue(msg) {
        log.info "namedQueue received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    @Subscriber(topic='$named.topic.key')
    def namedTopicByConfigurationKey(msg) {
        log.info "conf.named.topic received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }

    @Queue(name='$named.queue.key')
    def namedQueueByConfigurationKey(msg) {
        log.info "conf.named.queue received: $msg"
        putMessage(msg)
        callback?.call(msg)
    }
}
