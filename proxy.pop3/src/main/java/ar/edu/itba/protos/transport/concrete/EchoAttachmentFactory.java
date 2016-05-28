
	package ar.edu.itba.protos.transport.concrete;

	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Interceptor;

		/**
		* Esta implementación de 'AttachmentFactory', genera
		* instancias que poseen un buffer de tamaño fijo. En
		* este caso, el buffer es de 4 Kb. El buffer se comparte
		* para entrada y salida, lo que provoca que el servidor
		* actúe como un eco (Echo Server).
		*/

	public final class EchoAttachmentFactory implements AttachmentFactory {

		// Tamaño máximo del buffer (capacidad):
		public static final int BUFFER_SIZE = 4096;

		public Attachment create() {

			// El mismo buffer para los 2 canales:
			final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

			Attachment newAttachment = new Attachment() {

				// El buffer de entrada (lectura):
				private ByteBuffer inbound = buffer;

				// El buffer de salida (escritura):
				private ByteBuffer outbound = buffer;

				@Override
				public ByteBuffer getInboundBuffer() {

					return inbound;
				}

				@Override
				public ByteBuffer getOutboundBuffer() {

					return outbound;
				}

				@Override
				public Interceptor getInterceptor() {

					return Interceptor.DEFAULT;
				}

				@Override
				public void onUnplug(Event event) {

					System.out.println("> onUnplug(" + event + ")");
				}

				@Override
				public void setDownstream(SelectionKey downstream) {

					// Hacemos un loop-back:
					super.setDownstream(downstream);
					setUpstream(downstream);
				}
			};

			// Un mensaje de bienvenida (greeting-banner):
			newAttachment.getOutboundBuffer().put("Hi!\n".getBytes());
			return newAttachment;
		}
	}
