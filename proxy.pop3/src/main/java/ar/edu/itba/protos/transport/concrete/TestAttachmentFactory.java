
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

		// Mensaje generado durante una desconexión:
		private static final String greetingBanner
			= ">> Test Server\n";

		// Mensaje generado durante una desconexión:
		private static final String unplugMessage
			= "test.Unplug()";

		@Override
		public Attachment create() {

			return new TestAttachment();
		}

		private class TestAttachment extends Attachment {

			// El buffer utilizado:
			private ByteBuffer buffer
				= ByteBuffer.allocate(BUFFER_SIZE);

			public TestAttachment() {

				buffer.clear();
				buffer.put(greetingBanner.getBytes());
			}

			@Override
			public ByteBuffer getInboundBuffer() {

				return buffer;
			}

			@Override
			public ByteBuffer getOutboundBuffer() {

				return buffer;
			}

			@Override
			public int getInitialOptions() {

				return Event.READ.getOptions()
					| Event.WRITE.getOptions();
			}

			@Override
			public void setDownstream(SelectionKey downstream) {

				super.setDownstream(downstream);
				setUpstream(downstream);
			}

			@Override
			public boolean hasOutboundData() {

				boolean hasData = super.hasOutboundData();
				if (0 < buffer.position()) buffer.compact();
				return hasData;
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

				buffer.clear();
				buffer.put(unplugMessage.getBytes());
				closeUpstream();
			}
		};
	}
