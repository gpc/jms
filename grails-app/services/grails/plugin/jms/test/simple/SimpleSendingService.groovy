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

class SimpleSendingService {

    def sendToQueue(msg, template = null) {
        sendJMSMessage(service: 'simpleReceiving', method: 'queue', msg, template)
    }

    def sendToTransactionalQueue(msg, template = null) {
        sendJMSMessage(service: 'simpleReceiving', method: 'transactionalQueue', msg, template)
    }
    
    def sendToTopic(msg) {
        sendJMSMessage(topic: 'simpleTopic', msg)
    }

    def sendToGivenQueue(queue, msg, template = null, postProcessor = null) {
        sendJMSMessage(queue: queue, msg, template, postProcessor)
    }

    def sendToGivenTopic(topic, msg, template = null, postProcessor = null) {
        sendJMSMessage(topic: topic, msg, template, postProcessor)
    }

}