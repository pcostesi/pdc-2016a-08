
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

	import ar.edu.itba.protos.transport.reactor.Event;
	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Interceptor;
	import ar.edu.itba.protos.transport.support.Message;

		/**
		* Se encarga de procesar los eventos de lectura.
		* Estos suceden cuando, una vez establecido un
		* circuito de conexión, uno de los extremos de dicho
		* circuito comienza a emitir un flujo de bytes.
		*/

	public final class ReadHandler implements Handler {

		// Esta constante indica que el stream se ha cerrado:
		private static final int BROKEN_PIPE = -1;

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de lectura del flujo de bytes.
		*/

		public void handle(SelectionKey key) {

			/**/System.out.println("> Read (" + key + ")");

			Attachment attachment = (Attachment) key.attachment();
			ByteBuffer buffer = attachment.getInboundBuffer();
			SocketChannel socket = attachment.getSocket();

			try {

				// La terna debería ser (0, C, C):
				/**/System.out.println("\tPos: " + buffer.position());
				/**/System.out.println("\tLim: " + buffer.limit());
				/**/System.out.println("\tRem: " + buffer.remaining());

				if (BROKEN_PIPE < socket.read(buffer)) {

					buffer.flip();
					/**/System.out.println("Reading: " +
							buffer.remaining() + " byte's");

					// Consumo el flujo de bytes entrante:
					Interceptor interceptor = getInterceptor(attachment);
					interceptor.consume(buffer);

					// La terna debería ser (0, n, n):
					/**/System.out.println("\tPos: " + buffer.position());
					/**/System.out.println("\tLim: " + buffer.limit());
					/**/System.out.println("\tRem: " + buffer.remaining());
				}
				else {

					/**/System.out.println("El host remoto se ha desconectado");

					key.cancel();
					socket.close();
					attachment.setDownstream(null);
					attachment.onUnplug(Event.READ);
				}

				// Si hay información para enviar, abro el 'upstream':
				if (attachment.hasInboundData()) {

					SelectionKey upstream = attachment.getUpstream();
					enableWrite(upstream);
				}
			}
			catch (IOException exception) {

				System.out.println(Message.UNKNOWN);
			}
		}

		/*
		** Habilita la operación de escritura en el canal especificado
		** lo que permite enviar un flujo de bytes hacia un host
		** destino (outbound).
		*/

		private void enableWrite(SelectionKey stream) {

			if (stream != null) {

				int options = stream.interestOps();
				stream.interestOps(options | SelectionKey.OP_WRITE);
			}
		}

		/*
		** Intenta obtener un 'interceptor' del 'attachment' sobre
		** el canal por el cual se está leyendo. Si no existe uno, se
		** devuelve uno por defecto (que no hace nada).
		*/

		private Interceptor getInterceptor(Attachment attachment) {

			Interceptor interceptor = attachment.getInterceptor();
			if (interceptor != null) return interceptor;
			else return Interceptor.DEFAULT;
		}
	}
