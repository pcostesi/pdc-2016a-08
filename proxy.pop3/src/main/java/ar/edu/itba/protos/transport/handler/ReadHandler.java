
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Synchronizer;

		/**
		* <p>Se encarga de procesar los eventos de lectura.
		* Estos suceden cuando, una vez establecido un
		* circuito de conexión, uno de los extremos de dicho
		* circuito comienza a emitir un flujo de bytes.</p>
		*
		* <p>Esta clase es <b>thread-safe</b>.</p>
		*/

	@Singleton
	public final class ReadHandler implements Handler {

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(ReadHandler.class);

		// Esta constante indica que el stream se ha cerrado:
		private static final int BROKEN_PIPE = -1;

		// Repositorio global de claves:
		private final Synchronizer sync;

		@Inject
		private ReadHandler(final Synchronizer sync) {

			this.sync = sync;
		}

		public void onSubmit(SelectionKey key) {

			SelectionKey upstream
				= ((Attachment) key.attachment()).getUpstream();

			if (key != upstream && upstream != null)
				sync.save(upstream);
		}

		public void onResume(SelectionKey key) {

			SelectionKey upstream
				= ((Attachment) key.attachment()).getUpstream();

			if (key != upstream && upstream != null)
				sync.restore(upstream);
		}

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
					if (attachment.hasFullInbound())
						sync.disable(key, Event.READ);
				}
				else throw new IOException();
			}
			catch (IOException exception) {

				// Elimino la clave del repositorio:
				sync.delete(key);

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

				if (upstream != null)
					sync.enable(upstream, Event.WRITE);
			}
		}
	}
