package grails.plugins.jms.test.domain

import grails.plugins.jms.test.Person
import grails.plugins.jms.test.Thing
import grails.test.mixin.integration.Integration
import grails.transaction.Transactional
import spock.lang.Specification

@Integration
class DomainServiceSpec extends Specification {

    static transactional = false

    def jmsService
    def domainService

    @Transactional
    void setupData() {
        def p = new Person()
        p.name = 'p'
        [1, 2, 3, 4].each {
            p.addToThings(new Thing(name: it.toString()))
        }
        p.save(flush: true, failOnError: true)
    }

    void testIt() {
        given:
        setupData()
        when:
        jmsService.send(service: 'domain', 'p')
        then:
        domainService.message.sort() == ['1', '2', '3', '4']
    }
}
