package grails.plugin.jms.test.simple

import spock.lang.*
import grails.plugin.spock.*

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_QUEUE
import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_TOPIC

class SimpleSendingAndReceivingWithSelectorSpec extends IntegrationSpec {

    static final long TIMEOUT = 1000l

    def simpleReceivingSelectedService
    def simpleSendingService
    
    @AutoCleanup("shutdown")
    def executor = Executors.newCachedThreadPool()
    
    void "Queue Receivers with Selector"() {
        when:
        def tester = { qualifier, m ->
            Future receiver = executor.submit({
                simpleReceivingSelectedService.receiveSelectedFromQueue "aproperty='$qualifier'", TIMEOUT
            } as Callable)

            //Starting senders ...
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, 'nosie')
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, m, null) { javax.jms.Message msg ->
                msg.setStringProperty 'aproperty', qualifier
                msg
            }
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, 'noise')
            receiver.get()
        }

        tester 'A', 'a'
        tester 'B', 'b'

        then:
        simpleReceivingSelectedService.message == 'a'
        simpleReceivingSelectedService.message == 'b'
    }

    void "Suscriber with Selector"() {
        when:
        def tester = { qualifier, m ->
            Future receiver = executor.submit({
                simpleReceivingSelectedService.receiveSelectedFromTopic "aproperty='$qualifier'", TIMEOUT
            } as Callable)

            //Starting senders ... 
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, 'nosie')
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, m, null) { javax.jms.Message msg ->
                msg.setStringProperty 'aproperty', qualifier
                msg
            }
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, 'noise')
            receiver.get()
        }

        tester 'A', 'a'
        tester 'B', 'b'

        then:
        simpleReceivingSelectedService.message == 'a'
        simpleReceivingSelectedService.message == 'b'
    }

    void "Queue Receivers with Async Selector"() {
        when:
        def tester = { qualifier, m ->

            def barrier = new java.util.concurrent.CyclicBarrier(2)

            Future receiver = executor.submit({
                simpleReceivingSelectedService.receiveSelectedAsyncFromQueue barrier, "aproperty='$qualifier'", TIMEOUT
            } as Callable)
            //Wait for receivers.
            barrier.await()
            //Starting senders ...
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, 'nosie')
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, m, null) { javax.jms.Message msg ->
                msg.setStringProperty 'aproperty', qualifier
                msg
            }
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, 'noise')



            receiver.get()
        }

        tester 'Aasync', 'a async'
        tester 'Basync', 'b async'

        then:
        simpleReceivingSelectedService.message == 'a async'
        simpleReceivingSelectedService.message == 'b async'
    }

    void "Suscriber with Async Selector"() {
        when:
        def tester = { qualifier, m ->

            def barrier = new java.util.concurrent.CyclicBarrier(2)

            Future receiver = executor.submit({
                simpleReceivingSelectedService.receiveSelectedAsyncFromTopic barrier, "aproperty='$qualifier'", TIMEOUT
            } as Callable)
            //Wait for receivers.
            barrier.await()
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, 'nosie')
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, m, null) { javax.jms.Message msg ->
                msg.setStringProperty 'aproperty', qualifier
                msg
            }
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, 'noise')

            receiver.get()
        }

        tester 'Aasync', 'a async'
        tester 'Basync', 'b async'

        then:
        simpleReceivingSelectedService.message == 'a async'
        simpleReceivingSelectedService.message == 'b async'
    }
}