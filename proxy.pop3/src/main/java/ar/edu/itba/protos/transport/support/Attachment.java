
	package ar.edu.itba.protos.transport.support;

	import java.nio.ByteBuffer;
	import java.nio.channels.SocketChannel;

		/**
		* Para cada socket creado (a excepción de los sockets de
		* escucha), se instancia un objeto de esta clase, el cual
		* mantiene la información asociada a ese canal de información.
		*/

	public final class Attachment {

		private ByteBuffer buffer;
		private SocketChannel socket;

		public Attachment(SocketChannel socket, int bufferSize) {

			this.socket = socket;
			buffer = ByteBuffer.allocate(bufferSize);
		}

		public ByteBuffer getBuffer() {

			return buffer;
		}

		public SocketChannel getSocket() {

			return socket;
		}

		public boolean isOnline() {

			return socket.isConnected();
		}
	}
