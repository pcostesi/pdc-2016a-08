
	package ar.edu.itba.protos.transport.concrete;

	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;

		/**
		* <p>Esta fábrica se utiliza para realizar testeos
		* sobre la capa de transporte. Permite monitorear
		* el flujo de bytes consumidos y despachados, asi
		* como también obtener ciertas métricas sobre los
		* mismos.</p>
		*/

	public final class TestAttachmentFactory implements AttachmentFactory {

		// El logger utilizado:
		private static final Logger logger
			= LoggerFactory.getLogger(TestAttachmentFactory.class);

		// Tamaño del buffer de testeo:
		private static final int BUFFER_SIZE = 8192;

		// Habilita el servidor de descarte:
		private static boolean DISCARD_MODE = false;

		// Envía el 'greeting-banner' inicial:
		private static boolean SEND_GREETING = false;

		// Mensaje generado durante una desconexión:
		private static final String greetingBanner
			= ">> Test Server\n";

		@Override
		public Attachment create() {

			return new TestAttachment();
		}

		private class TestAttachment extends Attachment {

			// El buffer utilizado:
			private ByteBuffer buffer
				= ByteBuffer.allocate(BUFFER_SIZE);

			// Indica si se activó la señal de cierre:
			private boolean signalUnplug = false;

			public TestAttachment() {

				buffer.clear();
				if (!DISCARD_MODE)
					if (SEND_GREETING)
						buffer.put(greetingBanner.getBytes());
			}

			@Override
			public ByteBuffer getInboundBuffer() {

				if (DISCARD_MODE) buffer.clear();
				return buffer;
			}

			@Override
			public ByteBuffer getOutboundBuffer() {

				return buffer;
			}

			@Override
			public int getInitialOptions() {

				if (DISCARD_MODE | !SEND_GREETING)
					return Event.READ.getOptions();
				else
					return Event.READ.getOptions()
						| Event.WRITE.getOptions();
			}

			@Override
			public boolean hasFullInbound() {

				if (DISCARD_MODE) return false;
				else return super.hasFullInbound();
			}

			@Override
			public void setDownstream(SelectionKey downstream) {

				super.setDownstream(downstream);
				setUpstream(downstream);
			}

			@Override
			public boolean hasInboundData() {

				if (DISCARD_MODE) return false;
				else return super.hasInboundData();
			}

			@Override
			public boolean hasOutboundData() {

				if (DISCARD_MODE) return false;
				else {

					if (signalUnplug) {

						closeDownstream();
						return false;
					}
					boolean hasData = super.hasOutboundData();
					if (0 < buffer.position()) buffer.compact();
					return hasData;
				}
			}

			@Override
			public void onUnplug(Event event) {

				logger.debug(
					Message.UNKNOWN.getMessage(),
					this.getClass().getSimpleName()
					+ ".onUnplug(" + event + ")");

				logger.info(
					Message.CLIENT_UNPLUGGED.getMessage(),
					Server.tryToResolveAddress(downstream));

				if (event == Event.READ)
					signalUnplug = true;
			}
		};
	}
