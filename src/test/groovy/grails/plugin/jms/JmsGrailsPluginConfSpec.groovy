package grails.plugin.jms

import grails.config.Config
import grails.core.GrailsApplication
import org.grails.config.NavigableMapConfig
import org.grails.config.PropertySourcesConfig
import spock.lang.Specification
import spock.lang.Unroll

class JmsGrailsPluginConfSpec extends Specification {

    def jmsGrailsPlugin = new JmsGrailsPlugin()

    def "useDefaults"() {

        when: "no local JMS configuration"
            jmsGrailsPlugin.grailsApplication = Mock(GrailsApplication)
            jmsGrailsPlugin.grailsApplication.getConfig() >> Mock(Config)

        then:
            def jmsConfig = jmsGrailsPlugin.getJmsConfigurationWithDefaults()

        expect:
            jmsConfig == jmsGrailsPlugin.getDefaultConfig()

    }

    @Unroll("overriding default configuration should follow the rule [#rule]")
    def "overrideDefaults"() {
        given: "create a config"
            Config config = createConfig(configContent)

        when:
            jmsGrailsPlugin.grailsApplication = Mock(GrailsApplication)
            jmsGrailsPlugin.grailsApplication.getConfig() >> config
            NavigableMapConfig jmsConfig = toNavigableMapConfig(jmsGrailsPlugin.getJmsConfigurationWithDefaults())

        then:
            jmsConfig.navigate( (String[])configPath.toArray()) == expected

        where:
            rule                                                    | configContent             | configPath    | expected
            "override simple property"                              | [jms: [disabled: true]]   | ['disabled']      | true
            "adding new poperty"                                    | [jms: [newProp: 'test']]  | ['newProp']       | 'test'
            "overrriding deep default property"                     | [jms: [templates: [standard: [connectionFactoryBean: "pooledJmsConnectionFactory"]]]]  | ['templates', 'standard', 'connectionFactoryBean'] | 'pooledJmsConnectionFactory'
            "overriding only one deep default property's map value" | [jms: [templates: [standard: [connectionFactoryBean: "pooledJmsConnectionFactory"]]]]  | ['templates', 'standard', 'messageConverterBean']  | 'standardJmsMessageConverter'
    }



    private NavigableMapConfig toNavigableMapConfig(def config) {
        if(config instanceof NavigableMapConfig) {
            return (NavigableMapConfig)config
        }
        else {
            PropertySourcesConfig navConfig = new PropertySourcesConfig()
            navConfig.putAll(config)
            return navConfig
        }
    }

    private Config createConfig(Map configContent) {
        return new PropertySourcesConfig(configContent)
    }
}
