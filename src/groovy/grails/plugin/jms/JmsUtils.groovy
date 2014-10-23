package grails.plugin.jms


import grails.plugin.jms.listener.ListenerConfig
import grails.plugin.jms.listener.ListenerConfigFactory
import grails.plugin.jms.listener.ServiceInspector
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.ApplicationContext
import org.apache.commons.logging.LogFactory
import grails.util.Environment
import grails.util.Holders

public final class JmsUtils {

    static LOG = LogFactory.getLog(this)

    private static GrailsApplication application

    private static Map<String, List<ListenerConfig>> listenerConfigs = [:]
    private static ServiceInspector serviceInspector = new ServiceInspector()
    private static ListenerConfigFactory listenerConfigFactory = new ListenerConfigFactory()
    
    
    public static Map jmsConfig
    public static int jmsConfigHash
    

    private JmsUtils() {
        // static only
    }
    
   
    /**
     * Set at startup by plugin.
     * @param app the application
     */
    public static void setApplication(GrailsApplication app) {
        listenerConfigs.clear()
        application = app
        resetJmsConfig()
    }
    
    private static ConfigObject getDefaultConfig() {
        return new ConfigSlurper(Environment.current.name).parse(DefaultJmsBeans)
    }
    
    private static void resetJmsConfig(){
        jmsConfig = defaultConfig.merge(application.config.jms)
        LOG.debug("merged config: $jmsConfig")
        // We have to take a hash now because a config object
        // will dynamically create nested maps as needed
        jmsConfigHash = jmsConfig.flatten().sort().hashCode().hashCode()
    }
    
    public static boolean compareAndSetNewConfig(sourceJmsConfig){
        LOG.info "Setting new jms config."
        Map newJmsConfig = defaultConfig.merge(sourceJmsConfig)
        int newJmsConfigHash = newJmsConfig.flatten().sort().hashCode()
        
        if (newJmsConfigHash == jmsConfigHash) {
            LOG.info "Jms config has not changed."
            return false
        }
        jmsConfig = newJmsConfig
        jmsConfigHash = newJmsConfigHash
        return true
    }
    

    /**
     * Use when config.jms.manualStart=true. In bootstrap add JmsGrailsPlugin.startListeners()
     */
    static void startListeners(){
        LOG.info "Starting JMS listeners with application mainContext."
        startListeners(application.mainContext)
    }
    static void startListeners(ApplicationContext applicationContext){
        LOG.info "Starting JMS listeners."
        listenerConfigs.each { String serviceClassName, serviceClassListenerConfigs ->
            serviceClassListenerConfigs.each { ListenerConfig listenerConfig->
                 startListenerContainer(listenerConfig, applicationContext)
            }
        }
    }

    static void startListenerContainer(ListenerConfig listenerConfig, ApplicationContext applicationContext) {
        LOG.info "Starting JMS listener: ${listenerConfig.listenerContainerBeanName}"
        applicationContext.getBean(listenerConfig.listenerContainerBeanName).start()
    }

}
