
	package ar.edu.itba.protos.transport.support;

		/**
		* Esta interfaz permite instalar un 'attachment'
		* dentro de un canal especificado de forma genérica,
		* delegando de esta forma el proceso de instanciación
		* del mismo hacia una subclase.
		*/

	public interface AttachmentFactory {

		public Attachment create();

		/*
		** Fábrica por defecto. Simplemente devuelve un 'attachment' nulo.
		*/

		public static final AttachmentFactory DEFAULT
			= new AttachmentFactory() {

			public Attachment create() {

				return null;
			}
		};
	}
