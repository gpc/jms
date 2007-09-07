import org.codehaus.groovy.grails.commons.GrailsClassUtils

class JmsGrailsPlugin {
	def version = 0.1
    def author = "Justin Edelson"
    def authorEmail = "justin@justinedelson.com"
    def title = "This plugin adds MDB functionality to services."
	def dependsOn = [services:'0.6']
	def loadAfter = ['services']
	
	def watchedResources = ["**/grails-app/services/*Service.groovy"]
	
	def doWithSpring = {
		if (application.serviceClasses) {
			application.serviceClasses.each { service ->
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
	}   
	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)		
	}
	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional)
	}	                                      
	def doWithDynamicMethods = { ctx ->
		// TODO Implement additions to web.xml (optional)
	}	
	def onChange = { event ->
		// TODO Implement code that is executed when this class plugin class is changed  
		// the event contains: event.application and event.applicationContext objects
	}                                                                                  
	def onApplicationChange = { event ->
		// TODO Implement code that is executed when any class in a GrailsApplication changes
		// the event contain: event.source, event.application and event.applicationContext objects
	}
}
