
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

	public class ServerAttachment extends Attachment
									implements Interceptor {

		// El 'attachment' del 'upstream':
		private Attachment upstreamAttachment;

		public ServerAttachment(SelectionKey upstream) {

			// Cierro el circuito virtual:
			this.upstream = upstream;

			// Extraigo el 'attachment' del lado del cliente:
			upstreamAttachment = (Attachment) upstream.attachment();
		}

		@Override
		public ByteBuffer getInboundBuffer() {

			return upstreamAttachment.getOutboundBuffer();
		}

		@Override
		public ByteBuffer getOutboundBuffer() {

			return upstreamAttachment.getInboundBuffer();
		}

		@Override
		public Interceptor getInterceptor() {

			return this;
		}

		@Override
		public void onUnplug(Event event) {

			/**/System.out.println("Server.onUnplug(" + event + ")");

			// Vacío el buffer 'inbound':
			getInboundBuffer().clear();

			// Un mensaje de despedida:
			getInboundBuffer().put("(Server) Bye!".getBytes());

			// Fuerzo el cierre del cliente:
			closeUpstream();
		}

		public void consume(ByteBuffer buffer) {

			// Esto no va, hay que hacer algo con el buffer:
			super.getInterceptor().consume(buffer);
		}
	}
