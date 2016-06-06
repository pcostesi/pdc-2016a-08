
	package ar.edu.itba.protos.transport.support;

		/**
		* Se definen todos los mensajes que el servidor puede
		* emitir durante su ejecución, o durante la presencia de
		* un estado de error.
		*/

	public enum Message {

		/*
		** Static POP/3 Proxy Greeting-Banner:
		*/

		GREETING_BANNER
			("+OK POP-3 Proxy Server (ready).\r\n"),

		/*
		** Errores asociados a la exposición de un servidor:
		*/

		CANNOT_RAISE
			("No se pudo iniciar el servidor"),
		CANNOT_BIND
			("La dirección especificada ya se está usando"),
		UNRESOLVED_ADDRESS
			("La dirección especificada no se pudo resolver"),
		CANNOT_LISTEN
			("No se pudo agregar el nuevo 'listener'"),

		/*
		** Errores asociados a la clase 'ReadHandler'
		*/

		CLIENT_UNPLUGGED
			("El cliente se desconectó sorpresivamente"),

		/*
		** Errores asociados a la clase 'WriteHandler'
		*/

		SERVER_UNPLUGGED
			("El servidor se desconectó sorpresivamente"),

		/*
		** Errores asociados a la clase 'ConnectHandler':
		*/

		CLOSED_PORT
			("El puerto destino (origin-server) parece estar cerrado"),

		/*
		** Errores asociados a la clase 'ClientAttachment':
		*/

		CANNOT_FORWARD
			("No se puede conectar con el 'origin-server'"),

		/*
		** Error desconocido:
		*/

		UNKNOWN
			("Se produjo un error desconocido");

		// El texto que identifica el contenido del mensaje:
		private final String message;

		private Message(final String message) {

			this.message = message;
		}

		/*
		** Getter's
		*/

		public String getMessage() {

			return message;
		}

		@Override
		public String toString() {

			return getMessage();
		}
	}
