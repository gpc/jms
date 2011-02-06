package grails.plugin.jms.test.simple

import spock.lang.*
import grails.plugin.spock.*

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.CyclicBarrier

import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_QUEUE
import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_TOPIC

class SimpleSendingAndReceivingWithSelectorSpec extends IntegrationSpec {

    static final long TIMEOUT = 1000l

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
    def "sync"() {
        given: "a receiver on another thread"
        def receiver = execAsync { simpleReceivingSelectedService."receiveSelectedFrom${destination}"("aproperty='$propertyValueToMatch'", TIMEOUT) }
        
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
        def barrier = new CyclicBarrier(1)
        
        and: "an asynchronous receiver on another thread, who will wait on the barrier"
        def receiver = execAsync { simpleReceivingSelectedService."receiveSelectedAsyncFrom${destination}"(barrier, "aproperty='$propertyValueToMatch'", TIMEOUT) }
        
        when: "we wait for the async receive call to reach the barrier"
        barrier.await() // what for receiving thread to obtain future
        
        and: "we send some messages"
        "sendTo${destination}"(3, null) // no value for property
        "sendTo${destination}"(2, propertyValueNotToMatch) // doesn't match
        "sendTo${destination}"(1, propertyValueToMatch) // matches
        
        and: "we wait for them to be received"
        receiver.get() // wait for messages to be received

        then: "only the message matching the selector has been received"
        simpleReceivingSelectedService.message == 1
        simpleReceivingSelectedService.message == null // only 1 message received
        
        where:
        destination << ["Topic", "Queue"]
    }

}