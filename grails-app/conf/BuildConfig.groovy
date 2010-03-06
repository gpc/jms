grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        grailsHome()
        mavenCentral()
    }  
    dependencies {
        compile 'org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1.1'
    }
}