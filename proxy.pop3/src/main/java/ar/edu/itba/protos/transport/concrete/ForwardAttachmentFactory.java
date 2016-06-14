
	package ar.edu.itba.protos.transport.concrete;

	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;

		/**
		* <p>Esta implementación de <b>AttachmentFactory</b>, genera
		* instancias que poseen un buffer de tamaño fijo. En
		* este caso, el buffer es de 8 Kb. El buffer posee el
		* mismo tamaño, tanto para entrada como para salida.</p>
		*
		* <p>El objetivo de esta fábrica es generar <i>forwarders</i>,
		* es decir, circuitos que conectan clientes con servidores
		* especificados dentro del interceptor.</p>
		*/

	public final class ForwardAttachmentFactory implements AttachmentFactory {

		// TODO: Debería obtenerse por configuración:
		public static final int BUFFER_SIZE = 8192;

		/**
		* <p>Genera un nuevo <i>attachment</i> de forwarding.</p>
		*
		* @return Devuelve un objeto <b>ClientAttachment</b>, el cual
		*	se encarga de gestionar el sistema de proxy POP/3, y de
		*	generar los correspondientes <b>ServerAttachment</b>.
		*/

		public Attachment create() {

			return new ClientAttachment();
		}
	}
