
	package ar.edu.itba.protos.transport.support;

	import java.nio.ByteBuffer;

		/**
		* <p>El objetivo de esta interfaz es simplemente
		* proveer un método, el cual aplica una cadena
		* de transformaciones sobre un flujo de bytes.
		* Esto permite delegar el procesamiento del flujo
		* entrante hacia otra entidad, por ejemplo, un
		* parser o un encriptador.</p>
		*/

	@FunctionalInterface
	public interface Interceptor {

		/**
		* <p>Consume un flujo de bytes (buffer), aplicando
		* cualquier tipo de pre/post-procesamiento sobre
		* el mismo. Se espera que el mismo buffer consumido
		* sea transformado, en caso de que la entidad que
		* provee el flujo de bytes deba manipular el mismo,
		* luego del procesamiento.</p>
		*
		* @param buffer
		* 	Flujo de bytes a transformar. La clase que implemente
		* 	esta interfaz debe interpretar que el flujo de bytes
		* 	útil del mismo se encuentra entre <b>buffer.position()</b>
		* 	y <b>buffer.limit() - 1</b>, inclusive.
		*/

		public void consume(ByteBuffer buffer);

		/**
		* <p>Interceptor por defecto. Recibe un buffer,
		* pero no hace nada con él.</p>
		*/

		public static final Interceptor DEFAULT = new Interceptor() {

			public void consume(ByteBuffer buffer) {}
		};
	}
