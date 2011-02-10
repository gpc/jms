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
import javax.jms.Session
import javax.jms.QueueBrowser
import javax.jms.Message
import javax.jms.JMSException

import grails.plugin.jms.listener.GrailsMessagePostProcessor
import org.apache.commons.logging.LogFactory

import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.BrowserCallback
import org.springframework.jms.core.MessagePostProcessor
import org.springframework.jms.support.JmsUtils

import javax.annotation.PreDestroy

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * @todo Enable TTL for the Template.
 */
class JmsService {

    static transactional = false

    private static final LOG = LogFactory.getLog(JmsService)

    public static final DEFAULT_JMS_TEMPLATE_BEAN_NAME = "standard"
    public static final long DEFAULT_RECEIVER_TIMEOUT_MILLIS = 500l

    def grailsApplication

    ExecutorService asyncReceiverExecutor
    boolean asyncReceiverExecutorShutdown = true
    private Lock asyncReceiverExecutorCreateLock = new ReentrantLock()

    //-- Life Cycle --------------

    @PreDestroy
    void destroy() {
        doWithinAsyncLock {
            if (this.@asyncReceiverExecutor) {
                if (asyncReceiverExecutorShutdown) {
                    shutdownAsyncReceiverExecutorNow()
                } else {
                    LOG.info "The flag to shutdown the Async. Executor is turned off. The executor will not be terminated"
                }
            }
        }
    }

    private shutdownAsyncReceiverExecutorNow() {
        LOG.info "Shutting down current Async. Executor..."
        try {
            def runnables = this.asyncReceiverExecutor.shutdownNow()
            if (runnables && runnables.size() > 0) {
                LOG.warn "Async. Executor Shutting down with ${runnables.size()} pending tasks."
            }
        } catch (e) {
            LOG.error "Error while shutting down Async. Executor: $e.message."
        }
    }

    //-- Receivers ---------------

    def receiveSelected(destination, selector, String jmsTemplateBeanName) {
        receiveSelected(destination, selector, null, jmsTemplateBeanName)
    }

    /**
     * <blockquote>
     * Receive and converts a message synchronously from the default destination, but only wait up to a specified time for delivery.
     * This method should be used carefully, since it will block the thread until the message becomes available or until the timeout value is exceeded.
     * </blockquote>
     * <b>description copied from interface <i>org.springframework.jms.core.JmsOperations</i></b>.
     *
     * The big difference between the {@code receiveSelected} methods provided by the <i>org.springframework.jms.core.JmsOperations</i> is that we to a
     * message conversion and we try to enforce a <b>timeout</b>. Such timeout is defined by the following rules
     * described in the method. {@code JmsService # calculatedReceiverTimeout}.
     *
     * <ol>
     *  <li><i>argument</i> <b>timeout</b>: Selected if the value directly sent as argument is not null.</li>
     *  <li><i>jmsTemplate.receiverTimeout: Selected if the value of the {@code template.receiverTimeout} is different
     * from {@link JmsTemplate#RECEIVE_TIMEOUT_INDEFINITE_WAIT} (or zero).</li>
     *  <li>If the value provided by  {@code config.jms.receiveTimeout} is not null and different
     * from {@link JmsTemplate#RECEIVE_TIMEOUT_INDEFINITE_WAIT}.</li>
     *  <li>A default value of {@link #DEFAULT_RECEIVER_TIMEOUT_MILLIS} is used if none of the above are selected.</li>
     * </ol>
     */
    def receiveSelected(destination, selector, Long timeout = null, String jmsTemplateBeanName = null) {
        if (this.disabled) {
            LOG.warn "will not receive over [$destination] because JMS is disabled in config"
            return
        }

        final ctx = normalizeServiceCtx(destination, jmsTemplateBeanName)

        logAction "Awaiting for JMS message with selector '$selector' from ", ctx

        ctx.with {
            jmsTemplate.receiveTimeout = calculatedReceiverTimeout(timeout, jmsTemplate)
            JmsService.LOG.debug "JMS Template receiver timeout set to ${jmsTemplate.receiveTimeout}"

            logAction "Receivng JMS message with selector '$selector' from ", ctx
            def msg = jmsTemplate.receiveSelectedAndConvert(ndestination, selector)

            JmsService.LOG.debug "Received JMS message with selector '$selector': $msg"
            msg
        }
    }

