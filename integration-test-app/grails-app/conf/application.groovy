grails.codegen.defaultPackage = 'grails.plugin.jms.test'

appName = 'grails-jms'

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
         * You can override the destination name specified by the annotations Queue and Subscriber
         * e.g @Subscriber(topic='$named.topic.key') and @Queue(name='$named.queue.key')
         */
        named.topic.key = 'conf.named.topic'

        named.queue.key = 'conf.named.queue'
    }
}

dataSource {
    dbCreate = 'update'
    driverClassName = 'org.h2.Driver'
    jmxExport = false
    password = ''
    pooled = true
    url = 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
    username = 'sa'
}

hibernate {
    cache {
        queries = false
        use_query_cache = false
        use_second_level_cache = false
    }
    format_sql = true
    use_sql_comments = true
}

info {
    app {
        name = '@info.app.name@'
        version = '@info.app.version@'
        grailsVersion = '@info.app.grailsVersion@'
    }
}

spring.groovy.template.'check-template-location' = false