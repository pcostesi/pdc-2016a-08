
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

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

		// Representa el decodificador del flujo de bytes entrante:
		private Interceptor interceptor = null;

		public ReadHandler(Interceptor interceptor) {

			if (interceptor != null) this.interceptor = interceptor;
			else {

				// El interceptor, por defecto, no hace nada:
				this.interceptor = new Interceptor() {

					public void consume(ByteBuffer buffer) {

						return;
					}
				};
			}
		}

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de lectura del flujo de bytes.
		*/

		public void handle(SelectionKey key) {

			/**/System.out.println("> Read (" + key + ")");

			// Variables utilizadas, por simplicidad:
			Attachment attachment = (Attachment) key.attachment();
			SocketChannel socket = attachment.getSocket();
			ByteBuffer buffer = attachment.getBuffer();

			try {

				if (BROKEN_PIPE < socket.read(buffer)) {

					buffer.flip();
					/**/System.out.println("Reading: " +
							buffer.remaining() + " byte's");

					// Consumo el flujo de bytes entrante:
					interceptor.consume(buffer);

					// Si hay información para enviar, habilito escritura:
					if (buffer.hasRemaining()) enableWrite(key);
				}
				else {

					key.cancel();
					socket.close();
					/**/System.out.println("El host remoto se ha desconectado");

					// De que lado se cerró? Debemos cerrar el otro extremo!!!
				}
			}
			catch (IOException exception) {

				System.out.println(Message.UNKNOWN);
			}
		}

		/*
		** Habilita la escritura en el canal especificado. Es
		** importante notar que el resto de opciones permanece
		** sin modificación. Si el canal ya estaba habilitado
		** para escritura, este método no produce cambios.
		*/

		private void enableWrite(SelectionKey key) {

			key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
		}
	}