    Future receiveSelectedAsync(destination, selector, String jmsTemplateBeanName) {
        receiveSelectedAsync(destination, selector, null, postProcessor)
    }

    /**
     * Submits a {@code receiveSelected} call through an {@link java.util.concurrent.Executor} and returns a future
     * that reflects the execution of the task. The {@code Executor} is provided by {@code JmsService.getJmsAsyncReceiverExecutor ( )}.
     */
    Future receiveSelectedAsync(destination, selector, Long timeout = null, String jmsTemplateBeanName = null) {
        if (this.disabled) {
            LOG.warn "will not receive from [$destination] with selector [$selector] because JMS is disabled in config"
            return
        }

        LOG.debug "Submitting Async Selected Receiver for [$destination] with selector [$selector].."
        this.getAsyncReceiverExecutor().submit({ receiveSelected(destination, selector, timeout) } as Callable)
    }

    //-- Senders ---------------

    def send(destination, message, Closure callback) {
        send(destination, message, null, callback)
    }

    def send(destination, message, String jmsTemplateBeanName = null, Closure callback = null) {
        if (this.disabled) {
            LOG.warn "not sending message [$message] to [$destination] because JMS is disabled in config"
            return
        }

        def ctx = normalizeServiceCtx(destination, jmsTemplateBeanName)
        logAction "Sending JMS message '$message' to ", ctx

        ctx.with {
            if (callback) {
                jmsTemplate.convertAndSend(ndestination, message, toMessagePostProcessor(jmsTemplate, callback))
            } else {
                jmsTemplate.convertAndSend(ndestination, message)
            }
        }

    }

    protected MessagePostProcessor toMessagePostProcessor(JmsTemplate template, Closure callback) {
        new GrailsMessagePostProcessor(jmsService: this, jmsTemplate: template, processor: callback)
    }

    //-- Browsers ---------------
    /**
     * Returns a <i>list</i> with the <i>messages</i> inside the given <b>queue</b>.
     */
    def browseNoConvert(queue, String jmsTemplateBeanName = null, Closure browserCallback = null) {
        doBrowseSelected(queue, null, jmsTemplateBeanName, false, browserCallback)
    }

    def browseNoConvert(queue, Closure browserCallback) {
        doBrowseSelected(queue, null, null, false, browserCallback)
    }

    /**
     * Returns a <i>list</i> with the <i>messages</i> inside the given <b>queue</b>.
     * The list will contain <i>javax.jms.Message</i> instances since no conversion will be attempted.
     */
    def browse(queue, String jmsTemplateBeanName = null, Closure browserCallback = null) {
        doBrowseSelected(queue, null, jmsTemplateBeanName, true, browserCallback)
    }

    def browse(queue, Closure browserCallback) {
        doBrowseSelected(queue, null, null, true, browserCallback)
    }

    /**
     * Returns a <i>list</i> with the <i>messages</i> inside the given <b>queue</b> that match the given <b>selector</b>.
     * Messages will be converted using the <i>Jms Template</i> before being added to the list.
     */
    def browseSelected(queue, selector, String jmsTemplateBeanName = null, Closure browserCallback = null) {
        doBrowseSelected(queue, selector, jmsTemplateBeanName, true, browserCallback)
    }

    def browseSelected(queue, selector, Closure browserCallback) {
        doBrowseSelected(queue, selector, null, true, browserCallback)
    }

    /**
     * Returns a <i>list</i> with the <i>messages</i> inside the given <b>queue</b> that match the given <b>selector</b>.
     * The list will contain <i>javax.jms.Message</i> instances since no conversion will be attempted.
     */
    def browseSelectedNotConvert(queue, selector, String jmsTemplateBeanName = null, Closure browserCallback = null) {
        doBrowseSelected(queue, selector, jmsTemplateBeanName, false, browserCallback)
    }

