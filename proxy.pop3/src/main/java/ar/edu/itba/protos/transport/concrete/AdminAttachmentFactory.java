
	package ar.edu.itba.protos.transport.concrete;

	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;

		/**
		* El objetivo de esta fábrica es generar los 'attachment'
		* asociados a conexiones de administración, es decir, a las
		* conexiones hacia este servidor, cuyo objetivo es modificar
		* los parámetros de configuración del mismo, o bien, obtener
		* las estadísticas y métricas del estado actual del sistema.
		*/

	public final class AdminAttachmentFactory implements AttachmentFactory {

		// TODO: Debería obtenerse por configuración:
		public static final int BUFFER_SIZE = 8192;

		public Attachment create() {

			return new AdminAttachment();
		}
	}
