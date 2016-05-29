
	package ar.edu.itba.protos.transport.concrete;

	import ar.edu.itba.protos.transport.support.Attachment;
	import ar.edu.itba.protos.transport.support.AttachmentFactory;

	public final class AdminAttachmentFactory implements AttachmentFactory {

		public static final int BUFFER_SIZE = 8192;

		public Attachment create() {

			return new AdminAttachment();
		}
	}
