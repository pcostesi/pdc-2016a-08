
	package ar.edu.itba.protos.transport.support;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

		/**
		* Para cada canal creado (a excepción de los sockets de
		* escucha), se instancia un objeto de esta clase, el cual
		* mantiene la información asociada a ese canal de información.
		*/

	public abstract class Attachment {

		// Canal asociado a este 'attachment':
		protected SelectionKey downstream = null;

		// Canal asociado al servidor destino:
		protected SelectionKey upstream = null;

		/*
		** Devuelve el buffer interno que se usa para realizar IO
		** sobre el stream de bytes de entrada (inbound). Es necesario
		** que el mismo se acceda a través de un método debido a que de
		** esta forma se desacopla el mecanismo por el cual se obtiene
		** dicho buffer (el tamaño del buffer podría variar entre cada
		** llamada a este 'getter'). Este buffer se utiliza durante una
		** lectura en el canal asociado.
		*/

		public abstract ByteBuffer getInboundBuffer();

		/*
		** En este caso, se devuelve el buffer de salida del flujo de
		** datos (outbound). El buffer de salida es el que se utiliza
		** durante una escritura en el canal asociado.
		*/

		public abstract ByteBuffer getOutboundBuffer();

		/*
		** Devuelve el procesador o 'interceptor' del flujo de bytes
		** de entrada (inbound). Este método se especifica como 'abstract'
		** (en lugar de definirlo concretamente para esta clase), debido
		** a que permite más libertad en el diseño de este componente, por
		** ejemplo, permitiendo que el 'attachment' sea al mismo tiempo
		** quien procese la información (es decir, que sea un interceptor).
		*/

		public abstract Interceptor getInterceptor();

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
		** Devuelve el hostname del stream o su dirección IP. Este
		** método no aplica ningún tipo de 'reverse-lookup', y por lo
		** tanto es más eficiente. Si la dirección es inválida devuelve
		** una cadena vacía.
		*/

		public String getHost() {

			InetSocketAddress address = getAddress();
			if (address != null) {

				return address.getHostString();
			}
			return "";
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
		** Retorna 'true' si existe un flujo de bytes
		** en el buffer de entrada, el cual debe ser procesado,
		** antes de ser enviado.
		*/

		public boolean hasInboundData() {

			return getInboundBuffer().hasRemaining();
		}

		/*
		** Retorna 'true' si existe un flujo de bytes
		** en el buffer de salida, el cual debe ser enviado,
		** hacia el host remoto.
		*/

		public boolean hasOutboundData() {

			return getOutboundBuffer().hasRemaining();
		}

		/*
		** Especifica si el socket está conectado, es decir, si el
		** mismo puede utilizarse para transferir un flujo de bytes
		** entre sus extremos.
		*/

		public boolean isOnline() {

			SocketChannel socket = getSocket();
			return (socket != null) && socket.isConnected();
		}
	}
