
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Message;

		/**
		* Este 'handler' es el encargado de forwardear la información
		* a través de los circuitos establecidos. En primer lugar,
		* recibe la información del cliente, y la envía hacia el
		* servidor origen. En segundo lugar, envía las respuestas de
		* este último devuelta al cliente. Además, se encarga de
		* gestionar las situaciones de error.
		*/

	public final class WriteHandler implements Handler {

		public void handle(SelectionKey key) {

			/**/System.out.println("> Write (" + key + ")");

			Attachment attachment = (Attachment) key.attachment();
			ByteBuffer buffer = attachment.getOutboundBuffer();
			SocketChannel socket = attachment.getSocket();

			try {

				// Enviar un flujo de datos:
				int written = socket.write(buffer);

				// Si se envió todo el flujo, deshabilitar escritura:
				if (!attachment.hasOutboundData()) {

					disableWrite(key);
				}

				// Liberar espacio para una lectura (inbound):
				if (0 < written) buffer.compact();
			}
			catch (IOException exception) {

				// El canal se cerró.
				System.out.println(Message.UNKNOWN);
			}
		}

		/*
		** En caso de que todo el flujo de bytes disponible se
		** haya transferido por completo, se deshabilitan los
		** eventos de escritura para este canal.
		*/

		private void disableWrite(SelectionKey key) {

			key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
		}
	}
