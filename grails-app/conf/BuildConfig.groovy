grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        grailsHome()
        mavenCentral()
    }  
    dependencies {
        compile 'org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1.1'
        test ('org.apache.activemq:activemq-core:5.3.0') { 
            excludes 'activemq-openwire-generator'
            excludes 'xalan' // IVY-1006 - use xalan 2.7.0 to avoid (see below)
            excludes 'xml-apis' // GROOVY-3356
            exported = false
        }
    }
}