
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
	* <p>Para cada canal creado (a excepción de los sockets de
	* escucha), se instancia un objeto de esta clase, el cual
	* mantiene la información asociada a ese canal de información.</p>
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

		/**
		* <p>Devuelve el buffer interno que se usa para realizar IO
		* sobre el stream de bytes de entrada (inbound). Es
		* necesario que el mismo se acceda a través de un método
		* debido a que de esta forma se desacopla el mecanismo por
		* el cual se obtiene dicho buffer (el tamaño del buffer
		* podría variar entre cada llamada a este <i>getter</i>). Este
		* buffer se utiliza durante una lectura en el canal asociado.</p>
		*
		* @return El siguiente buffer de entrada, utilizado durante una
		*	operación de lectura desde un canal.
		*/

		public abstract ByteBuffer getInboundBuffer();

		/**
		* <p>En este caso, se devuelve el buffer de salida del flujo
		* de datos (outbound). El buffer de salida es el que se
		* utiliza durante una escritura en el canal asociado.</p>
		*
		* @return El siguiente buffer de salida, utilizado durante una
		*	operación de escritura en un canal.
		*/

		public abstract ByteBuffer getOutboundBuffer();

		/**
		* <p>Este método es llamado cada vez que el canal asociado a
		* este <i>attachment</i> se cierra y permite aplicar un post-proceso
		* adicional (por ejemplo, llenar el buffer de salida con algún
		* mensaje de error). Se espera que este método manipule
		* libremente el contenido del buffer <i>inbound</i>.</p>
		*
		* @param event
		*	El evento durante el cual se tuvo que ejecutar este método.
		*/

		public abstract void onUnplug(Event event);

		/**
		* <p>Intenta determinar la dirección remota a la cual este canal
		* está asociado.</p>
		*
		* @return Devuelve la dirección asociada a este canal, o <b>null</b>
		* si no puede obtenerla.
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

		/**
		* <p>Entrega la clave asociada al canal sobre el cual este
		* <i>attachment</i> se encuentra instalado.</p>
		*
		* @return Devuelve la clave de selección de este mismo canal.
		*/

		public SelectionKey getDownstream() {

			return downstream;
		}

		/**
		* <p>Entrega la clave asociada al canal de forwarding, si es
		* que existe alguno hasta el momento. En algunos casos, donde
		* no es necesario establecer una segunda conexión remota de
		* forwarding, esta clave es equivalente a <b>downstream</b>.</p>
		*
		* @return Devuelve la clave del servidor destino (upstream).
		*/

		public SelectionKey getUpstream() {

			return upstream;
		}

		/**
		* <p>Cuando este <i>attachment</i> es creado y asociado a un canal,
		* se utiliza este método para determinar el estado inicial
		* del mismo, es decir, se indican los eventos para los
		* cuales va a responder. Por defecto se habilita la lectura
		* para todos los canales nuevos.</p>
		*
		* <p>Es de esperarse que, si se requiere el envío de un
		* <b>greeting-banner</b> inicial, se habilite la escritura, además
		* de la disponibilidad para lectura.</p>
		*
		* @return Devuelve las opciones iniciales a las que este canal
		*	debe responder luego de que su conexión fue establecida.
		*/

		public int getInitialOptions() {

			return Event.READ.getOptions();
		}

		/**
		* <p>Devuelve el hostname del stream o su dirección IP.
		* Este método no aplica ningún tipo de <b>reverse-lookup</b>,
		* y por lo tanto es más eficiente. Si la dirección es
		* inválida devuelve una cadena vacía.</p>
		*
		* @return La cadena que identifica el host-name.
		*/

		public String getHost() {

			InetSocketAddress address = getAddress();
			if (address != null) {

				return address.getHostString();
			}
			return "";
		}

		/**
		* <p>Devuelve el procesador o <b>interceptor</b> del flujo de bytes
		* de entrada (inbound). Este método puede redefinirse en la
		* subclase (en lugar de definirlo concretamente para esta
		* clase), debido a que permite más libertad en el diseño de
		* este componente, por ejemplo, permitiendo que el <i>attachment</i>
		* sea al mismo tiempo quien procese la información (es decir,
		* que sea un interceptor).</p>
		*
		* <p>Por defecto, devuelve un interceptor que no hace nada.</p>
		*
		* @return El interceptor que debe utilizarse durante una operación
		*	de lectura, en el canal asociado (downstream).
		*/

		public Interceptor getInterceptor() {

			return Interceptor.DEFAULT;
		}

		/**
		* <p>Intenta obtener el puerto de la dirección remota del canal
		* asociado.</p>
		*
		* @return Devuelve el puerto del stream remoto de datos, o cero,
		*	si la dirección es inválida.
		*/

		public int getPort() {

			InetSocketAddress address = getAddress();
			if (address != null) {

				return address.getPort();
			}
			return 0;
		}

		/**
		* <p>Devuelve el stream de datos. Debido a que este canal
		* se representa por el <i>downstream</i>, el socket se debe
		* extraer de este flujo.</p>
		*
		* @return Devuelve el canal asociado a este <i>attachment</i>.
		*/

		public SocketChannel getSocket() {

			return (SocketChannel) downstream.channel();
		}

		/**
		* <p>Permite especificar en qué repositorio se almacenan las
		* claves de este <i>attachment</i>. El repositorio se puede
		* utilizar para manipular los canales <i>upstream</i> y
		* <i>downstream</i>.</p>
		*
		* @param sync
		*	El repositorio global de claves a utilizar.
		*/

		public void setSynchronizer(final Synchronizer sync) {

			this.sync = sync;
		}

		/**
		* <p>Setea la clave de selección de este mismo canal.</p>
		*
		* @param downstream
		*	La clave que representa a este canal.
		*/

		public void setDownstream(SelectionKey downstream) {

			this.downstream = downstream;
		}

		/**
		* <p>Setea la clave del servidor destino (upstream).</p>
		*
		* @param upstream
		*	La clave que representa el canal de forwarding.
		*/

		public void setUpstream(SelectionKey upstream) {

			this.upstream = upstream;
		}

		/**
		* <p>Este método permite identificar la situación en la que
		* se encuentra disponible más información en el buffer de
		* entrada, sea cual sea este buffer.</p>
		*
		* @return Devuelve <i>true</i>, si hay más información en el
		*	buffer de entrada (inbound).
		*/

		public boolean hasInboundData() {

			return getInboundBuffer().hasRemaining();
		}

		/**
		* <p>Especifica si hay más información para enviar, lo que
		* permite que la clave no se desubscriba del evento de
		* escritura.</p>
		*
		* @return Devuelve <i>true</i> si hay más información para
		*	enviar en este <i>attachment</i>.
		*/

		public boolean hasOutboundData() {

			return getOutboundBuffer().hasRemaining();
		}

		/**
		* <p>Especifica si el buffer de entrada se encuentra lleno.</p>
		*
		* @return Devuelve <i>true</i> si el buffer de entrada se llenó,
		*	o <i>false</i> en otro caso.
		*/

		public boolean hasFullInbound() {

			return !getInboundBuffer().hasRemaining();
		}

		/**
		* <p>Especifica si el buffer de salida se encuentra lleno.</p>
		*
		* @return Devuelve <i>true</i> si el buffer de salida se llenó,
		*	o <i>false</i> en otro caso.
		*/

		public boolean hasFullOutbound() {

			return !getOutboundBuffer().hasRemaining();
		}

		/**
		* <p>Especifica si el socket está conectado, es decir,
		* si el mismo puede utilizarse para transferir un
		* flujo de bytes entre sus extremos.</p>
		*
		* @return Devuelve <i>true</i> si el canal no se encuentra
		*	conectado en el otro extremo.
		*/

		public boolean isOnline() {

			SocketChannel socket = getSocket();
			return (socket != null) && socket.isConnected();
		}

		/**
		* <p>Permite establecer una nueva conexión remota, en
		* la dirección especificada, seleccionando además el
		* <i>attachment</i> que este canal debe tener asociado.</p>
		*
		* @param address
		*	La dirección remota a la cual conectarse.
		* @param attachment
		*	El <i>attachment</i> a instalar en la nueva conexión.
		*
		* @return El método devuelve la clave si pudo crear el canal, o
		*	<i>null</i>, si no pudo.
		*/

		public SelectionKey addStream(
			SocketAddress address, Attachment attachment) {

			try {

				SocketChannel socket = SocketChannel.open();

				// Registro el canal en el selector:
				SelectionKey key = socket
					.configureBlocking(false)
					.register(getDownstream().selector(), 0, attachment);

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

		/**
		* <p>Cierra el <b>downstream</b> de este <i>attachment</I>.
		* Además, cancela la clave asociada a ese canal.</p>
		*/

		public void closeDownstream() {

			close(downstream);
		}

		/**
		* <p>Cierra el <b>upstream</b> de este <i>attachment</I>.
		* Además, cancela la clave asociada a ese canal.</p>
		*/

		public void closeUpstream() {

			close(upstream);
		}

		/**
		* <p>Cierra el canal especificado, y su correspondiente
		* socket. Además cancela la clave asociada y elimina la
		* misma del repositorio global de claves.</p>
		*
		* @param stream
		*	La clave a cancelar, con su canal a cerrar.
		*/

		private void close(SelectionKey stream) {

			Server.close(stream);

			if (stream != null)
				sync.delete(stream);
		}
	}
