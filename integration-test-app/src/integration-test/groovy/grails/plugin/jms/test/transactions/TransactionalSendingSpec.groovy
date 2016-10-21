package grails.plugin.jms.test.transactions

import grails.plugin.jms.test.Person
import grails.test.mixin.integration.Integration
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.CountDownLatch

@Integration
class TransactionalSendingSpec extends Specification {

    static transactional = false

    def simpleSendingService
    def simpleReceivingService

    static final String PAYLOAD = "payload"

    @Timeout(10)
    void "A message sent in a successful transaction to a non-transacted template is sent straightaway"() {
        given: "a thread sending a messaging in a transaction, but waiting to close the transaction"
        def latch = new CountDownLatch(1)


        Thread.start {
            Person.withTransaction {
                simpleSendingService.sendToQueue(PAYLOAD) // default template is not transacted
                latch.await()
            }
        }

        expect: "the message is sent and received (even though the transaction is still open)"
        simpleReceivingService.getMessage(2) == PAYLOAD

        cleanup:
        latch.countDown()
    }

    @Timeout(10)
    void "A message sent in a successful transaction should be received by receiver."() {
        given: "a sender that wraps the send in a successful transaction"
        def latch = new CountDownLatch(1)
        def payload = "payload"

        Thread.start {
            Person.withTransaction {
                simpleSendingService.sendToQueue(PAYLOAD, 'transacted')
                latch.await()
            }
        }

        expect: "the receiver to get the message only after the transaction has been committed."
        simpleReceivingService.getMessage(2) == null

        when: "the transaction is allowed to commit"
        latch.countDown()

        then: "the message is sent to the receiver since the transaction was successful"
        simpleReceivingService.getMessage(2) == PAYLOAD
    }

    @Timeout(10)
    void "A message sent in an unsuccessful transaction shouldn't be received"() {

        given: "a sender that wraps the send in a failed transaction"
        def latch = new CountDownLatch(1)
        def payload = "payload"

        Thread.start {
            try {
                Person.withTransaction {
                    simpleSendingService.sendToQueue(PAYLOAD, 'transacted')
                    latch.await()
                    throw new RuntimeException("Error that we intentionally throw to fail the transaction.")
                }
            } catch (e) {
            }
        }

        expect: "that the receiver shouldn't get the message"
        simpleReceivingService.getMessage(2) == null

        when: "the transaction is allowed to fail and rollback"
        latch.countDown()

        then: "the message is not sent to the receiver since the transaction failed"
        simpleReceivingService.getMessage(2) == null
    }

}
