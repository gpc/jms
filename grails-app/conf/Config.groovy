/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
log4j = {
    error ''
    debug 'grails.plugin.jms'
    debug 'grails.app.service'
}

beans {
    /* If you want to define an executor and/or disable the auto-shutdown mechanism for the async executor.
    jmsService {
        asyncReceiverExecutor = Executors.newSingleThreadExecutor()
        asyncReceiverExecutorShutdown = false
    }
   */
}

jms {
    disabled = false

    templates {
        other {
            meta {
                parentBean = 'standardJmsTemplate'
            }
            connectionFactoryBean = "otherJmsConnectionFactory"
        }
        transacted {
            meta {
                parentBean = 'standardJmsTemplate'
            }
            sessionTransacted = true
        }
    }
    containers {
        other {
            meta {
                parentBean = 'standardJmsListenerContainer'
            }
            connectionFactoryBean = "otherJmsConnectionFactory"
        }
        other {
            meta {
                parentBean = 'standardJmsListenerContainer'
            }
            connectionFactoryBean = "otherJmsConnectionFactory"
        }
        transacted {
            meta {
                parentBean = 'standardJmsListenerContainer'
            }
            transactionManagerBean = "transactionManager"
            sessionTransacted = true
        }
    }
    adapters {
        other {
            meta {
                parentBean = 'standardJmsListenerAdapter'
            }
            messageConverter = null
        }
    }
    destinations {
        /**
         *You can override the destination name specified by the annotations Queue and Subscriber
         * e.g @Subscriber(topic='$named.topic.key') and @Queue(name='$named.queue.key')
         */
        named.topic.key = 'conf.named.topic'

        named.queue.key = 'conf.named.queue'
    }
}

grails {
    doc {
        title = "Grails JMS Plugin"
        subtitle = "JMS integration for Grails"
        authors = "Grails Plugin Collective"
        copyright = "Copies of this document may be made for your own use and for distribution to others, provided that you do not charge any fee for such copies and further provided that each copy contains this Copyright Notice, whether distributed in print or electronically."
        footer = "Developed by the <a href='http://gpc.github.com'>Grails Plugin Collective</a>"
        license = "Apache License 2.0"
    }
}

// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    info 'grails.plugin.jms.test'
    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}

