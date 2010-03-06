package grails.plugin.jms.test.simple

import grails.plugin.spock.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SimpleSendingAndReceivingSpec extends IntegrationSpec {

    def simpleSendingService
    def simpleReceivingService
    
    void testQueue() {
        given:
        def latch = new CountDownLatch(1)
        def msg
        Thread.start {
            msg = simpleReceivingService.queueReceived.take()
            latch.countDown()
        }
        when:
        simpleSendingService.sendToQueue("a")
        latch.await(5, TimeUnit.SECONDS)
        then:
        msg == "a"
    }

    void testSubscriber() {
        given:
        def latch = new CountDownLatch(1)
        def msg
        Thread.start {
            msg = simpleReceivingService.subscriberReceived.take()
            latch.countDown()
        }
        when:
        simpleSendingService.sendToTopic("a")
        latch.await(5, TimeUnit.SECONDS)
        then:
        msg == "a"
    }

}