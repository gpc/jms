package grails.plugin.jms.test.transactions

import grails.plugin.jms.test.*
import grails.plugin.spock.*
import java.util.concurrent.CountDownLatch
import spock.lang.*

@Ignore("This is not yet working, may need to create a listener container subclass")
class TransactionalReceivingSpec extends IntegrationSpec {

    static transactional = false
    
    def simpleSendingService
    def simpleReceivingService
    def latch = new CountDownLatch(1)   
    
    static PAYLOAD = "payload"

    def setup() {
        Person.list()*.delete(flush: true)
        assert Person.count() == 0
    }
    
    @Timeout(10)
    void "non transactional listeners do not open database transactions"() {
        given: "a non transactional message listener that creates a domain object, but waits before finishing "
        simpleReceivingService.callback = {
            new Person(name: "a").save(flush: true)
            latch.await()
        }

        when: "we send it a message"
        simpleSendingService.sendToQueue(PAYLOAD)
        
        then: "it should have written to the database"
        waitFor { Person.count() == 1 }

        cleanup:
        latch.countDown()
    }

    @Timeout(10)
    void "transactional listeners open database transactions"() {
        given: "a transactional message listener that creates a domain object, but waits before finishing"
        def received = false
        simpleReceivingService.callback = {
            new Person(name: "a").save()
            received = true
            latch.await()
        }

        when: "we send it a message"
        simpleSendingService.sendToTransactionalQueue(PAYLOAD)
        
        then: "the domain object is not yet written to the database"
        waitFor(1) { received }
        Person.count() == 0

        when: "we allow the listener to finish"
        latch.countDown()
        
        then: "the domain object is written to the database"
        waitFor { Person.count() == 1 }
        
        cleanup:
        if (latch.count != 0) latch.countDown()
    }
    
    def waitFor(condition) {
        waitFor(null, condition)
    }
    
    def waitFor(timeoutSecs, condition) {
        waitFor(timeoutSecs, null, condition)
    }
    
    def waitFor(timeoutSecs, intervalSecs, condition) {
        timeoutSecs = timeoutSecs ?: 5
        intervalSecs = intervalSecs ?: 0.5
        def loops = Math.ceil(timeoutSecs / intervalSecs)
        def pass = condition()
        def i = 0
        while (pass == false && i++ < loops) {
            Thread.sleep((intervalSecs * 1000) as long)
            pass = condition()
        }
        
        i < loops
    }
    
   def cleanup() {
       simpleReceivingService.callback = null
   }

}