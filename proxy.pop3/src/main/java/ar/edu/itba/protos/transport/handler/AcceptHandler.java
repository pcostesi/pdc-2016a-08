
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.ServerSocketChannel;
	import java.nio.channels.SocketChannel;

	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Message;

		/**
		* Se encarga de procesar los eventos de aceptación.
		* Estos suceden cuando un cliente establece conexión
		* en alguna de las direcciones y puertos de escucha de
		* alguno de los servidores que están utilizando el mismo
		* reactor al cual este 'handler' está subscripto.
		*/

	public final class AcceptHandler implements Handler {

		public AcceptHandler() {

			return;
		}

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de aceptación.
		*/

		public void handle(SelectionKey key) {

			/**/System.out.println("> Accept (" + key + ")");

			try {

				// Establecemos la nueva conexión entrante:
				ServerSocketChannel server = getServer(key);
				SocketChannel socket = server.accept();

				if (socket != null) {

					// Registro el nuevo cliente y sus datos:
					socket.configureBlocking(false);
					socket.register(
							key.selector(),
							SelectionKey.OP_READ,
							new Attachment(socket, 4096));			// Más genérico!!!
					InetSocketAddress remote = getRemote(socket);

					/**/System.out.println(
							"La conexión fue aceptada correctamente");
					/**/System.out.println(
							"\t(IP, Port) = (" + remote.getHostString() +
							", " + remote.getPort() + ")");
				}
				else throw new IOException();
			}
			catch (IOException exception) {

				exception.printStackTrace();
				System.out.println(Message.UNKNOWN);
			}
		}

		/*
		** Obtiene la dirección remota del cliente conectado,
		** es decir, su dirección IP y su puerto de salida.
		*/

		private InetSocketAddress getRemote(SocketChannel socket)
			throws IOException {

			return (InetSocketAddress) socket.getRemoteAddress();
		}

		/*
		** Para la clave del evento recibido, se extrae el
		** ServerSocketChannel correspondiente.
		*/

		private ServerSocketChannel getServer(SelectionKey key) {

			return (ServerSocketChannel) key.channel();
		}
	}
