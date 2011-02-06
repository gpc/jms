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
    
    @AutoCleanup("shutdown")
    def executor = Executors.newCachedThreadPool()
    
    Future execAsync(Closure task) {
        executor.submit(task as Callable)
    }
    
    void "Queue Receivers with Selector"() {
        when:
        def tester = { qualifier, m ->
            def receiver = execAsync {
                simpleReceivingSelectedService.receiveSelectedFromQueue "aproperty='$qualifier'", TIMEOUT
            }

            //Starting senders ...
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, 'nosie')
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, m, null) {
                it.setStringProperty 'aproperty', qualifier
                it
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
            def receiver = execAsync {
                simpleReceivingSelectedService.receiveSelectedFromTopic "aproperty='$qualifier'", TIMEOUT
            }

            //Starting senders ... 
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, 'nosie')
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, m, null) {
                it.setStringProperty 'aproperty', qualifier
                it
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

            def barrier = new CyclicBarrier(2)

            def receiver = execAsync {
                simpleReceivingSelectedService.receiveSelectedAsyncFromQueue barrier, "aproperty='$qualifier'", TIMEOUT
            }
            
            //Wait for receivers.
            barrier.await()
            //Starting senders ...
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, 'nosie')
            simpleSendingService.sendToGivenQueue(RECEIVING_QUEUE, m, null) {
                it.setStringProperty 'aproperty', qualifier
                it
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

            def barrier = new CyclicBarrier(2)

            def receiver = execAsync {
                simpleReceivingSelectedService.receiveSelectedAsyncFromTopic barrier, "aproperty='$qualifier'", TIMEOUT
            }
            
            //Wait for receivers.
            barrier.await()
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, 'nosie')
            simpleSendingService.sendToGivenTopic(RECEIVING_TOPIC, m, null) {
                it.setStringProperty 'aproperty', qualifier
                it
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