
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Message;

		/**
		* Este 'handler' es el encargado de forwardear la información
		* a través de los circuitos establecidos. En primer lugar,
		* recibe la información del cliente, y la envía hacia el servidor
		* origen. En segundo lugar, envía las respuestas de este último
		* devuelta al cliente. Además, se encarga de gestionar las
		* situaciones de error.
		*/

	public final class WriteHandler implements Handler {

		// Logger:
		private final static Logger logger
			= LoggerFactory.getLogger(WriteHandler.class);

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de escritura del flujo de bytes.
		*/

		public void handle(SelectionKey key) {

			logger.debug("Write ({})", key);

			Attachment attachment = (Attachment) key.attachment();
			ByteBuffer buffer = attachment.getOutboundBuffer();
			SocketChannel socket = attachment.getSocket();

			// El buffer estaba lleno?
			boolean full = false;

			try {

				// Si no hay espacio, hay que rehabilitar la lectura:
				if (buffer.position() == buffer.limit()) full = true;

				// Veo qué hay para enviar:
				buffer.flip();

				// Enviar un flujo de datos:
				int written = socket.write(buffer);

				// Si se envió todo el flujo, deshabilitar escritura:
				if (!attachment.hasOutboundData()) {

					Event.disable(key, SelectionKey.OP_WRITE);
				}

				// Si logró enviar datos:
				if (0 < written) {

					// Liberar espacio para lectura (inbound):
					buffer.compact();

					// Si estaba lleno y se vació algo, habilito lectura:
					if (full) Event.enable(key, SelectionKey.OP_READ);
				}
			}
			catch (IOException exception) {

				logger.error(
					"Handle failed with code {}",
					Message.SERVER_UNPLUGGED,
					exception);

				// Desconecto el 'downstream':
				attachment.closeDownstream();
				attachment.setDownstream(null);
				attachment.onUnplug(Event.WRITE);

				// Si hay información para enviar, abro el 'upstream':
				detectInbound(attachment);
			}
		}

		/*
		** En caso de que el 'attachment' posea información disponible
		** para enviar (en el buffer 'inbound'), habilita el canal de
		** escritura en el 'upstream'.
		*/

		private void detectInbound(Attachment attachment) {

			if (attachment.hasInboundData()) {

				SelectionKey upstream = attachment.getUpstream();
				Event.enable(upstream, SelectionKey.OP_WRITE);
			}
		}
	}
