
	package ar.edu.itba.protos.transport.concrete;

	import java.net.InetSocketAddress;
	import java.net.SocketAddress;
	import java.nio.ByteBuffer;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Interceptor;

		/**
		* Esta implementación de 'AttachmentFactory', genera
		* instancias que poseen un buffer de tamaño fijo. En
		* este caso, el buffer es de 4 Kb. El buffer posee el
		* mismo tamaño, tanto para entrada como para salida.
		* 
		* El objetivo de esta fábrica es generar 'forwarders',
		* es decir, circuitos que conectan clientes con servidores
		* especificados dentro del interceptor.
		*/

	public final class ForwardAttachmentFactory implements AttachmentFactory {

		// Tamaño máximo del buffer (capacidad):
		public static final int BUFFER_SIZE = 4096;

		public Attachment create() {

			Attachment newAttachment = new Attachment() {

				// El buffer de entrada (lectura):
				private ByteBuffer inbound = ByteBuffer.allocate(BUFFER_SIZE);

				// El buffer de salida (escritura):
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

					return new Interceptor() {

						public void consume(ByteBuffer buffer) {

							// El 'attachment' de tipo 'server-side':
							Attachment attach = new OriginServerAttachment(
								downstream, outbound, inbound);

							// El servidor remoto (origin-server):
							SocketAddress address = new InetSocketAddress(
								"192.168.2.2", 110);

							// Creo el stream 'server-side':
							upstream = addStream(address, attach);

							if (upstream == null)
								System.out.println("> No se pudo crear el STREAM");
						}
					};
				}

				@Override
				public void onUnplug(Event event) {

					System.out.println("> onUnplug(" + event + ")");

					// Un mensaje de despedida:
					inbound.put("Bye!\n".getBytes());
				}
			};

			// Un mensaje de bienvenida (greeting-banner):
			newAttachment.getOutboundBuffer().put("Hi!\n".getBytes());
			return newAttachment;
		}
	}
