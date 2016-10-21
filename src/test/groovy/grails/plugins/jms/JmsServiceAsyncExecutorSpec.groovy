package grails.plugins.jms

import grails.plugins.jms.JmsService

import java.util.concurrent.Executors

import spock.lang.*

class JmsServiceAsyncExecutorSpec extends Specification {

    def jmsService = new JmsService()

    @AutoCleanup("shutdown")
    def executor = Executors.newCachedThreadPool()

    @AutoCleanup("shutdown")
    def executor2 = Executors.newCachedThreadPool()

    def "we can set an Async Receiver Executor"() {
        when: "we set the executor in the service"
        jmsService.asyncReceiverExecutor = executor

        and: "we 'destroy' the service bean"
        jmsService.destroy()

        then: "the executor should receive a shutdown request"
        jmsService.asyncReceiverExecutor == executor
        executor.shutdown
    }

    def "setting a second executor will shutdown the first one"() {
        when: "we assign one executor"
        jmsService.asyncReceiverExecutor = executor

        and: "after that we set another"
        jmsService.asyncReceiverExecutor = executor2

        then: "the first executor should receive a shutdown request"
        jmsService.asyncReceiverExecutor == executor2
        executor.shutdown
    }

    def "we can disable auto-shutdown of the executor on destruction"() {
        given: "an executor that is set"
        jmsService.asyncReceiverExecutor = executor

        and: "the disablement of the shutdown flag"
        jmsService.asyncReceiverExecutorShutdown = false

        when: "we destroy the service"
        jmsService.destroy()

        then: "the executor shouldn't receive a shutdown request"
        jmsService.asyncReceiverExecutor == executor
        !executor.shutdown
    }

    def "we get a default executor if none is set"() {
        given: "a service"
        assert jmsService

        when:"we get the default executor"
        def _executor = jmsService.asyncReceiverExecutor

        and: "we destroy the service"
        jmsService.destroy()

        then: "the executor shouldn't be null and should have receive a shutdown request"
        _executor
        _executor.shutdown
    }
}
