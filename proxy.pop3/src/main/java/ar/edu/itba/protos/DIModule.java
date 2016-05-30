package ar.edu.itba.protos;

import com.google.inject.AbstractModule;

import ar.edu.itba.protos.config.Configuration;
import ar.edu.itba.protos.transport.support.Server;

/**
 * Injector config object. We use a single, app-wide config because the project is
 * too small to bother with modules.
 */
public class DIModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(POP3Server.class);
		bind(Configuration.class);
		bind(Server.class);
	}

}
