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
package grails.plugin.jms.listener
import org.springframework.jms.core.MessagePostProcessor
import javax.jms.Message

class GrailsMessagePostProcessor implements MessagePostProcessor {

    def jmsTemplate
    def jmsService
    def processor

    def createDestination(destination) {
        def destinationMap = jmsService.convertToDestinationMap(destination)
        def session = jmsTemplate.createSession(jmsTemplate.createConnection())
        def destinationResolver = jmsTemplate.destinationResolver
        def isTopic = destinationMap.containsKey("topic") 
        def destinationString = (isTopic) ? destinationMap.topic : destinationMap.queue
        destinationResolver.resolveDestinationName(session, destinationString, isTopic)
    }
    
    Message postProcessMessage(Message message) {
        processor.delegate = this
        processor.resolveStrategy = Closure.DELEGATE_ONLY
        processor.call(message) ?: message
    }
}