    def browseSelectedNotConvert(queue, selector, Closure browserCallback) {
        doBrowseSelected(queue, selector, null, false, browserCallback)
    }

    /**
     * Delegate to all browse actions. It leverages the {@code org.springframework.jms.core.JmsTemplate.browseSelected} method.
     * This method accepts a <i>JMS selector</i> to filter the messages. You can also define a
     * <i>browserCallback</i> closure which will receive all messages, if defined its return value will be the one
     * added to the <i>list</i> of messages. If no <i>browserCallback</i> closure is specified the <i>list</i>
     * will contain the <i>messages</i> inside the given <b>queue</b> filtered only by the <i>JMS selector</i> if available.
     *
     * By default it will try to convert the messages according to the given <i>JmsTemplate</i>.
     *
     * <b>Note:</b>This method will throw an {@code IllegalArgumentException} if the <i>destination</i> is not a <b>queue</b>.
     * @param queue
     * @param selector
     * @param jmsTemplateBeanName
     * @param convert {@code true} if you want to convert the {@code javax.jms.Message}; {@code false} to receive the raw {@code javax.jms.Message}
     * @param browserCallback Closure that gets executed per message. If specified its return value will be the one added to the <i>list</i> that this method returns.
     */
    private doBrowseSelected(queue, selector, String jmsTemplateBeanName = null, boolean convert = true, Closure browserCallback = null) {
        if (this.disabled) {
            if (selector) {
                LOG.warn "not browsing [$queue] with selector [$selector] because JMS is disabled in config"
            } else {
                LOG.warn "not browsing [$queue] because JMS is disabled in config"
            }
            return
        }

        def ctx = normalizeServiceCtx(queue, jmsTemplateBeanName)
        if (ctx.type != 'queue') {
            new IllegalArgumentException("The destination [$queue] must be a queue.")
        }

        if (selector) {
            logAction "Browsing messages with selector [$selector] ", ctx
        } else {
            logAction "Browsing messages ", ctx
        }

        final messages = [] as ArrayList

        ctx.with {
            def callback = { Session session, QueueBrowser browser ->
                for (Message m in browser.enumeration) {
                    def processedMessage = convert ? convertMessageWithTemplate(jmsTemplate, m) : m
                    if (browserCallback) {
                        def val = browserCallback.call(processedMessage)
                        if (val != null) {
                            messages << val
                        }
                    } else {
                        messages << (processedMessage)
                    }
                }
            } as BrowserCallback

            jmsTemplate.browseSelected(ndestination, selector, callback)
        }

        messages
    }

    //-- Util ---------------

    boolean isDisabled() {
        grailsApplication.config.jms.disabled
    }

