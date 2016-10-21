package jms

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration
import org.springframework.boot.autoconfigure.jta.JtaAutoConfiguration

@EnableAutoConfiguration(exclude = [JmsAutoConfiguration, JtaAutoConfiguration])
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}