log4j = {
    error ''
    debug 'grails.plugin.jms'
}

dataSource {
    pooled = true
    driverClassName = "org.hsqldb.jdbcDriver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'com.opensymphony.oscache.hibernate.OSCacheProvider'
}

environments {
    development {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:hsqldb:mem:devDB"
        }
    }
    test {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:hsqldb:mem:testDb"
        }
    }
}

jms {
    templates {
        other {
            meta {
                parentBean = 'standardJmsTemplate'
            }
            connectionFactoryBean = "otherJmsConnectionFactory"
        }
    }
    containers {
        other {
            meta {
                parentBean = 'standardJmsListenerContainer'
            }
            connectionFactoryBean = "otherJmsConnectionFactory"
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
}