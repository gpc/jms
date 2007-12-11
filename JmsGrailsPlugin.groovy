import org.codehaus.groovy.grails.commons.GrailsClassUtils
import grails.util.GrailsUtil
import org.springframework.jms.core.JmsTemplate

class JmsGrailsPlugin {
	def version = 0.2
    def author = "Justin Edelson"
    def authorEmail = "justin@justinedelson.com"
    def title = "This plugin adds MDB functionality to services."
    
	def loadAfter = ['services', 'controllers']
	def observe = ['services', 'controllers']
    def dependsOn = [services: GrailsUtil.getGrailsVersion(),
                     controllers: GrailsUtil.getGrailsVersion()]
	
	def doWithSpring = {
		application.serviceClasses?.each { service ->
			def serviceClass = service.getClazz()
			def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')
			if (exposeList!=null && exposeList.contains('jms')) {
				println '>>>> exposing ' + service.shortName
				def sName = service.propertyName.replaceFirst("Service","")
					
				def listenerCount = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'listenerCount')
				if (!listenerCount)
					listenerCount = 1
					
				def destination = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'destination')
				if (!destination)
					destination = sName
					
				def listenerMethod = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'listenerMethod')
				if (!listenerMethod)
					listenerMethod = "onMessage"
						
				def pubSub = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'pubSub')
				if (!pubSub)
					pubSub = false
				
				"${sName}JMSListener"(org.codehaus.grails.jms.ClosureMessageListenerAdapter, ref("${service.propertyName}")) {
					defaultListenerMethod = listenerMethod
				}
					
				"${sName}JMSListenerContainer"(org.springframework.jms.listener.DefaultMessageListenerContainer) {
					concurrentConsumers = listenerCount
					destinationName = destination
					pubSubDomain = pubSub
					connectionFactory = ref("connectionFactory")
					messageListener = ref("${sName}JMSListener")
				}
			}
		}
	}   
	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)		
	}
	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional)
	}
    def sendJMSMessage = {jmsTemplate, destinationName, payload ->
    	def convertedPayload = payload
    	if (payload instanceof GString) {
    	    convertedPayload = payload.toString()
    	}
    	jmsTemplate.convertAndSend(destinationName, convertedPayload)
	}        
	def doWithDynamicMethods = { ctx ->
		def connectionFactory = ctx.getBean("connectionFactory")
		def queueTemplate = new JmsTemplate(connectionFactory)
		def topicTemplate = new JmsTemplate(connectionFactory)
		topicTemplate.setPubSubDomain(true)
		application.serviceClasses?.each { service ->
			def mc = service.metaClass
			mc.sendJMSMessage = sendJMSMessage.curry(queueTemplate)
			mc.sendQueueJMSMessage = sendJMSMessage.curry(queueTemplate)
			mc.sendPubSubJMSMessage = sendJMSMessage.curry(topicTemplate)
			mc.sendTopicJMSMessage = sendJMSMessage.curry(topicTemplate)
		}
		application.controllerClasses?.each { controller ->
    	   def mc = controller.metaClass
			mc.sendJMSMessage = sendJMSMessage.curry(queueTemplate)
			mc.sendQueueJMSMessage = sendJMSMessage.curry(queueTemplate)
			mc.sendPubSubJMSMessage = sendJMSMessage.curry(topicTemplate)
			mc.sendTopicJMSMessage = sendJMSMessage.curry(topicTemplate)
    	}
	}	
	def onChange = { event ->
		if (event.source && event.ctx) {
		    def connectionFactory = event.ctx.getBean("connectionFactory")
			def queueTemplate = new JmsTemplate(connectionFactory)
			def topicTemplate = new JmsTemplate(connectionFactory)
		    topicTemplate.setPubSubDomain(true)
		    def mc = event.source.metaClass
			mc.sendJMSMessage = sendJMSMessage.curry(queueTemplate)
			mc.sendQueueJMSMessage = sendJMSMessage.curry(queueTemplate)
			mc.sendPubSubJMSMessage = sendJMSMessage.curry(topicTemplate)
			mc.sendTopicJMSMessage = sendJMSMessage.curry(topicTemplate)
		}
	}                                                                                  
	def onApplicationChange = { event ->
		// TODO Implement code that is executed when any class in a GrailsApplication changes
		// the event contain: event.source, event.application and event.applicationContext objects
	}
}
