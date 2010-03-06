package grails.plugin.jms.test.domain

import grails.plugin.spock.*
import grails.plugin.jms.test.*

class DomainServiceSpec extends IntegrationSpec {

    static transactional = false
    def jmsService
    def domainService
    
    void testIt() {
        given:
        def p = new Person(
            name: "p",
        )
        [1,2,3,4].each {
            p.addToThings(new Thing(name: it.toString()))
        }
        p.save(flush: true)
        when:
        jmsService.send(service: 'domain', 'p')
        then:
        domainService.message.sort() == ['1', '2', '3', '4']
    }
}
