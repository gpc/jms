package grails.plugin.jms.connection;

import javax.jms.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
* A connection factory that falls back to another connection factory if the primary
* connection factory fails.
* <p />
* If you do not assign a fallback JMS queue, it will try to use a
* local (i.e.: JVM-resident in-memory) 
* ActiveMQ connection, assuming that {@code org.apache.activemq.ActiveMQConnectionFactory} is
* a class on the class path. (See 
* <a href="https://activemq.apache.org/activemq-551-release.html#ActiveMQ5.5.1Release-GettingtheBinariesusingMaven2">
* the ActiveMQ download page's Maven config area</a> for the information necessary to pull it down.)
* <p />
* This class is thread-safe, in that it is safe to change the parent no matter what other
* activity may be going on.
*/
public class JmsFallbackConnectionFactory implements ConnectionFactory {


	private static final Log LOG = LogFactory.getLog(JmsLocalFallbackConnectionFactory.class);

	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	private final AtomicReference<WrappedParentConnectionFactory> parentRef = new AtomicReference<>(null);
	private final AtomicReference<ConnectionFactory> fallbackRef = new AtomicReference<>(null);

	public JmsLocalFallbackConnectionFactory() {
		LOG.trace("Creating a " + this.getClass().getSimpleName() + " instance without a primary");
	}

	public JmsLocalFallbackConnectionFactory(ConnectionFactory primary) {
		this();
		LOG.trace("Creating a " + this.getClass().getSimpleName() + " instance with primary [" + parent + "]");
		setParent(parent);
	}

	public void setParent(ConnectionFactory parent) {
		if(parent == null) {
			LOG.warn("Assigning a null parent to a " + this.getClass().getSimpleName() + ": will always use local fallback JMS");
			parentRef.set(null);
		} else {
			LOG.debug("Setting a non-null parent to a " + this.getClass().getSimpleName() + ":  [" + parent + "]");
			parent.set(new WrappedParentConnectionFactory(parent));
		}
	}

	public ConnectionFactory getParent() {
		WrappedParentConnectionFactory toReturn = parent.get();
		if(toReturn == null) {
			LOG.info("Returning a null parent from a " + this.getClass().getSimpleName());
			return null;
		} else {
			return toReturn.unwrap();
		}
	}

	public void setFallback(ConnectionFactory fallback) {
		if(fallback == null) {
			LOG.warn("Assigning a null fallback to a " + this.getClass().getSimpleName() + ": will always use local fallback JMS");
		}
		fallbackRef.set(fallback);
	}

	public ConnectionFactory getFallback() {
		ConnectionFactory toReturn = fallbackRef.get();
		if(toReturn == null) {
			LOG.info("Creating a local fallback JMS queue");
			toReturn = createLocalFallback();
			if(!fallbackRef.compareAndSet(null, toReturn)) {
				LOG.info("Detected that the fallback JMS queue was set while initializing a local fallback; trying to get fallback again");
				return getFallback();
			}
		}
		return toReturn;
	}

	protected ConnectionFactory createLocalFallback() {
		final String className = "org.apache.activemq.ActiveMQConnectionFactory";
		final Class<ConnectionFactory> clazz;
		try {
			clazz  = (Class<ConnectionFactory>)Class.forName("org.apache.activemq.ActiveMQConnectionFactory");
		} catch(ClassNotFoundException cnfe) {
			LOG.warn("Could not find " + className + ", so cannot create the in-memory fallback queue");
			return null;
		}
		
		final Constructor<ConnectionFactory> constructor;
		try {
			constructor = clazz.getConstructor(String.class);
		} catch(NoSuchMethodException ex) {
			throw new RuntimeException("Serious implementation error: could not find expected String constructor on " + clazz.getName());
		}

		final String connStr = 
			"vm://localhost?broker.persistent=false&broker.useShutdownHook=false&broker.brokerName=fallback-" + 
			COUNTER.incrementAndGet();

		try {
			return constructor.newInstance(connStr);
		} catch(Exception ex) {
			throw new RuntimeException("Could not construct an instance of " + clazz.getName() + " using String \"" + connStr + "\"", ex);
		}
	}

	public Connection createConnection() throws JMSException {
		ConnectionFactory toUse = getParent();
		if(toUse == null) {
			toUse = getFallback();
			if(toUse == null) {
				throw new IllegalStateException("Could not retrieve either a parent or fallback Connection Factory");
			}
			LOG.info("Using fallback JMS connection factory");
		}
		return toUse.createConnection();
	}

	public Connection createConnection(String userName, String password) throws JMSException {
		ConnectionFactory toUse = getParent();
		if(toUse == null) {
			toUse = getFallback();
			if(toUse == null) {
				throw new IllegalStateException("Could not retrieve either a parent or fallback Connection Factory");
			}
			LOG.info("Using fallback JMS connection factory");
		}
		return toUse.createConnection(userName, password);
	}

	public class WrappedParentConnectionFactory implements ConnectionFactory {

		private final ConnectionFactory source;
		
		public WrappedParentConnectionFactory(ConnectionFactory source) {
			if(source == null) throw new IllegalArgumentException("Connection Factory to wrap cannot be null");
			this.source = source;
		}

		/**
		* Attempts to create a connection; failing that, 
		* attempts to create a fallback connection.
		*/
		public Connection createConnection() throws JMSException {
			try {
				return source.createConnection();
			} catch(Exception e) {
				LOG.info("Error in connecting to the JMS queue; reverting to fallback JMS connection", e);
			}
			return getFallback().createConnection();
		}

		/**
		* Attempts to create a connection with the given username and password; failing that, 
		* attempts to create a fallback connection with the given username and password; failing
		* that, attempts to create a fallback connection without a username or password.
		*/
		public Connection createConnection(String userName, String password) throws JMSException {
			try {
				return source.createConnection(userName, password);
			} catch(Exception e) {
				LOG.info("Error in connecting to the JMS queue; reverting to fallback JMS connection", e);
			}
			try {
				return getFallback().createConnection(userName, password);
			} catch(Exception e) {
				LOG.info("Reverting to fallback JMS connection without username or password", e);
			}
			return getFallback().createConnection();
		}

		/**
		* Provides the underlying parent connection factory.
		*/
		public ConnectionFactory unwrap() {
			return source;
		}

	}

}
