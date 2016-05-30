
package ar.edu.itba.protos.transport.concrete;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.transport.reactor.Event;
import ar.edu.itba.protos.transport.support.Attachment;
import ar.edu.itba.protos.transport.support.Interceptor;
import ar.edu.itba.protos.transport.support.Message;

/**
 * Este 'attachment' se crea cada vez que un cliente establece efectivamente la
 * conexión con un servidor de origen real. Bajo estas circustancias, el
 * circuito virtual a través del proxy estará completo, y el flujo de bytes será
 * forwardeado efectivamente en las dos direcciones.
 */

public final class ClientAttachment extends Attachment implements Interceptor {
	private final static Logger logger = LoggerFactory.getLogger(ClientAttachment.class);

	// El buffer de entrada (lectura):
	private ByteBuffer inbound = ByteBuffer.allocate(ForwardAttachmentFactory.BUFFER_SIZE);

	// El buffer de salida (escritura):
	private ByteBuffer outbound = ByteBuffer.allocate(ForwardAttachmentFactory.BUFFER_SIZE);

	public ClientAttachment() {
		// Este es el 'greeting-banner' (client-side):
		byte[] greetingBanner = "+OK POP-3 Proxy Server (ready).\r\n".getBytes();

		// Se lo envío al cliente (MUA):
		if (greetingBanner.length <= outbound.remaining())
			outbound.put(greetingBanner);
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

		logger.trace("> Client.onUnplug({})", event);

		// Vacío el buffer 'inbound':
		inbound.clear();

		// Un mensaje de despedida:
		inbound.put("(Client) Bye!\n".getBytes());

		// Fuerza el cierre del 'upstream':
		closeUpstream();
	}

	private boolean first = true;

	public void consume(ByteBuffer buffer) {

		if (first) {

			// Vacío el buffer antes de conectarme al 'origin-server':
			buffer.limit(buffer.position());

			// El 'attachment' de tipo 'server-side':
			Attachment attach = new ServerAttachment(downstream, outbound, inbound);

			// El servidor remoto (origin-server):
			SocketAddress address = new InetSocketAddress("pop.speedy.com.ar", 110);

			// Creo el stream 'server-side':
			upstream = addStream(address, attach);

			if (upstream == null) {

				// No se pudo resolver el 'origin-server':
				closeDownstream();

				logger.error(Message.CANNOT_FORWARD);
			}

			// Soló una vez:
			first = false;
		}
	}
}
