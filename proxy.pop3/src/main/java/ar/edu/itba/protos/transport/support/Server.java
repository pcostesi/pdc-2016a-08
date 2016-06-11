
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
		* <p>Un servidor recibe conexiones entrantes en las
		* direcciones y puertos especificados, y genera
		* eventos de forma no-bloqueante, los cuales son
		* despachados hacia un demultiplexor (implementado
		* mediante un reactor). En cada interfaz especificada
		* se asocia una fábrica de <i>attachments</i>.</p>
		*/

	public final class Server {

		// TODO: obtener por configuración Pablo!!!
		/**/private static final long TIMEOUT = 15000;
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

		/**
		* <p>Devuelve la cantidad de <b>listeners</b> activos en este
		* servidor. Cada <i>listener</i> se corresponde con una dirección
		* IP y un puerto de escucha en la que se reciben conexiones
		* entrantes.</p>
		*
		* @return La cantidad de interfaces abiertas en las que este
		* 	servidor está escuchando.
		*/

		public int getListeners() {

			return listeners.size();
		}

		/**
		* <p>Agrega una nueva dirección y puerto de escucha para este
		* servidor. Es importante notar que el nuevo canal de escucha
		* puede o no poseer un <i>attachment</i>. En caso de que no posea,
		* debe utilizarse algún mecanismo adicional para diferenciar
		* en cuál de las direcciones se recibió una petición de conexión.</p>
		*
		* @param address
		*	La dirección en la cual escuchar conexiones entrantes.
		* @param factory
		*	La fábrica de <i>attachments</i>.
		*
		* @return El servidor sobre el cual se instalaron las interfaces.
		*/

		public Server addListener(
				InetSocketAddress address,
				AttachmentFactory factory) {

			try {

				ServerSocketChannel channel = ServerSocketChannel.open();
				channel.configureBlocking(false);
				channel.socket().bind(address);
				channel.register(selector, SelectionKey.OP_ACCEPT, factory);
				listeners.add(channel);
			}
			catch (IllegalArgumentException exception) {

				logger.error(
					Message.INVALID_ADDRESS.getMessage(),
					address);
			}
			catch (IOException exception) {

				logger.error(
					Message.CANNOT_LISTEN.getMessage(),
					address);
			}
			return this;
		}

		/**
		* En este caso se agrega una nueva dirección de escucha,
		* especificando directamente la IP y el puerto en cuestión.
		*
		* @param IP
		*	Dirección IP sobre la cual se recibirán conexiones entrantes.
		* @param port
		*	Puerto sobre el cual escuchar.
		* @param factory
		*	La fábrica de <i>attachments</i>.
		*
		* @return El servidor sobre el cual se instalaron las interfaces.
		*/

		public Server addListener(
				String IP, int port,
				AttachmentFactory factory) {

			try {

				InetSocketAddress address = new InetSocketAddress(IP, port);
				return addListener(address, factory);
			}
			catch (IllegalArgumentException exception) {

				logger.error(
					Message.INVALID_ADDRESS.getMessage(),
					IP + ":" + port);
			}
			return this;
		}

		/**
		* <p>Comienza a despachar eventos. Para ello, comienza por
		* seleccionar los canales que deben ser procesados, y luego
		* solicita que un manejador adecuado procese dicho evento.</p>
		*
		* @throws IOException
		*	Si ocurre algún error de I/O inesperado.
		*/

		public void dispatch() throws IOException {

			logger.info(toString());

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

		/**
		* <p>Implementa un <b>graceful-shutdown</b> para todas las
		* direcciones de escucha y para todos los clientes
		* conectados. Luego de ejecutar este método, se puede
		* levantar devuelta el servidor, especificando nuevos
		* <i>listeners</i> y despachando sus eventos.</p>
		*
		* @throws IOException
		*	Si ocurre algún error de I/O inesperado.
		*/

		public void shutdown() throws IOException {

			logger.info(Message.SERVER_SHUTDOWN.getMessage());

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

		/**
		* <p>Genera una cadena que representa el estado del servidor,
		* en la cual se especifican todas las interfaces en las
		* que se están escuchando conexiones entrantes. Si no puede
		* determinar las interfaces, devuelve una cadena especial.</p>
		*
		* @return Una cadena que representa el estado del servidor.
		*/

		@Override
		public String toString() {

			try {

				StringBuilder builder = new StringBuilder();
				builder.append("Escuchando en las direcciones {");

				for (ServerSocketChannel listener : listeners) {

					builder.append(listener.getLocalAddress().toString());
					builder.append(", ");
				}

				if (0 < getListeners()) {

					builder.deleteCharAt(builder.length() - 1);
					builder.deleteCharAt(builder.length() - 1);
				}

				builder.append("}.");
				return builder.toString();
			}
			catch (IOException exception) {

				return Message.UNKNOWN_INTERFACES.getMessage();
			}
		}

		/**
		* <p>Método público para cerrar canales de forma
		* segura. Todas las excepciones son suprimidas, por
		* lo que se deben tomar recaudos necesarios para
		* determinar el origen de las posibles fallas.</p>
		*
		* @param key
		*	La clave a cancelar, cuyo canal asociado se cerrará.
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

		/**
		* <p>Intenta determinar la dirección IP y puerto al
		* cual este canal se encuentra asociado. Si no
		* puede resolver la dirección, genera un mensaje
		* especial.</p>
		*
		* @param key
		*	La clave para la cual se intentará resolver su dirección.
		*
		* @return Una cadena que representa la dirección asociada a
		*	esta clave, o un mensaje especial indicando que la misma
		*	no se pudo resolver (porque el canal estaba cerrado).
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

		/**
		* <p>Indica si el canal de la clave está activa para el
		* evento <b>ACCEPT</b>, es decir, que el canal se encuentra
		* a disposición de conexiones entrantes, tal cual lo
		* hace un <i>ServerSocketChannel</i> (listener).</p>
		*
		* @param key
		*	La clave para la cual se determinará su funcionalidad.
		*
		* @return Devuelve <i>true</i> si la clave representa un
		*	socket de escucha (<i>ServerSocketChannel</i>).
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

		/**
		* <p>Separa el monitor de inactividad en un thread
		* secundario, lo que reduce la latencia en el bucle
		* principal de selección.</p>
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
