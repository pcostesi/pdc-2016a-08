
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
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

		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de aceptación.
		*/

		public void handle(SelectionKey key) {

			/**/System.out.println("> Accept (" + key + ")");

			try {

				// Establecemos la nueva conexión entrante:
				SocketChannel socket = getServer(key).accept();

				if (socket != null) {

					// Fabrico un nuevo 'attachment':
					Attachment attachment = getFactory(key).create();

					// Registro el nuevo cliente y sus datos:
					SelectionKey downstream = socket
						.configureBlocking(false)
						.register(
							key.selector(),
							getOptions(attachment),
							attachment);

					// Especifico el flujo que identifica este canal:
					attachment.setDownstream(downstream);
				}
				else throw new IOException();
			}
			catch (IOException exception) {

				System.out.println(Message.UNKNOWN);
			}
		}

		/*
		** Permite obtener la fábrica almacenada en el canal
		** asociado al ServerSocket que aceptó la conexión.
		*/

		private AttachmentFactory getFactory(SelectionKey key) {

			AttachmentFactory factory = (AttachmentFactory) key.attachment();
			if (factory != null) return factory;
			else return AttachmentFactory.DEFAULT;
		}

		/*
		** Determina los eventos iniciales a los que este canal
		** debe responder en función del estado por defecto del
		** 'attachment', el cual es obtenido inmediatamente luego
		** crear el mismo a través de una fábrica (AttachmentFactory).
		*/

		private int getOptions(Attachment attachment) {

			int options = SelectionKey.OP_READ;
			if (attachment != null) {

				attachment.getOutboundBuffer().flip();
				if (attachment.hasOutboundData()) {

					options |= SelectionKey.OP_WRITE;
				}
				attachment.getOutboundBuffer().compact();
			}
			return options;
		}

		/*
		** Para la clave del evento recibido, se extrae el
		** ServerSocketChannel correspondiente.
		*/

		private ServerSocketChannel getServer(SelectionKey key) {

			return (ServerSocketChannel) key.channel();
		}
	}
