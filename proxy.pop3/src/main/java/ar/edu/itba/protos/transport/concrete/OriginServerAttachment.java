
	package ar.edu.itba.protos.transport.concrete;

	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Interceptor;

		/**
		* Este 'attachment' se crea cada vez que un cliente
		* establece efectivamente la conexión con un servidor de
		* origen real. Bajo estas circustancias, el circuito virtual
		* a través del proxy estará completo, y el flujo de bytes
		* será forwardeado efectivamente en las dos direcciones.
		*/

	public class OriginServerAttachment extends Attachment {

		// El buffer de entrada (lectura):
		private ByteBuffer inbound = null;

		// El buffer de salida (escritura):
		private ByteBuffer outbound = null;

		public OriginServerAttachment(
								SelectionKey upstream,
								ByteBuffer inbound,
								ByteBuffer outbound) {

			this.inbound = inbound;
			this.outbound = outbound;
			this.upstream = upstream;

			// Este es el 'greeting-banner' (server-side):
			this.outbound.put("(Server) Hi!\n".getBytes());
		}

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

			// Aquí se encuentra el interceptor 'server-side':
			return Interceptor.DEFAULT;
		}

		@Override
		public void onUnplug(Event event) {

			System.out.println("> onUnplug(" + event + ")");

			// Un mensaje de despedida:
			inbound.put("(Server) Bye!".getBytes());
		}
	}
