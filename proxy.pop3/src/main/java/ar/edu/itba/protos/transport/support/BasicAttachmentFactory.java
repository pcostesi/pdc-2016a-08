
	package ar.edu.itba.protos.transport.support;

	import java.nio.ByteBuffer;
	import java.nio.channels.SocketChannel;

		/**
		* Esta implementación de 'AttachmentFactory', genera
		* instancias que poseen un buffer de tamaño fijo. En
		* este caso, el buffer es de 4 Kb.
		*/

	public final class BasicAttachmentFactory implements AttachmentFactory {

		// Tamaño máximo del buffer (capacidad):
		public static final int BUFFER_SIZE = 4096;

		public BasicAttachmentFactory() {

			return;
		}

		public Attachment create(SocketChannel socket) {

			return new Attachment(socket) {

				// El buffer de bytes:
				private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

				@Override
				public ByteBuffer getBuffer() {

					return buffer;
				}
			};
		}
	}
