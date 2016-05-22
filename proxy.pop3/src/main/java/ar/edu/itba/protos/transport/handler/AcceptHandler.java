
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.ServerSocketChannel;
	import java.nio.channels.SocketChannel;

	import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;
	import ar.edu.itba.protos.transport.support.Message;

		/**
		* Se encarga de procesar los eventos de aceptación.
		* Estos suceden cuando un cliente establece conexión
		* en alguna de las direcciones y puertos de escucha de
		* alguno de los servidores que están utilizando el mismo
		* reactor al cual este 'handler' está subscripto.
		*/

	public final class AcceptHandler implements Handler {

		// Este objeto permite instalar 'attachments':
		private AttachmentFactory factory = null;

		public AcceptHandler(AttachmentFactory factory) {

			if (factory != null) this.factory = factory;
			else {

				// En caso de que no exista una fábrica:
				this.factory = new AttachmentFactory() {

					public Attachment create(SocketChannel socket) {

						return null;
					}
				};
			}
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
					InetSocketAddress remote = getRemote(socket);
					socket.configureBlocking(false);
					socket.register(
						key.selector(),
						SelectionKey.OP_READ,
						factory.create(socket));

					/**/System.out.println(
							"La conexión fue aceptada correctamente");
					/**/System.out.println(
							"\t(IP, Port) = (" + remote.getHostString() +
							", " + remote.getPort() + ")");
				}
				else throw new IOException();
			}
			catch (IOException exception) {

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
