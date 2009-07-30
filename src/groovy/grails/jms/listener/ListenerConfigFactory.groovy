package grails.jms.listener
import org.codehaus.groovy.grails.commons.GrailsClassUtils

class ListenerConfigFactory {

    def getListenerConfig(Class serviceClass, grailsApplication) {
        new ListenerConfig(
            serviceBeanName: GrailsClassUtils.getPropertyName(serviceClass),
            grailsApplication: grailsApplication
        )
    }
}