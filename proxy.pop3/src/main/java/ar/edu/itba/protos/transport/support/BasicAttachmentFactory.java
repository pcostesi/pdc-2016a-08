
	package ar.edu.itba.protos.transport.support;

	import java.nio.ByteBuffer;

	import ar.edu.itba.protos.transport.reactor.Event;

		/**
		* Esta implementación de 'AttachmentFactory', genera
		* instancias que poseen un buffer de tamaño fijo. En
		* este caso, el buffer es de 4 Kb. El buffer posee el
		* mismo tamaño, tanto para entrada como para salida.
		*/

	public final class BasicAttachmentFactory implements AttachmentFactory {

		// Tamaño máximo del buffer (capacidad):
		public static final int BUFFER_SIZE = 4096;

		public Attachment create() {

			return new Attachment() {

				// El buffer de entrada (lectura):
				private ByteBuffer inbound = ByteBuffer.allocate(BUFFER_SIZE);

				// El buffer de salida (esritura):
				private ByteBuffer outbound = ByteBuffer.allocate(BUFFER_SIZE);

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

					System.out.println("> onUnplug");
				}
			};
		}
	}
