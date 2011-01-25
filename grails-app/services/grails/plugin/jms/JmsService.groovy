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
package grails.plugin.jms

import javax.jms.Destination
import javax.jms.Topic

import grails.plugin.jms.listener.GrailsMessagePostProcessor
import org.apache.commons.logging.LogFactory
import org.springframework.jms.core.JmsTemplate

/**
 * @todo Enable TTL for the Template.
 */
class JmsService {

    static transactional = false
    static final LOG = LogFactory.getLog(JmsService)
    static final DEFAULT_JMS_TEMPLATE_BEAN_NAME = "standard"
    static final long DEFAULT_RECEIVER_TIMEOUT_MILLIS = 500

    def grailsApplication

    private java.util.concurrent.ExecutorService asyncReceiverExecutor

    //-- Life Cycle --------------

    @javax.annotation.PreDestroy
    void destroy() {
        //Shutting down Async. Executor. 
        if (this.asyncReceiverExecutor) {
            try {
                def runnables = this.asyncReceiverExecutor.shutdownNow()
                if (runnables && runnables.size() > 0) {
                    LOG.warn "Async. Executor Shutting down with ${runnables.size()} pending tasks."
                }
            } catch (e) {
                LOG.error "Error while shutting down Async. Executor: $e.message."
            }
        }
    }

    //-- Receivers ---------------

    def receiveSelected(destination, selector, String jmsTemplateBeanName) {
        receiveSelected(destination, selector, null, jmsTemplateBeanName)
    }
    /**
     *
     * @param destination
     * @param messageSelector
     * @param timeout
     * @param jmsTemplateBeanName
     * @return
     */
    def receiveSelected(destination,
                        selector,
                        Long timeout = null,
                        String jmsTemplateBeanName = null) {

        if (this.disabled) {
            LOG.warn "will not receiving over [$destination] because JMS is disabled in config"
            return
        }

        final def ctx = normalizeServiceCtx(destination, jmsTemplateBeanName)

        logAction "Awaiting for JMS message with selector '$selector' from ", ctx

        def msg = null
        ctx.with {
            jmsTemplate.receiveTimeout = calculatedReceiverTimeout(timeout, jmsTemplate)
            JmsService.LOG.debug "JMS Template receiver timeout set to ${jmsTemplate.receiveTimeout}"

            logAction "Receivng JMS message with selector '$selector' from ", ctx
            msg = jmsTemplate.receiveSelectedAndConvert(ndestination, selector)

            JmsService.LOG.debug "Received JMS message with selector '$selector': $msg"

        }

        return msg

    }

    /**
     * Calculates the Receiver Timeout according to the following precedence.
     * <ol>
     *  <li>callReceiveTimeout: Selected if the value directly sent as argument during the invocation of a receive* method is not null.</li>
     *  <li>jmsTemplate: Selected if the value of the             {@code template.receiverTimeout}             is different from             {@link JmsTemplate#RECEIVE_TIMEOUT_INDEFINITE_WAIT}            (or zero).</li>
     *  <li>configReceiveTimeout: Selected if the value provided by            {@code config.jms.receiveTimeout}            is not null
     *      and different than            {@link JmsTemplate#RECEIVE_TIMEOUT_INDEFINITE_WAIT}           .</li>
     *  <li>defaultValue: Assigned if none of the above.</li>
     * </ol>
     * @param jmsTeplate
     * @param callReceiveTmeout
     * @param configReceiveTmeout
     * @return
     */
    private long calculatedReceiverTimeout(callReceiveTimeout, jmsTemplate) {

        if (callReceiveTimeout != null)
            return callReceiveTimeout

        if (jmsTemplate.receiveTimeout != JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT)
            return jmsTemplate.receiveTimeout

        def configReceiveTimeout = grailsApplication.config?.jms?.receiveTimeout
        if (configReceiveTimeout != null && configReceiveTimeout instanceof Number)
            return configReceiveTimeout


        return DEFAULT_RECEIVER_TIMEOUT_MILLIS
    }

    /**
     * Provides the executor that handles Async. Receiving requests. By default it will use a          {@code Cached Thread Pool}
     * as provided by         {@link java.util.concurrent.Executors#newCachedThreadPool()}        , but if through configuration a number
     * of <i>Async. Receiver Threads</i> is specified through         {@code config.jms.asyncReceiverThreads}         a thread limit
     * will be imposed through a         {@code Fixed Thread Pool}         where the given number is the limit.
     * @return
     */
    private getAsyncReceiverExecutor() {

        if (!this.asyncReceiverExecutor) {
            if (grailsApplication.config?.jms?.asyncReceiverThreads) {
                LOG.info "Establishing a Fixed Thread Pool for Async Selected Receivers with size : ${grailsApplication.config?.jms?.asyncReceiverThreads}."
                this.asyncReceiverExecutor = java.util.concurrent.Executors.newFixedThreadPool(
                        Integer.valueOf(grailsApplication.config?.jms?.asyncReceiverThreads))
            } else {
                LOG.debug "Establishing a Cached Thread Pool for Async Selected Receivers."
                this.asyncReceiverExecutor = java.util.concurrent.Executors.newCachedThreadPool()
            }
        }
        return this.asyncReceiverExecutor
    }

