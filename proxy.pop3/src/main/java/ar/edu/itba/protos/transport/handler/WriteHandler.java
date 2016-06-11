
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
		* <p>Este <i>handler</i> es el encargado de forwardear la información
		* a través de los circuitos establecidos. En primer lugar,
		* recibe la información del cliente, y la envía hacia el servidor
		* origen. En segundo lugar, envía las respuestas de este último
		* devuelta al cliente. Además, se encarga de gestionar las
		* situaciones de error.</p>
		*
		* <p>Esta clase es <b>thread-safe</b>.</p>
		*/

	@Singleton
	public final class WriteHandler implements Handler {

		// Logger:
		private final static Logger logger
			= LoggerFactory.getLogger(WriteHandler.class);

		// Repositorio global de claves:
		private final Synchronizer sync;

		@Inject
		private WriteHandler(final Synchronizer sync) {

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
		* <p>Su función es enviar los datos (escribir) por el canal
		* especificado. Debido a que la información se procesa durante
		* una lectura, este método no realiza ninguna verificación
		* sobre el flujo de bytes saliente. Adicionalmente, actualiza
		* el estado de las claves que utiliza en el repositorio global.</p>
		*
		* @param key
		*	La clave a procesar, en la cual se activó el
		*	evento <b>WRITE</b>.
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
				if (!attachment.hasOutboundData())
					sync.disable(key, Event.WRITE);

				// Si logró enviar datos y estaba lleno, habilito lectura:
				if (0 < written && full)
					sync.enable(key, Event.READ);
			}
			catch (IOException exception) {

				// Elimino la clave del repositorio:
				sync.delete(key);

				// Desconecto el 'downstream':
				attachment.closeDownstream();
				attachment.setDownstream(null);
				attachment.onUnplug(Event.WRITE);

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
