package grails.plugin.jms.test.simple

import grails.plugin.spock.IntegrationSpec
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_QUEUE
import static grails.plugin.jms.test.simple.SimpleReceivingSelectedService.RECEIVING_TOPIC
import java.util.concurrent.Executor

class SimpleSendingAndReceivingWithSelectorSpec extends IntegrationSpec {

    static final long TIMEOUT = 1000l

    def simpleReceivingSelectedService
    def simpleSendingService

    void "Queue Receivers with Selector"() {
        when:

        Executor executor = Executors.newCachedThreadPool()

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

        executor.shutdown()

        then:
        simpleReceivingSelectedService.message == 'a'
        simpleReceivingSelectedService.message == 'b'
    }

    void "Suscriber with Selector"() {
        when:
        Executor executor = Executors.newCachedThreadPool()

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


        executor.shutdown()

        then:
        simpleReceivingSelectedService.message == 'a'
        simpleReceivingSelectedService.message == 'b'
    }

    void "Queue Receivers with Async Selector"() {
        when:

        Executor executor = Executors.newCachedThreadPool()


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

        executor.shutdown()

        then:
        simpleReceivingSelectedService.message == 'a async'
        simpleReceivingSelectedService.message == 'b async'
    }


    void "Suscriber with Async Selector"() {
        when:
        Executor executor = Executors.newCachedThreadPool()

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


        executor.shutdown()

        then:
        simpleReceivingSelectedService.message == 'a async'
        simpleReceivingSelectedService.message == 'b async'
    }
}