package grails.plugin.jms


import grails.plugin.jms.listener.ListenerConfig
import grails.plugin.jms.listener.ListenerConfigFactory
import grails.plugin.jms.listener.ServiceInspector
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.apache.commons.logging.LogFactory
import grails.util.Environment

public final class JmsUtils {

    static LOG = LogFactory.getLog(this)

    private static GrailsApplication application

    private static Map<String, ListenerConfig> listenerConfigs = [:]
    private static ServiceInspector serviceInspector = new ServiceInspector()
    private static ListenerConfigFactory listenerConfigFactory = new ListenerConfigFactory()
    
    
    public static ConfigObject jmsConfig
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
        jmsConfigHash = jmsConfig.hashCode()
    }
    
    public static boolean setNewConfig(sourceJmsConfig){
        
        def newJmsConfig = defaultConfig.merge(sourceJmsConfig)
        def newJmsConfigHash = newJmsConfig.hashCode()
        if (newJmsConfigHash == jmsConfigHash) {
            return false
        }
        jmsConfig = newJmsConfig
        jmsConfigHash = newJmsConfigHash
        return true
    }


}