    private convertMessageWithTemplate(template, Message message) {
        if (message) {
            def converter = template?.messageConverter
            try {
                converter?.fromMessage(message)
            } catch (JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex)
            }
        }
    }

    /**
     * Calculates the Receiver Timeout according to the following precedence.
     * <ol>
     *  <li><i>argument</i> <b>callReceiveTimeout</b>: Selected if the value directly sent as argument is not null.</li>
     *  <li><i>jmsTemplate.receiverTimeout: Selected if the value of the {@code template.receiverTimeout} is different
     * from {@code JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT} (or zero).</li>
     *  <li>Thre Grails Configuration mechanism provides a <b>jms.receiveTimeout</b> which value is not null and different
     * from {@code JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT} (or zero) .</li>
     *  <li>A default value of {@link JmsService#DEFAULT_RECEIVER_TIMEOUT_MILLIS} is used if none of the above are selected.</li>
     * </ol>
     */
    long calculatedReceiverTimeout(callReceiveTimeout, jmsTemplate) {
        if (callReceiveTimeout != null) {
            return callReceiveTimeout
        }

        if (jmsTemplate.receiveTimeout != JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT) {
            return jmsTemplate.receiveTimeout
        }

        def configReceiveTimeout = grailsApplication.config?.jms?.receiveTimeout
        if (configReceiveTimeout != null
                && configReceiveTimeout instanceof Number
                && configReceiveTimeout != JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT) {
            return configReceiveTimeout
        }

        DEFAULT_RECEIVER_TIMEOUT_MILLIS
    }

    private doWithinAsyncLock(Closure closure) {
        if (asyncReceiverExecutorCreateLock.tryLock(500, TimeUnit.MILLISECONDS)) {
            try {
                closure.call()
            } finally {
                asyncReceiverExecutorCreateLock.unlock()
            }
        }
    }

    /**
     * Setter for the <i>executor</i> that handles Async. Receiving requests.
     * <b>Warning</b> this method will shutdown any previous <i>executor</i> regardless of the {@code asyncReceiverExecutorShutdown} flag.
     * The {@code asyncReceiverExecutor} will be internally initialized if no <i>executor</i> is set but an <b>async. receiver</b> is
     * requested.
     */
    void setAsyncReceiverExecutor(ExecutorService executorService) {
        LOG.debug "attempting to set asyncReceiverExecutor $executorService ..."
        doWithinAsyncLock {
            if (this.@asyncReceiverExecutor && !this.@asyncReceiverExecutor.shutdown) {
                shutdownAsyncReceiverExecutorNow()
            }
            this.@asyncReceiverExecutor = executorService
        }
    }

    /**
     * Provides the executor that handles Async. Receiving requests. If no {@code asyncReceiverExecutor} is specified
     * a {@code Cached Thread Pool}, as provided by {@link java.util.concurrent.Executors#newCachedThreadPool()},
     * will be used.
     */
    ExecutorService getAsyncReceiverExecutor() {
        if (!this.asyncReceiverExecutor) {
            doWithinAsyncLock {
                if (this.@asyncReceiverExecutor == null) {
                    LOG.debug "default to a Cached Thread Pool for Async Selected Receivers."
                    this.@asyncReceiverExecutor = Executors.newCachedThreadPool()
                }
            }
        }
        this.asyncReceiverExecutor
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
     */
    private normalizeServiceCtx(destination, final String jmsTemplateBeanName) {
        final String _jmsTemplateBeanName = "${ jmsTemplateBeanName ?: DEFAULT_JMS_TEMPLATE_BEAN_NAME }JmsTemplate"
        boolean defaultTemplate = _jmsTemplateBeanName == "${DEFAULT_JMS_TEMPLATE_BEAN_NAME}JmsTemplate"

        def jmsTemplate = grailsApplication.mainContext.getBean(_jmsTemplateBeanName)
        if (jmsTemplate == null) {
            throw new Error("Could not find bean with name '${_jmsTemplateBeanName}' to use as a JmsTemplate")
        }

        def isTopic
        if (destination instanceof Destination) {
            isTopic = destination instanceof Topic
        } else {
            def destinationMap = convertToDestinationMap(destination)
            isTopic = destinationMap.containsKey("topic")
            jmsTemplate.pubSubDomain = isTopic
            destination = (isTopic) ? destinationMap.topic : destinationMap.queue
        }

        [
                jmsTemplate: jmsTemplate,                    // org.springframework.jms.core.JmsTemplate
                ndestination: destination,                   // Normalized Destination
                type: (isTopic ? 'topic' : 'queue'),         // Type of Destination [topic|queue]
                jmsTemplateBeanName: _jmsTemplateBeanName,   // Name of the bean used to retrieve the JmsTemplate.
                defaultTemplate: defaultTemplate             // Boolean value that tells us if the JmsTemplate is the Default Template.
        ]
    }

    /**
     * Single point of entry to log an action.
     * @param action Description of the Action.
     * @param ctx Context of the action as provided by the {@link JmsService#normalizeServiceCtx(Object, String)} method.
     */
    private void logAction(final String action, final ctx) {
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