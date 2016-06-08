
package ar.edu.itba.protos.transport.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.transport.reactor.Reactor;

/**
 * Un servidor recibe conexiones entrantes en las direcciones y puertos
 * especificados, y genera eventos de forma no-bloqueante, los cuales son
 * despachados hacia un demultiplexor (implementado mediante un reactor).
 */

public final class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    // TODO: obtener por configuración Pablo!!!
    private static final long TIMEOUT = 10000;
    private static final long LAZY_DETECTION_INTERVAL = 1000;

    // Watchdog-timer utilizado para cerrar canales inactivos:
    private final WatchdogTimer watchdog
    = new WatchdogTimer(TIMEOUT);

    // Generador de eventos:
    private Selector selector;

    // Lista de sockets escuchando conexiones entrantes:
    private List<ServerSocketChannel> listeners = null;

    // Demultiplexador de eventos generados:
    private final Reactor demultiplexor;

    @Inject
    private Server(final Reactor demultiplexor) {
        this.demultiplexor = demultiplexor;
        try {
            selector = Selector.open();
            listeners = new ArrayList<ServerSocketChannel>();
        } catch (final IOException exception) {

            exception.printStackTrace();
        }
    }

    /*
     ** Devuelve la cantidad de 'listeners' activos en este servidor.
     ** Cada 'listener' se corresponde con una dirección IP y un puerto
     ** de escucha en la que se reciben conexiones entrantes.
     */

    public int getListeners() {

        return listeners.size();
    }

    /*
     ** Agrega una nueva dirección y puerto de escucha para este
     ** servidor. Es importante notar que el nuevo canal de escucha
     ** puede o no poseer un 'attachment'. En caso de que no posea,
     ** debe utilizarse algún mecanismo adicional para diferenciar
     ** en cual de las direcciones se recibió una petición de conexión.
     ** Devuelve 'true' si pudo agregar el canal.
     */

    public Server addListener(final InetSocketAddress address, final Object attach)
            throws IOException {

        logger.debug("Attaching listen socket {} to {}", address, attach);

        final ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(address);
        channel.register(selector, SelectionKey.OP_ACCEPT, attach);

        listeners.add(channel);

        return this;
    }

    /*
     ** En este caso se agrega una nueva dirección de escucha,
     ** especificando directamente la IP y el puerto en cuestión.
     ** Devuelve 'true' si pudo agregar el canal.
     */

    public Server addListener(final String IP, final int port, final Object attach)
            throws IOException {

        final InetSocketAddress address = new InetSocketAddress(IP, port);
        return addListener(address, attach);
    }

    /*
     ** Comienza a despachar eventos. Para ello, comienza por
     ** seleccionar los canales que deben ser procesados, y luego
     ** solicita que un manejador adecuado procese dicho evento.
     */

    public void dispatch() throws IOException {

        while (true) {

            // Cierro todos los canales inactivos:
            watchdog.killLazyActivities();

            if (0 < selector.select(LAZY_DETECTION_INTERVAL)) {

                final Set<SelectionKey> keys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {

                    // Obtengo una clave:
                    final SelectionKey key = iterator.next();

                    // Si no es un 'listener', actualizo el watchdog:
                    if (!isListener(key)) {

                        watchdog.removeActivity(key);
                        watchdog.addActivity(key);
                    }

                    // Habría que sacar esto, eventualmente...
                    logger.trace("> Select ({})", key);

                    // Solicito que un manejador resuelva el evento:
                    demultiplexor.dispatch(key);

                    iterator.remove();
                }
            }
        }
    }

    /*
     ** Implementa un 'graceful-shutdown' para todas las
     ** direcciones de escucha y para todos los clientes
     ** conectados. Luego de ejecutar este método, se puede
     ** levantar devuelta el servidor, especificando nuevos
     ** 'listeners' y despachando sus eventos.
     */

    public void shutdown() throws IOException {

        final Set<SelectionKey> keys = selector.keys();

        // Cierra el monitoreo de actividades:
        watchdog.removeAll();

        // Cierra los canales:
        for (final SelectionKey key : keys) {

            key.cancel();
            final Channel channel = key.channel();
            if (channel.isOpen()) {
                channel.close();
            }
        }

        // Cierra los 'listeners':
        for (final ServerSocketChannel listener : listeners) {

            // En teoría, no es necesario:
            if (listener.isOpen()) {
                listener.close();
            }
        }
        listeners.clear();

        // Cierra el selector:
        if (selector.isOpen()) {
            selector.close();
        }
    }

    /*
     ** Devuelve 'true' si la clave está activa para el
     ** evento ACCEPT, es decir, que el canal se encuentra
     ** a disposición de conexiones entrantes, tal cual lo
     ** hace un 'ServerSocketChannel' (listener).
     */

    private boolean isListener(final SelectionKey key) {

        try {
            return 0 != (key.interestOps() & SelectionKey.OP_ACCEPT);
        } catch (final CancelledKeyException exception) {
            return false;
        }
    }
}
