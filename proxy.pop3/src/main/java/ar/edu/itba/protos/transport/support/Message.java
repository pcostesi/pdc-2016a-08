
	package ar.edu.itba.protos.transport.support;

		/**
		** En esta interfaz se definen todos los mensajes que el servidor
		** puede emitir durante su ejecución, o durante la presencia de
		** un estado de error.
		*/

	public interface Message {

		public static final String CANNOT_RAISE = "No se pudo iniciar el servidor";
	}
