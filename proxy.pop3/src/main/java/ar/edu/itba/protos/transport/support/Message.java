
	package ar.edu.itba.protos.transport.support;

		/**
		* En esta interfaz se definen todos los mensajes que el servidor
		* puede emitir durante su ejecución, o durante la presencia de
		* un estado de error.
		*/

	public interface Message {

		// XXX: THIS SHOULD BE AN ENUM
		
		/*
		** Errores asociados a la exposición de un servidor:
		*/

		public static final String CANNOT_RAISE
			= "No se pudo iniciar el servidor";

		public static final String CANNOT_BIND
			= "La dirección especificada ya se está usando";

		public static final String UNRESOLVED_ADDRESS
			= "La dirección especificada no se pudo resolver";

		public static final String CANNOT_LISTEN
			= "No se pudo agregar el nuevo 'listener'";

		/*
		** Errores asociados a la clase 'ConnectHandler':
		*/

		public static final String CLOSED_PORT
			= "El puerto destino (origin-server) parece estar cerrado";

		/*
		** Errores asociados a la clase 'ClientAttachment':
		*/

		public static final String CANNOT_FORWARD
			= "No se puede conectar con el 'origin-server'";

		/*
		** Error desconocido:
		*/

		public static final String UNKNOWN
			= "Se produjo un error desconocido";
	}
