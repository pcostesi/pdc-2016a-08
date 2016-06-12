
package ar.edu.itba.protos;

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.config.ConfigurationLoader;
import ar.edu.itba.protos.config.ProxyConfiguration;
import ar.edu.itba.protos.transport.concrete.AdminAttachmentFactory;
import ar.edu.itba.protos.transport.concrete.ForwardAttachmentFactory;
import ar.edu.itba.protos.transport.concrete.TestAttachmentFactory;
import ar.edu.itba.protos.transport.handler.AcceptHandler;
import ar.edu.itba.protos.transport.handler.ConnectHandler;
import ar.edu.itba.protos.transport.handler.ReadHandler;
import ar.edu.itba.protos.transport.handler.WriteHandler;
import ar.edu.itba.protos.transport.reactor.Event;
import ar.edu.itba.protos.transport.reactor.Reactor;
import ar.edu.itba.protos.transport.support.AttachmentFactory;
import ar.edu.itba.protos.transport.support.Message;
import ar.edu.itba.protos.transport.support.Server;

/**
 * Ciclo principal de ejecución (master thread). Su función es levantar el
 * servidor en las direcciones y puertos especificados y comenzar a recibir
 * conexiones entrantes, las cuales serán despachadas entre los 'workers'
 * disponibles.
 */

public final class POP3Server {
    private static final Logger logger = LoggerFactory.getLogger(POP3Server.class);
    private final Reactor demultiplexor;
    private final Server pop3;

    // Handler injections:
    @Inject private AcceptHandler acceptHandler;
    @Inject private ReadHandler readHandler;
    @Inject private WriteHandler writeHandler;
    @Inject private ConnectHandler connectHandler;

    @Inject
    private POP3Server(final Reactor demultiplexor, final Server pop3) {
        this.demultiplexor = demultiplexor;
        this.pop3 = pop3;
    }

    public void run() throws IOException {

        final ProxyConfiguration config = ConfigurationLoader.getProxyConfig();

        /*
         ** Fábricas de 'attachments'. Cada servidor puede tener una fábrica
         * distinta en cada puerto de escucha (es decir, en cada 'listener'):
         */
        final AttachmentFactory forwardFactory = new ForwardAttachmentFactory();

        final AttachmentFactory adminFactory = new AdminAttachmentFactory();

        /*
         ** Se instalan los manejadores (Handlers) en el demultiplexador de
         * eventos global:
         */
        demultiplexor.add(acceptHandler, Event.ACCEPT)
                .add(readHandler, Event.READ)
                .add(writeHandler, Event.WRITE)
                .add(connectHandler, Event.CONNECT);

        /*
         ** Se instancia un nuevo servidor y se aplica un 'binding' en cada
         * dirección especificada:
         */
        pop3.addListener(config.getListenAddr(), config.getListenPort(), forwardFactory)
        .addListener(config.getAdminListenAddr(), config.getAdminListenPort(), adminFactory)
        .addListener("0.0.0.0", 60000, new TestAttachmentFactory());

        try {

            // Alguien, al menos, debe estar escuchando:
            if (0 < pop3.getListeners()) {
                pop3.dispatch();
                pop3.shutdown();
            }
        } catch (final IOException exception) {
            logger.error(Message.CANNOT_RAISE.getMessage());
        }

        // Quitar todos los manejadores del demultiplexor global:
        demultiplexor.unplug();

        // Bloquear por completo el rector:
        demultiplexor.block();

        logger.info(Message.SHUTDOWN_COMPLETE.getMessage());
    }
}
