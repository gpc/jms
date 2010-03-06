package grails.plugin.jms.test.domain

import grails.plugin.jms.test.TestListeningServiceSupport
import grails.plugin.jms.test.*

class DomainService extends TestListeningServiceSupport {

    static transactional = false
    static exposes = ['jms']
    
    def onMessage(msg) {
        println "here"
        try {
            def person = Person.findByName(msg)
            putMessage(person.things*.name)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}