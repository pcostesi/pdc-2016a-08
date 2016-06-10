
	package ar.edu.itba.protos.transport.support;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.SelectableChannel;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.Selector;
	import java.nio.channels.ServerSocketChannel;
	import java.nio.channels.SocketChannel;
	import java.util.ArrayList;
	import java.util.Iterator;
	import java.util.List;
	import java.util.Set;

	import javax.inject.Inject;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import ar.edu.itba.protos.transport.reactor.Reactor;

		/**
		* Un servidor recibe conexiones entrantes en las
		* direcciones y puertos especificados, y genera
		* eventos de forma no-bloqueante, los cuales son
		* despachados hacia un demultiplexor (implementado
		* mediante un reactor).
		*/

	public final class Server {

		// TODO: obtener por configuración Pablo!!!
		/**/private static final long TIMEOUT = 10000;
		/**/private static final long LAZY_INTERVAL_DETECTION = 1000;

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(Server.class);

		// Demultiplexador de eventos generados:
		private final Reactor demultiplexor;

		// Watchdog-timer utilizado para cerrar canales inactivos:
		private final WatchdogTimer watchdog;

		// Lista de sockets escuchando conexiones entrantes:
		private List<ServerSocketChannel> listeners = null;

		// Generador de eventos:
		private Selector selector;

		// Indica si el monitor de inactividad está activo:
		private volatile boolean monitoring = true;

		@Inject
		public Server(Reactor demultiplexor, WatchdogTimer watchdog) {

			this.watchdog = watchdog;
			this.demultiplexor = demultiplexor;

			try {

				watchdog.setTimeout(TIMEOUT);
				selector = Selector.open();
				listeners = new ArrayList<>();
			}
			catch (IOException exception) {

				logger.error(Message.CANNOT_RAISE.getMessage());
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

		public Server addListener(
				InetSocketAddress address,
				AttachmentFactory factory) throws IOException {

			logger.debug("Attaching listen socket {} with {}", address, factory);

			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().bind(address);
			channel.register(selector, SelectionKey.OP_ACCEPT, factory);

			listeners.add(channel);

			return this;
		}

		/*
		** En este caso se agrega una nueva dirección de escucha,
		** especificando directamente la IP y el puerto en cuestión.
		** Devuelve 'true' si pudo agregar el canal.
		*/

		public Server addListener(
				String IP, int port,
				AttachmentFactory factory) throws IOException {

			InetSocketAddress address = new InetSocketAddress(IP, port);
			return addListener(address, factory);
		}

		/*
		** Comienza a despachar eventos. Para ello, comienza por
		** seleccionar los canales que deben ser procesados, y luego
		** solicita que un manejador adecuado procese dicho evento.
		*/

		public void dispatch() throws IOException {

			// Levanto el monitor de inactividad:
			runWatchdog();

			while (true) {

				if (0 < selector.selectNow()) {

					Set<SelectionKey> keys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();

					while (iterator.hasNext()) {

						// Obtengo una clave:
						SelectionKey key = iterator.next();

						// Si no es un 'listener', actualizo el watchdog:
						if (!isListener(key))
							watchdog.update(key);

						// Solicito que un manejador resuelva el evento:
						demultiplexor.dispatch(key);

						// Quito la clave despachada:
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

			Set<SelectionKey> keys = selector.keys();

			// Cierra el monitoreo de actividades:
			watchdog.removeAll();
			monitoring = false;

			// Cierra los canales:
			for (SelectionKey key : keys)
				close(key);

			// Cierra los 'listeners':
			for (ServerSocketChannel listener : listeners)
				close(listener.keyFor(selector));

			listeners.clear();

			// Cierra el selector:
			if (selector.isOpen()) selector.close();
		}

		/*
		** Devuelve 'true' si la clave está activa para el
		** evento ACCEPT, es decir, que el canal se encuentra
		** a disposición de conexiones entrantes, tal cual lo
		** hace un 'ServerSocketChannel' (listener).
		*/

		private boolean isListener(SelectionKey key) {

			try {

				return 0 != (key.interestOps() & SelectionKey.OP_ACCEPT);
			}
			catch (CancelledKeyException exception) {

				logger.error(
					Message.UNEXPECTED_UNPLUG.getMessage(),
					tryToResolveAddress(key));

				close(key);
				return false;
			}
		}

		/*
		** Método público para cerrar canales de forma
		** segura. Todas las excepciones son suprimidas, por
		** lo que se deben tomar recaudos necesarios para
		** determinar el origen de las posibles fallas.
		*/

		public static void close(SelectionKey key) {

			if (key != null) {

				key.cancel();
				SelectableChannel channel = key.channel();
				try {

					if (channel.isOpen())
						channel.close();
				}
				catch (IOException spurious) {}
			}
		}

		/*
		** Intenta determinar la dirección IP y puerto al
		** cual este canal se encuentra asociado. Si no
		** puede resolver la dirección, genera un mensaje
		** especial.
		*/

		public static String tryToResolveAddress(SelectionKey key) {

			try {

				SelectableChannel channel = key.channel();
				if (channel instanceof SocketChannel)
					return ((SocketChannel) channel)
						.getRemoteAddress().toString();
				else
					return ((ServerSocketChannel) channel)
						.getLocalAddress().toString();
			}
			catch (IOException
				| NullPointerException spurious) {}
			return Message.UNKNOWN_ADDRESS.getMessage();
		}

		/*
		** Separa el monitor de inactividad en un thread
		** secundario, lo que reduce la latencia en el bucle
		** principal de selección.
		*/

		private void runWatchdog() {

			new Thread(new Runnable() {

				@Override
				public void run() {

					while (monitoring) {

						// Cierra canales inactivos:
						watchdog.killLazyActivities();
						try {

							Thread.sleep(LAZY_INTERVAL_DETECTION);
						}
						catch (InterruptedException spurious) {}
					}
				}
			}).start();
		}
	}
