
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

		// El buffer de entrada (lectura):
		private ByteBuffer inbound = null;

		// El buffer de salida (escritura):
		private ByteBuffer outbound = null;

		public ServerAttachment(
							SelectionKey upstream,
							ByteBuffer inbound,
							ByteBuffer outbound) {

			// Cierro el circuito virtual:
			this.inbound = inbound;
			this.outbound = outbound;
			this.upstream = upstream;
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

			return this;
		}

		@Override
		public void onUnplug(Event event) {

			/**/System.out.println("> Server.onUnplug(" + event + ")");

			// Vacío el buffer 'inbound':
			inbound.clear();

			// Un mensaje de despedida:
			inbound.put("(Server) Bye!".getBytes());

			// Fuerzo el cierre del cliente:
			closeUpstream();
		}

		public void consume(ByteBuffer buffer) {

			Interceptor.DEFAULT.consume(buffer);
		}
	}
