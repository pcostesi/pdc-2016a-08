
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

		/**
		* <p>En caso de que la clave posea un forwarder,
		* almacena el estado del mismo en el repositorio de
		* claves, siempre y cuando el downstream y el upstream
		* no representen el mismo canal.</p>
		*
		* @param key
		*	La clave a procesar.
		*/

		public void onSubmit(SelectionKey key) {

			SelectionKey upstream
				= ((Attachment) key.attachment()).getUpstream();

			if (key != upstream && upstream != null)
				sync.save(upstream);
		}

		/**
		* <p>Recupera el estado del canal de forwarding, en caso
		* de que exista uno. El canal downstream es recuperado
		* automáticamente por el núcleo de procesamiento.</p>
		*
		* @param key
		*	La clave a procesar.
		*/

		public void onResume(SelectionKey key) {

			SelectionKey upstream
				= ((Attachment) key.attachment()).getUpstream();

			if (key != upstream && upstream != null)
				sync.restore(upstream);
		}

		/**
		* <p>Se encarga de consumir el flujo de entrada para la
		* clave especificada, procesando el msimo a través del
		* interceptor obtenido del <i>attachment</i>. Adicionalmente
		* verifica y modifica los eventos para los cuales responde
		* este canal en función del estado de los buffers internos.</p>
		*
		* @param key
		*	La clave a procesar, en la cual se activó el
		*	evento <b>READ</b>.
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

		/**
		* <p>En caso de que el <i>attachment</i> posea información
		* disponible para enviar (en el buffer <i>inbound</i>),
		* habilita el canal de escritura en el <i>upstream</i>.</p>
		*
		* @param attachment
		*	El <i>attachment</i> asociado a la clave procesada, el cual
		*	determina el estado de los buffers internos.
		*/

		private void detectInbound(Attachment attachment) {

			if (attachment.hasInboundData()) {

				SelectionKey upstream = attachment.getUpstream();

				if (upstream != null)
					sync.enable(upstream, Event.WRITE);
			}
		}
	}
