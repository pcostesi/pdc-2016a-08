package ar.edu.itba.protos;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ar.edu.itba.protos.config.ConfigurationLoader;

public class App {
	private final static Injector injector = Guice.createInjector(new DIModule());
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static final int CONFIG_ERR = -1;
	private static final int SERVER_ERR = -2;

	private static void initializeProxyConfig() {
		try {
			ConfigurationLoader.loadProxyConfig("proxy.xml");
		} catch (JAXBException e) {
			logger.error("Invalid config file", e);
			System.exit(CONFIG_ERR);
		}
	}
	
	private static void runServer() {
		try {
			injector.getInstance(POP3Server.class).run();
		} catch (IOException e) {
			logger.error("Fatal: could not run the server", e);
			System.exit(SERVER_ERR);
		}
	}
	
	public static void main(String[] args) {
		logger.debug("Initializing POP3Server");
		initializeProxyConfig();
		runServer();
	}

}