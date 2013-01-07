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
            autoStartup = true
        }
        transacted {
            meta {
                parentBean = 'standardJmsListenerContainer'
            }
            transactionManagerBean = "transactionManager"
            sessionTransacted = true
            autoStartup = true
        }
        manualStart {
            meta {
                parentBean = 'standardJmsListenerContainer'
            }
            autoStartup = false
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
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
