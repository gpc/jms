import grails.plugin.spock.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SimpleSendingSpec extends IntegrationSpec {

    def sendingService
    def receivingService
    
    void testQueue() {
        given:
        def latch = new CountDownLatch(1)
        def msg
        Thread.start {
            msg = receivingService.queueReceived.take()
            latch.countDown()
        }
        when:
        sendingService.sendToQueue("a")
        latch.await(5, TimeUnit.SECONDS)
        then:
        msg == "a"
    }

    void testSubscriber() {
        given:
        def latch = new CountDownLatch(1)
        def msg
        Thread.start {
            msg = receivingService.subscriberReceived.take()
            latch.countDown()
        }
        when:
        sendingService.sendToTopic("a")
        latch.await(5, TimeUnit.SECONDS)
        then:
        msg == "a"
    }

}