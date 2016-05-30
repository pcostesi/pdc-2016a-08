
package ar.edu.itba.protos.transport.support;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ar.edu.itba.protos.transport.reactor.Reactor;

/**
 * Un servidor genérico recibe conexiones entrantes en las direcciones y puertos
 * especificados, y genera eventos de forma no-bloqueante, los cuales son
 * despachados hacia un demultiplexor (implementado mediante un reactor).
 */

public final class Server {

	// Generador de eventos:
	private Selector selector;

	// Lista de sockets escuchando conexiones entrantes:
	private List<ServerSocketChannel> listeners = null;

	// Demultiplexador de eventos generados:
	private final Reactor demultiplexor = Reactor.getInstance();

	public Server() {

		try {

			selector = Selector.open();
			listeners = new ArrayList<ServerSocketChannel>();
		} catch (IOException exception) {

			exception.printStackTrace();
		}
	}

	/*
	 ** Devuelve la cantidad de 'listeners' activos en este servidor. Cada
	 * 'listener' se corresponde con una dirección IP y un puerto de escucha en
	 * la que se reciben conexiones entrantes.
	 */

	public int getListeners() {

		return listeners.size();
	}

	/*
	 ** Agrega una nueva dirección y puerto de escucha para este servidor. Es
	 * importante notar que el nuevo canal de escucha puede o no poseer un
	 * 'attachment'. En caso de que no posea, debe utilizarse algún mecanismo
	 * adicional para diferenciar en cual de las direcciones se recibió una
	 * petición de conexión. Devuelve 'true' si pudo agregar el canal.
	 */

	public Server addListener(InetSocketAddress address, Object attach) {

		try {

			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().bind(address);
			channel.register(selector, SelectionKey.OP_ACCEPT, attach);

			// Si todo funcionó, agrego el nuevo canal:
			listeners.add(channel);
		} catch (BindException exception) {

			System.out.println(Message.CANNOT_BIND);
		} catch (SocketException exception) {

			System.out.println(Message.UNRESOLVED_ADDRESS);
		} catch (IOException exception) {

			System.out.println(Message.CANNOT_LISTEN);
		}
		return this;
	}

	/*
	 ** En este caso se agrega una nueva dirección de escucha, especificando
	 * directamente la IP y el puerto en cuestión. Devuelve 'true' si pudo
	 * agregar el canal.
	 */

	public Server addListener(String IP, int port, Object attach) {

		InetSocketAddress address = new InetSocketAddress(IP, port);
		return addListener(address, attach);
	}

	/*
	 ** Comienza a despachar eventos. Para ello, comienza por seleccionar los
	 * canales que deben ser procesados, y luego solicita que un manejador
	 * adecuado procese dicho evento.
	 */

	// ¿Este 'timeout' debe existir?:
	private static final int TIMEOUT = 10000;

	public void dispatch() throws IOException {

		while (true) {

			if (0 < selector.select(TIMEOUT)) {

				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();

				while (iterator.hasNext()) {

					SelectionKey key = iterator.next();
					System.out.println("> Select (" + key + ")");

					// Solicito que un manejador resuelva el evento:
					demultiplexor.dispatch(key);

					iterator.remove();
				}
			} else {

				System.out.println("Selector Timeout");
				return;
			}
		}
	}

	/*
	 ** Implementa un 'graceful-shutdown' para todas las direcciones de escucha y
	 * para todos los clientes conectados. Luego de ejecutar este método, se
	 * puede levantar devuelta el servidor, especificando nuevos 'listeners' y
	 * despachando sus eventos.
	 */

	public void shutdown() throws IOException {

		Set<SelectionKey> keys = selector.keys();

		// Cierra los canales:
		for (SelectionKey key : keys) {

			key.cancel();
			Channel channel = key.channel();
			if (channel.isOpen())
				channel.close();
		}

		// Cierra los 'listeners':
		for (ServerSocketChannel listener : listeners) {

			// En teoría, no es necesario:
			if (listener.isOpen())
				listener.close();
		}
		listeners.clear();

		// Cierra el selector:
		if (selector.isOpen())
			selector.close();
	}
}
