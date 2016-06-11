package ar.edu.itba.protos;

import com.google.inject.AbstractModule;

import ar.edu.itba.protos.transport.handler.AcceptHandler;
import ar.edu.itba.protos.transport.handler.ConnectHandler;
import ar.edu.itba.protos.transport.handler.ReadHandler;
import ar.edu.itba.protos.transport.handler.WriteHandler;
import ar.edu.itba.protos.transport.reactor.Reactor;
import ar.edu.itba.protos.transport.support.Server;
import ar.edu.itba.protos.transport.support.Synchronizer;
import ar.edu.itba.protos.transport.support.ThreadingCore;
import ar.edu.itba.protos.transport.support.WatchdogTimer;

/**
 * Injector config object. We use a single, app-wide config because the project
 * is too small to bother with modules.
 */
public class DIModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(POP3Server.class);
        bind(Server.class);
        bind(WatchdogTimer.class);
        bind(Reactor.class);
        bind(ThreadingCore.class);
        bind(Synchronizer.class);
        bind(AcceptHandler.class);
        bind(ReadHandler.class);
        bind(WriteHandler.class);
        bind(ConnectHandler.class);
    }

}
