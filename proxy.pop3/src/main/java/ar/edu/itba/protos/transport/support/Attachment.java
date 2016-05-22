
	package ar.edu.itba.protos.transport.support;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.ByteBuffer;
	import java.nio.channels.SocketChannel;

		/**
		* Para cada socket creado (a excepción de los sockets de
		* escucha), se instancia un objeto de esta clase, el cual
		* mantiene la información asociada a ese canal de información.
		*/

	public abstract class Attachment {

		// Dirección IP y puerto del stream:
		protected InetSocketAddress address = null;

		// Stream de datos asignado a este 'attachment':
		protected SocketChannel socket = null;

		public Attachment(SocketChannel socket) {

			this.socket = socket;
			if (socket != null) {

				try {

					address = (InetSocketAddress) socket.getRemoteAddress();
				}
				catch (IOException exception) {}
			}
		}

		/*
		** Devuelve el buffer interno que se usa para realizar IO
		** sobre el stream de bytes asignado (el socket). Es necesario
		** que el mismo se acceda a través de un método debido a que de
		** esta forma se desacopla el mecanismo por el cual se obtiene
		** dicho buffer (el tamaño del buffer podría variar entre cada
		** llamada a este 'getter').
		*/

		public abstract ByteBuffer getBuffer();

		/*
		** Devuelve el hostname del stream o su dirección IP. Este
		** método no aplica ningún tipo de 'reverse-lookup', y por lo
		** tanto es más eficiente. Si la dirección es inválida devuelve
		** una cadena vacía.
		*/

		public String getHost() {

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

			if (address != null) {

				return address.getPort();
			}
			return 0;
		}

		/*
		** Devuelve el stream de datos.
		*/

		public SocketChannel getSocket() {

			return socket;
		}

		/*
		** Especifica si el socket está conectado, es decir, si el
		** mismo puede utilizarse para transferir un flujo de bytes
		** entre sus extremos.
		*/

		public boolean isOnline() {

			return (socket != null) && socket.isConnected();
		}
	}
