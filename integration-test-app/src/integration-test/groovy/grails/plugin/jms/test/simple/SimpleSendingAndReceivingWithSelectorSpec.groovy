package grails.plugin.jms.test.simple

import grails.test.mixin.integration.Integration
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_QUEUE
import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_TOPIC

@Integration
class SimpleSendingAndReceivingWithSelectorSpec extends Specification {

    static final long TIMEOUT = 3000

    def simpleReceivingSelectedService
    def simpleSendingService

    def propertyValueToMatch = "a"
    def propertyValueNotToMatch = "b"

    @AutoCleanup("shutdown")
    def executor = Executors.newCachedThreadPool()

    Future execAsync(Closure task) {
        executor.submit(task as Callable)
    }

    void sendToQueue(message, propertyValue) {
        simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, message, null, createPropertySettingPostProcessor(propertyValue))
    }

    void sendToTopic(message, propertyValue) {
        simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, message, null, createPropertySettingPostProcessor(propertyValue))
    }

    Closure createPropertySettingPostProcessor(propertyValue) {
        return {
            if (propertyValue) {
                it.setStringProperty 'aproperty', propertyValue
            }
            it
        }
    }

    @Unroll("only messages matching selector are returned from #destination")
    @Ignore
    def "sync"() {
        given: "a receiver on another thread"
        def receiver = execAsync {
            simpleReceivingSelectedService."receiveSelectedFrom${destination}"("aproperty='$propertyValueToMatch'", TIMEOUT)
        }

        when: "we send some messages"
        "sendTo${destination}"(3, null)
        "sendTo${destination}"(2, propertyValueNotToMatch)
        "sendTo${destination}"(1, propertyValueToMatch)

        and: "we wait for them to be received"
        receiver.get()

        then: "only the message matching the selector has been received"
        simpleReceivingSelectedService.message == 1
        simpleReceivingSelectedService.message == null

        where:
        destination << ["Topic", "Queue"]
    }

    @Unroll("only messages matching selector are returned from #destination asynchronously")
    def "async"() {
        given: "a barrier"
        def barrier = new CyclicBarrier(2)

        and: "an asynchronous receiver on another thread, who will wait on the barrier"
        def receiver = execAsync {
            simpleReceivingSelectedService."receiveSelectedAsyncFrom${destination}"(barrier, "aproperty='$propertyValueToMatch'", TIMEOUT)
        }

        and: "an asyncrhounous sender that will wait on the barrier so it doesn't send without the receivers"
        def sender = execAsync {
            barrier.await()
            "sendTo${destination}"(3, null)
            "sendTo${destination}"(2, propertyValueNotToMatch)
            "sendTo${destination}"(1, propertyValueToMatch)
        }

        when: "we wait for the senders"
        sender.get()

        and: "we wait for the messages to be received"
        receiver.get()

        then: "only the message matching the selector has been received"
        simpleReceivingSelectedService.message == 1
        simpleReceivingSelectedService.message == null

        where:
        destination << ["Topic", "Queue"]
    }

}