    java.util.concurrent.Future receiveSelectedAsync(destination, messageSelector, String jmsTemplateBeanName) {
        receiveSelectedAsync(destination, messageSelector, null, postProcessor)
    }
    /**
     *
     * @param destination
     * @param messageSelector
     * @param timeout
     * @param postProcessor
     * @return
     */
    java.util.concurrent.Future receiveSelectedAsync(destination, selector, Long timeout = null, String jmsTemplateBeanName = null) {
        if (this.disabled) {
            LOG.warn "will not receive from [$destination] with selector [$selector] because JMS is disabled in config"
            return
        }
        LOG.debug "Submitting Async Selected Receiver for [$destination] with selector [$selector].."
        return this.getAsyncReceiverExecutor().submit(
                { receiveSelected(destination, selector, timeout) } as java.util.concurrent.Callable
        )
    }

    //-- Senders ---------------

    /**
     *
     * @param destination
     * @param message
     * @param postProcessor
     * @return
     */
    def send(destination, message, Closure postProcessor) {
        send(destination, message, null, postProcessor)
    }

    /**
     *
     * @param destination
     * @param message
     * @param jmsTemplateBeanName
     * @param postProcessor
     * @return
     */
    def send(destination, message, String jmsTemplateBeanName = null, Closure postProcessor = null) {
        if (this.disabled) {
            LOG.warn "not sending message [$message] to [$destination] because JMS is disabled in config"
            return
        }
        def ctx = normalizeServiceCtx(destination, jmsTemplateBeanName)
        logAction "Sending JMS message '$message' to ", ctx

        ctx.with {
            if (postProcessor) {
                jmsTemplate.convertAndSend(
                        ndestination,
                        message,
                        new GrailsMessagePostProcessor(
                                jmsService: this,
                                jmsTemplate: jmsTemplate,
                                processor: postProcessor))
            } else {
                jmsTemplate.convertAndSend(ndestination, message)
            }
        }

    }

    boolean isDisabled() {
        return grailsApplication.config.jms.disabled
    }

    /**
     * Normalizes the context for sending or receiving a message.
     * <ul>
     * <li><b>jmsTemplate</b>           : org.springframework.jms.core.JmsTemplate instance.
     * <li><b>ndestination</b>          : Normalized Destination
     * <li><b>type</b>                  : Type of Destination, either ['topic'|'queue']
     * <li><b>jmsTemplateBeanName</b>   : Name of the bean used to retrieve the JmsTemplate.
     * <li><b>defaultTemplate</b>       : Boolean value that tells us if the JmsTemplate is the Default Template.
     * </ul>
     * @param destination
     * @param jmsTemplateBeanName
     * @return
     */
    private def normalizeServiceCtx(def destination, final String jmsTemplateBeanName) {

        final String _jmsTemplateBeanName = "${ jmsTemplateBeanName ?: DEFAULT_JMS_TEMPLATE_BEAN_NAME }JmsTemplate"
        boolean defaultTemplate = _jmsTemplateBeanName == "${DEFAULT_JMS_TEMPLATE_BEAN_NAME}JmsTemplate"


        def jmsTemplate = grailsApplication.mainContext.getBean(_jmsTemplateBeanName)
        if (jmsTemplate == null) {
            throw new Error("Could not find bean with name '${_jmsTemplateBeanName}' to use as a JmsTemplate")
        }

        def isTopic
        if (destination instanceof javax.jms.Destination) {
            isTopic = destination instanceof javax.jms.Topic
        } else {
            def destinationMap = convertToDestinationMap(destination)
            isTopic = destinationMap.containsKey("topic")
            jmsTemplate.pubSubDomain = isTopic
            destination = (isTopic) ? destinationMap.topic : destinationMap.queue
        }

        [
                jmsTemplate: jmsTemplate                    /** org.springframework.jms.core.JmsTemplate  */,
                ndestination: destination                   /** Normalized Destination  */,
                type: isTopic ? 'topic' : 'queue'           /** Type of Destination [topic|queue]   */,
                jmsTemplateBeanName: _jmsTemplateBeanName   /** Name of the bean used to retrieve the JmsTemplate.  */,
                defaultTemplate: defaultTemplate            /** Boolean value that tells us if the JmsTemplate is the Default Template.   */
        ]
    }

    /**
     * Single point of entry to log an action.
     * @param action Description of the Action.
     * @param ctx Context of the action as provided by the         {@link JmsService#normalizeServiceCtx(Object, String)}         method.
     */
    private void logAction(final String action, final def ctx) {
        if (LOG.infoEnabled) {
            def logMsg = ''
            ctx.with {
                logMsg = "$action $type '$ndestination'"
                if (!defaultTemplate) {
                    logMsg += " using template '$jmsTemplateBeanName'"
                }
            }
            LOG.info(logMsg)
        }
    }

    /**
     *
     * @param destination
     * @return
     */
    def convertToDestinationMap(destination) {

        if (destination == null) {
            [queue: null]
        } else if (destination instanceof String) {
            [queue: destination]
        } else if (destination instanceof Map) {
            if (destination.queue) {
                [queue: destination.queue]
            } else if (destination.topic) {
                [topic: destination.topic]
            } else {
                def parts = []
                if (destination.app) {
                    parts << destination.app
                } else {
                    parts << grailsApplication.metadata['app.name']
                }
                if (destination.service) {
                    parts << destination.service
                    if (destination.method) {
                        parts << destination.method
                    }
                }
                [queue: (parts) ? parts.join('.') : null]
            }
        } else {
            [queue: destination.toString()]
        }
    }
}