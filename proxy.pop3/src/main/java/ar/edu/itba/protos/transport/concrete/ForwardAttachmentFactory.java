
	package ar.edu.itba.protos.transport.concrete;

	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;

		/**
		* Esta implementación de 'AttachmentFactory', genera
		* instancias que poseen un buffer de tamaño fijo. En
		* este caso, el buffer es de 4 Kb. El buffer posee el
		* mismo tamaño, tanto para entrada como para salida.
		* 
		* El objetivo de esta fábrica es generar 'forwarders',
		* es decir, circuitos que conectan clientes con servidores
		* especificados dentro del interceptor.
		*/

	public final class ForwardAttachmentFactory implements AttachmentFactory {

		// Tamaño máximo del buffer (capacidad):
		public static final int BUFFER_SIZE = 8192;

		public Attachment create() {

			return new ClientAttachment();
		}
	}
