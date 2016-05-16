
	package ar.edu.itba.protos.transport;

	import java.nio.ByteBuffer;
	import java.nio.channels.SocketChannel;

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
