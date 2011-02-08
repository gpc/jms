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

class SimpleQueueBrowserService extends TestListeningServiceSupport {

    public static final BROWSER_QUEUE = "simpleBrowserQueue"


    def jmsService

    def callback = null

    def browse( callback = null ) {
        def messages = jmsService.browse(BROWSER_QUEUE, callback)
        log.info "queue messages received : $messages"
        messages.each {
            putMessage(it)
        }
    }

    def browseNoConvert( callback = null ) {
        def messages = jmsService.browseNoConvert(BROWSER_QUEUE, callback)
        log.info "queue messages received : $messages"
        messages.each {
            putMessage(it)
        }
    }

    def browseSelected( selector, callback = null ) {
        def messages = jmsService.browseSelected(BROWSER_QUEUE, selector, callback)
        log.info "queue messages received : $messages"
        messages.each {
            putMessage(it)
        }
    }

    def browseSelectedNotConvert( selector, callback = null ) {
        def messages = jmsService.browseSelectedNotConvert(BROWSER_QUEUE, selector, callback)
        log.info "queue messages received : $messages"
        messages.each {
            putMessage(it)
        }
    }

    def purge( ){
        log.info "purging.."
        int cx = 0
        while ( receiveSelectedJMSMessage( queue: BROWSER_QUEUE, null, 100l) ){ cx++ }

        log.info "purging done $cx."

        return cx
    }
}