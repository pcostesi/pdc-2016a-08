package ar.edu.itba.protos;

import com.google.inject.AbstractModule;

import ar.edu.itba.protos.transport.reactor.Reactor;
import ar.edu.itba.protos.transport.support.Server;
import ar.edu.itba.protos.transport.support.Synchronizer;

/**
 * Injector config object. We use a single, app-wide config because the project
 * is too small to bother with modules.
 */
public class DIModule extends AbstractModule {

	/**
	* @see
	*	<a href = "https://github.com/google/guice/wiki/Scopes">
	*		Google Guice Scopes
	*	</a>
	*/

    @Override
    protected void configure() {
        bind(POP3Server.class);
        bind(Server.class);
        bind(Reactor.class);
        bind(Synchronizer.class);
    }

}
