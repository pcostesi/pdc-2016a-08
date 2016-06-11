
	package ar.edu.itba.protos.transport.support;

		/**
		* <p>Se definen todos los mensajes que el servidor puede
		* emitir durante su ejecución, o durante la presencia de
		* un estado de error.</p>
		*/

	public enum Message {

		/*
		** Static POP/3 Proxy Greeting-Banner:
		*/

		GREETING_BANNER
			("+OK POP-3 Proxy Server (ready).\r\n"),

		/*
		** Mensajes informativos (no son errores):
		*/

		INCOMING_CONNECTION
			("Se aceptó una nueva conexión desde {} (sobre la interfaz {})."),
		SHUTDOWN_COMPLETE
			("El servidor se desconectó por completo. Adiós!"),
		SERVER_SHUTDOWN
			("El servidor se está cerrando."),
		TIMEOUT_TRIGGER
			("El umbral de inactividad fue actualizado a {} segundos."),
		KILL_BY_LAZY
			("Se detectó inactividad en {}. El canal fue cerrado."),

		/*
		** Errores internos generales:
		*/

		CANNOT_TERMINATE
			("No se pudo cancelar todas las tareas pendientes en el núcleo."),
		SHUTDOWN_INTERRUPTED
			("El sistema de subprocesos fue interrumpido durante el cierre."),
		UNEXPECTED_UNPLUG
			("Un canal se desconectó abruptamente ({})."),
		UNKNOWN_ADDRESS
			("dirección desconocida"),
		UNKNOWN_INTERFACES
			("No se pueden determinar las interfaces abiertas."),

		/*
		** Errores asociados a la exposición de un servidor:
		*/

		INVALID_ADDRESS
			("La dirección especificada es inválida ({})."),
		CANNOT_RAISE
			("Error interno. No se pudo levantar el servidor."),
		UNRESOLVED_ADDRESS
			("La dirección remota no se pudo resolver ({})."),
		CANNOT_LISTEN
			("Error de binding. No se pudo escuchar en la dirección {}."),

		/*
		** Errores asociados a la clase 'AcceptHandler':
		*/

		INTERFACE_DOWN
			("Una de las interfaces del servidor se cerró abruptamente ({})."),

		/*
		** Errores asociados a la clase 'ReadHandler':
		*/

		CLIENT_UNPLUGGED
			("Un cliente se desconectó abruptamente ({})."),

		/*
		** Errores asociados a la clase 'WriteHandler':
		*/

		SERVER_UNPLUGGED
			("El servidor se desconectó abruptamente ({})."),

		/*
		** Errores asociados a la clase 'ConnectHandler':
		*/

		CONNECTION_TIMEOUT
			("El intento de conexión tardó demasiado, y fue cancelado ({})."),
		CONNECTION_SUCCEED
			("Se estableció una nueva conexión remota ({})."),
		PENDING_CONNECTION
			("La conexión remota ({}), sigue pendiente."),

		/*
		** Errores asociados a la clase 'ClientAttachment':
		*/

		CANNOT_FORWARD
			("No se pudo conectar con el servidor remoto ({})."),

		/*
		** Error desconocido:
		*/

		UNKNOWN
			("Error desconocido (módulo {}). Contacte un Ingeniero.");

		// El texto que identifica el contenido del mensaje:
		private final String message;

		private Message(final String message) {

			this.message = message;
		}

		/*
		** Getter's
		*/

		/**
		* <p>Devuelve la cadena asociada a este mensaje.</p>
		*
		* @return El mensaje asociado, como <i>String</i>.
		*/

		public String getMessage() {

			return message;
		}

		/**
		* <p>Este método solo se incluye por completitud, pero
		* se comporta de forma equivalente a <i>getMessage()</i>.</p>
		*
		* @return La cadena asociada a este mensaje.
		*/

		@Override
		public String toString() {

			return getMessage();
		}
	}
