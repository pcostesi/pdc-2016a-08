
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
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Synchronizer;

		/**
		* <p>Este 'handler' es el encargado de forwardear la información
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

		public void onSubmit(SelectionKey key) {

			Attachment attachment = (Attachment) key.attachment();
			SelectionKey upstream = attachment.getUpstream();

			if (upstream != null) {

				sync.save(upstream);
				sync.suspend(upstream);
			}
		}

		public void onResume(SelectionKey key) {

			Attachment attachment = (Attachment) key.attachment();
			SelectionKey upstream = attachment.getUpstream();

			if (upstream != null)
				sync.restore(upstream);
		}

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
				if (!attachment.hasOutboundData())
					sync.disable(key, Event.WRITE);

				// Si logró enviar datos y estaba lleno, habilito lectura:
				if (0 < written && full)
					sync.enable(key, Event.READ);
			}
			catch (IOException exception) {

				logger.error(
					"Handle failed with code {}",
					Message.SERVER_UNPLUGGED,
					exception);

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

		/*
		** En caso de que el 'attachment' posea información disponible
		** para enviar (en el buffer 'inbound'), habilita el canal de
		** escritura en el 'upstream'.
		*/

		private void detectInbound(Attachment attachment) {

			/*
			** Podemos modificar el estado de la clave
			** asociada al 'upstream', debido a que el método
			** 'onSubmit()', garantiza que la misma se encuentre
			** en el repositorio global.
			*/

			if (attachment.hasInboundData()) {

				SelectionKey upstream = attachment.getUpstream();
				sync.enable(upstream, Event.WRITE);
			}
		}
	}
