grails.project.dependency.resolution = {
    inherits("global")
    log "warn"
    repositories {
        grailsPlugins()
        grailsHome()
        mavenCentral()
    }
    dependencies {
        compile 'org.apache.activemq:activemq-core:5.3.0'
    }
}

grails.plugin.location.'jms' = '../../../'
