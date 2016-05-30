package ar.edu.itba.protos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ar.edu.itba.protos.config.Configuration;

public class App {
	private final static Injector injector = Guice.createInjector(new DIModule());
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	
	public static void main(String[] args) {
		logger.debug("Initializing POP3Server");
		final Configuration config = injector.getInstance(Configuration.class);
		logger.debug("Config set to {}", config);
		injector.getInstance(POP3Server.class).run();
	}

}
