
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
		* Se encarga de procesar los eventos de lectura.
		* Estos suceden cuando, una vez establecido un
		* circuito de conexión, uno de los extremos de dicho
		* circuito comienza a emitir un flujo de bytes.
		*/

	public final class ReadHandler implements Handler {

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(ReadHandler.class);

		// Esta constante indica que el stream se ha cerrado:
		private static final int BROKEN_PIPE = -1;

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de lectura del flujo de bytes.
		*/

		public void handle(SelectionKey key) {

			logger.debug("Read ({})", key);

			Attachment attachment = (Attachment) key.attachment();
			ByteBuffer buffer = attachment.getInboundBuffer();
			SocketChannel socket = attachment.getSocket();

			try {

				// Recuerdo donde estaba antes de leer:
				int position = buffer.position();

				if (BROKEN_PIPE < socket.read(buffer)) {

					// Realiza un 'flip' acotado al nuevo flujo:
					buffer.limit(buffer.position());
					buffer.position(position);

					// Consumo el flujo de bytes entrante:
					attachment.getInterceptor().consume(buffer);

					// Backtracking (no recuerda el límite):
					buffer.position(position);

					// Abrir el 'upstream' si hay algo nuevo:
					detectInbound(attachment);

					// Habilito más espacio para lectura:
					buffer.position(buffer.limit());
					buffer.limit(buffer.capacity());

					// Si el buffer se llenó, ya no se puede leer:
					if (attachment.hasFullInbound()) {

						Event.disable(key, SelectionKey.OP_READ);
					}
				}
				else throw new IOException();
			}
			catch (IOException exception) {

				logger.error(
					"Handling message failed with code {}",
					Message.CLIENT_UNPLUGGED,
					exception);

				// Desconecto el 'downstream':
				attachment.closeDownstream();
				attachment.setDownstream(null);
				attachment.onUnplug(Event.READ);

				// Si hay información para enviar, abro el 'upstream':
				detectInbound(attachment);
			}
		}

		/*
		** En caso de que el 'attachment' posea información
		** disponible para enviar (en el buffer 'inbound'),
		** habilita el canal de escritura en el 'upstream'.
		*/

		private void detectInbound(Attachment attachment) {

			if (attachment.hasInboundData()) {

				SelectionKey upstream = attachment.getUpstream();
				Event.enable(upstream, SelectionKey.OP_WRITE);
			}
		}
	}
