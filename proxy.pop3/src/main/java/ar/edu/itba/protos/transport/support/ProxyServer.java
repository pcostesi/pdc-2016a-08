
	package ar.edu.itba.protos.transport.support;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.ByteBuffer;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.SocketChannel;

	// Todo esto va a desaparecer!!!

	public final class ProxyServer {

		public void onConnect(SelectionKey key) throws IOException {

			System.out.println("> Connect");
			/*	Intentamos establecer una conexión hacia
			**	un servidor remoto (ya podemos transmitir).
			*/

			Attachment attachment = (Attachment) key.attachment();
			try {

				if (attachment.getSocket().finishConnect()) {

					System.out.println("Totalmente conectados al servidor remoto!");
					key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
				else System.out.println("No debería pasar nunca, pero no se terminó la conexión!");
			}
			catch (IOException exception) {

				System.out.println("No se pudo establecer la conexión. Hubo un error");
				exception.printStackTrace();
			}
		}

		public void onRead(SelectionKey key) throws IOException {

			System.out.println("> Read");
			/*	Leemos de un socket y determinamos hacia
			**	donde enviar dicho flujo de bytes.
			*/

			Attachment attachment = (Attachment) key.attachment();
			// No haría falta verificar que está online
			if (attachment.isOnline()) {

				System.out.println("Ya se puede leer del buffer");

				ByteBuffer buffer = attachment.getBuffer();
				buffer.clear();
				if (-1 < attachment.getSocket().read(buffer)) {

					buffer.flip();
					System.out.println("Reading: " + buffer.remaining() + " byte's");
				}
				else {

					System.out.println("El host remoto se ha desconectado");
					attachment.getSocket().close();
				}
			}
			else {

				/* En realidad entrás acá cuando sabés que tenés un mensaje armado,
				** es decir, que todo esto depende del protocolo implementado.
				*/
				InetSocketAddress address = new InetSocketAddress("190.55.61.237", 80);
				if (address.isUnresolved()) {

					// Por DNS tarda más y siempre va a saltar esto...
					System.out.println("No se puede resolver la dirección");
				}
				else {

					// Una vez que se pueda determinar el host destino...
					SocketChannel socket = SocketChannel.open();
					SelectionKey outboundKey = null;//addSocket(socket, SelectionKey.OP_CONNECT);
					if (socket.connect(address)) {

						System.out.println("Conexión remota instantánea!");

						// Forwarding! (finalizamos la conexión)
						onConnect(outboundKey);
					}
					else {

						System.out.println("La conexión (outbound) no ha finalizado");
					}

					System.out.println("Intentando acceder al host destino");
				}
			}
		}
	}
