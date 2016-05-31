
	package ar.edu.itba.protos.transport.handler;

	import java.io.IOException;
	import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.transport.reactor.Handler;
	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.Message;

		/**
		* Se encarga de establecer una conexión con el 'origin-server',
		* por la cual el circuito cliente-servidor quedará establecido.
		* Para determinar el 'origin-server' es necesario pre-procesar una
		* cantidad mínima del flujo de bytes desde el cliente, para poder
		* determinar hacia donde demultiplexar la conexión.
		*/

	public final class ConnectHandler implements Handler {
		private final static Logger logger = LoggerFactory.getLogger(ConnectHandler.class);
		
		/*
		** Procesa el evento para el cual está subscripto. En este
		** caso, el evento es de conexión saliente establecida.
		*/
		public void handle(SelectionKey key) {
			logger.debug("> Connect ({})", key);
			Attachment attachment = (Attachment) key.attachment();

			try {
				// Finalizo la conexión remota:
				if (attachment.getSocket().finishConnect()) {
					logger.debug("Connection established on {}", this);
					// Selecciona los eventos a los cuáles responder:
					key.interestOps(getOptions(attachment));
				} else {
					logger.debug("Pending connection on {}", this);
				}
			}
			catch (IOException exception) {
				logger.error("Could not handle: {}", Message.CLOSED_PORT, exception);
			}
		}

		/*
		** Determina los eventos iniciales a los que este canal
		** debe responder en función del estado por defecto del
		** 'attachment', el cual es obtenido inmediatamente luego de
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
	}
