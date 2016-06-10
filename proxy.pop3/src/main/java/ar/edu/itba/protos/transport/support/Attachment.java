
	package ar.edu.itba.protos.transport.support;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.net.SocketAddress;
	import java.nio.ByteBuffer;
	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;
	import java.nio.channels.UnresolvedAddressException;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import ar.edu.itba.protos.transport.reactor.Event;

	/**
	* Para cada canal creado (a excepción de los sockets de
	* escucha), se instancia un objeto de esta clase, el cual
	* mantiene la información asociada a ese canal de información.
	*/

	public abstract class Attachment {

		// Logger:
		private final static Logger logger
			= LoggerFactory.getLogger(Attachment.class);

		// Canal asociado a este 'attachment':
		protected SelectionKey downstream = null;

		// Canal asociado al servidor destino:
		protected SelectionKey upstream = null;

		// Repositorio global de claves:
		protected Synchronizer sync = null;

		/*
		** Devuelve el buffer interno que se usa para realizar IO
		** sobre el stream de bytes de entrada (inbound). Es
		** necesario que el mismo se acceda a través de un método
		** debido a que de esta forma se desacopla el mecanismo por
		** el cual se obtiene dicho buffer (el tamaño del buffer
		** podría variar entre cada llamada a este 'getter'). Este
		** buffer se utiliza durante una lectura en el canal asociado.
		*/

		public abstract ByteBuffer getInboundBuffer();

		/*
		** En este caso, se devuelve el buffer de salida del flujo
		** de datos (outbound). El buffer de salida es el que se
		** utiliza durante una escritura en el canal asociado.
		*/

		public abstract ByteBuffer getOutboundBuffer();

		/*
		** Este método es llamado cada vez que el canal asociado a
		** este 'attachment' se cierra y permite aplicar un post-proceso
		** adicional (por ejemplo, llenar el buffer de salida con algún
		** mensaje de error). Se espera que este método manipule
		** libremente el contenido del buffer 'inbound'.
		*/

		public abstract void onUnplug(Event event);

		/*
		** Devuelve la dirección asociada a este canal, o 'null'
		** si no puede obtenerla.
		*/

		public InetSocketAddress getAddress() {

			try {

				SocketChannel socket = getSocket();
				return (InetSocketAddress) socket.getRemoteAddress();
			}
			catch (IOException exception) {

				return null;
			}
		}

		/*
		** Devuelve la clave de selección de este mismo canal.
		*/

		public SelectionKey getDownstream() {

			return downstream;
		}

		/*
		** Devuelve la clave del servidor destino (upstream).
		*/

		public SelectionKey getUpstream() {

			return upstream;
		}

		/*
		** Cuando este 'attachment' es creado y asociado a un canal,
		** se utiliza este método para determinar el estado inicial
		** del mismo, es decir, se indican los eventos para los
		** cuales va a responder. Por defecto se habilita la lectura
		** para todos los canales nuevos.
		*/

		public int getInitialOptions() {

			return Event.READ.getOptions();
		}

		/*
		** Devuelve el hostname del stream o su dirección IP.
		** Este método no aplica ningún tipo de 'reverse-lookup',
		** y por lo tanto es más eficiente. Si la dirección es
		** inválida devuelve una cadena vacía.
		*/

		public String getHost() {

			InetSocketAddress address = getAddress();
			if (address != null) {

				return address.getHostString();
			}
			return "";
		}

		/*
		** Devuelve el procesador o 'interceptor' del flujo de bytes
		** de entrada (inbound). Este método puede redefinirse en la
		** subclase (en lugar de definirlo concretamente para esta
		** clase), debido a que permite más libertad en el diseño de
		** este componente, por ejemplo, permitiendo que el 'attachment'
		** sea al mismo tiempo quien procese la información (es decir,
		** que sea un interceptor).
		*/

		public Interceptor getInterceptor() {

			return Interceptor.DEFAULT;
		}

		/*
		** Devuelve el puerto del stream remoto de datos, o cero,
		** si la dirección es inválida.
		*/

		public int getPort() {

			InetSocketAddress address = getAddress();
			if (address != null) {

				return address.getPort();
			}
			return 0;
		}

		/*
		** Devuelve el stream de datos. Debido a que este canal
		** se representa por el 'downstream', el socket se debe
		** extraer de este flujo.
		*/

		public SocketChannel getSocket() {

			return (SocketChannel) downstream.channel();
		}

		/*
		** Permite especificar en qué repositorio se almacenan las
		** claves de este 'attachment'. El repositorio se puede
		** utilizar para manipular los canales 'upstream' y
		** 'downstream'.
		*/

		public void setSynchronizer(final Synchronizer sync) {

			this.sync = sync;
		}

		/*
		** Setea la clave de selección de este mismo canal.
		*/

		public void setDownstream(SelectionKey downstream) {

			this.downstream = downstream;
		}

		/*
		** Setea la clave del servidor destino (upstream).
		*/

		public void setUpstream(SelectionKey upstream) {

			this.upstream = upstream;
		}

		/*
		** Retorna 'true' si existe un flujo de bytes en
		** el buffer de entrada, el cual debe ser procesado,
		** antes de ser enviado.
		*/

		public boolean hasInboundData() {

			return getInboundBuffer().hasRemaining();
		}

		/*
		** Retorna 'true' si existe un flujo de bytes en el
		** buffer de salida, el cual debe ser enviado, hacia
		** el host remoto.
		*/

		public boolean hasOutboundData() {

			return getOutboundBuffer().hasRemaining();
		}

		/*
		** Devuelve 'true' si el buffer de entrada se llenó,
		** o 'false' en otro caso.
		*/

		public boolean hasFullInbound() {

			return !getInboundBuffer().hasRemaining();
		}

		/*
		** Devuelve 'true' si el buffer de salida se llenó,
		** o 'false' en otro caso.
		*/

		public boolean hasFullOutbound() {

			return !getOutboundBuffer().hasRemaining();
		}

		/*
		** Especifica si el socket está conectado, es decir,
		** si el mismo puede utilizarse para transferir un
		** flujo de bytes entre sus extremos.
		*/

		public boolean isOnline() {

			SocketChannel socket = getSocket();
			return (socket != null) && socket.isConnected();
		}

		/*
		** Permite establecer una nueva conexión remota, en
		** la dirección especificada, seleccionando además el
		** 'attachment' que este canal debe tener asociado.
		** El método devuelve 'true' si pudo crear el canal, o
		** 'false', si no pudo.
		*/

		public SelectionKey addStream(
			SocketAddress address, Attachment attachment) {

			try {

				SocketChannel socket = SocketChannel.open();

				// Registro el canal en el selector:
				SelectionKey key = socket
					.configureBlocking(false)
					.register(
						getDownstream().selector(),
						0,
						attachment);

				// Especifico la clave del nuevo stream:
				attachment.setDownstream(key);

				// El repositorio de claves usado:
				attachment.setSynchronizer(sync);

				// Almaceno el nuevo canal (y lo configuro):
				sync.save(key);
				sync.enable(key, Event.CONNECT);

				// NO TOCAR!!!!
				if (socket.connect(address)) {

					/* En este caso la conexión se efectuó
					** instantáneamente, y todavía no fue testeado
					** que sucede en este caso con las claves
					** seleccionadas.
					*/
				}
				else {

					/* Este es el caso que en general se espera,
					** en el cual se requiere una resolución por
					** DNS, lo cual es más lento. Luego se procesa
					** a través de ConnectHandler.
					*/
				}

				// SIEMPRE!!! devolver la clave...
				return key;
			}
			catch (UnresolvedAddressException exception) {

				logger.error(
					Message.UNRESOLVED_ADDRESS.getMessage(),
					address);
			}
			catch (CancelledKeyException
				| IOException exception) {

				logger.error(
					Message.UNKNOWN.getMessage(),
					this.getClass().getSimpleName());
			}
			return null;
		}

		/*
		** Cierra el 'downstream' de este 'attachment'. Además,
		** cancela la clave asociada a ese canal.
		*/

		public void closeDownstream() {

			close(downstream);
		}

		/*
		** Cierra el 'upstream' de este 'attachment'. Además,
		** cancela la clave asociada a ese canal.
		*/

		public void closeUpstream() {

			close(upstream);
		}

		/*
		** Cierra el canal especificado, y su correspondiente
		** socket. Además cancela la clave asociada.
		*/

		private void close(SelectionKey stream) {

			Server.close(stream);

			if (stream != null)
				sync.delete(stream);
		}
	}
