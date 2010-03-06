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