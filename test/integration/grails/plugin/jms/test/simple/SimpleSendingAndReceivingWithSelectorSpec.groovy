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
        given:
        def receiver = execAsync { simpleReceivingSelectedService."receiveSelectedFrom${destination}"("aproperty='$propertyValueToMatch'", TIMEOUT) }
        
        when:
        "sendTo${destination}"(3, null) // no value for property
        "sendTo${destination}"(2, propertyValueNotToMatch) // doesn't match
        "sendTo${destination}"(1, propertyValueToMatch) // matches
        
        and:
        receiver.get() // wait for messages to be received

        then:
        simpleReceivingSelectedService.message == 1
        simpleReceivingSelectedService.message == null // only 1 message received
        
        where:
        destination << ["Topic", "Queue"]
    }

    @Unroll("only messages matching selector are returned from #destination asynchronously")
    def "async"() {
        given:
        def barrier = new CyclicBarrier(1)
        
        and:
        def receiver = execAsync { simpleReceivingSelectedService."receiveSelectedAsyncFrom${destination}"(barrier, "aproperty='$propertyValueToMatch'", TIMEOUT) }
        
        when:
        barrier.await() // what for receiving thread to obtain future
        
        and:
        "sendTo${destination}"(3, null) // no value for property
        "sendTo${destination}"(2, propertyValueNotToMatch) // doesn't match
        "sendTo${destination}"(1, propertyValueToMatch) // matches
        
        and:
        receiver.get() // wait for messages to be received

        then:
        simpleReceivingSelectedService.message == 1
        simpleReceivingSelectedService.message == null // only 1 message received
        
        where:
        destination << ["Topic", "Queue"]
    }

}