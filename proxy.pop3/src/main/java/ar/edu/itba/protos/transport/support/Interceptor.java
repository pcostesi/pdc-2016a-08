
package ar.edu.itba.protos.transport.support;

import java.nio.ByteBuffer;

/**
 * El objetivo de esta interfaz es simplemente proveer un método, el cual aplica
 * una cadena de transformaciones sobre un flujo de bytes. Esto permite delegar
 * el procesamiento del flujo entrante hacia otra entidad, por ejemplo, un
 * parser o un encriptador.
 */

@FunctionalInterface
public interface Interceptor {

	/**
	 * Consume un flujo de bytes (buffer), aplicando cualquier tipo de
	 * pre/post-procesamiento sobre el mismo. Se espera que el mismo buffer
	 * consumido sea transformado, en caso de que la entidad que provee el flujo
	 * de bytes deba manipular el mismo, luego del procesamiento.
	 * 
	 * @param buffer
	 *            Flujo de bytes a transformar. La clase que implemente esta
	 *            interfaz debe interpretar que el flujo de bytes útil del mismo
	 *            se encuentra entre <b>buffer.position()</b> y
	 *            <b>buffer.limit() - 1</b>, inclusive.
	 */

	public void consume(ByteBuffer buffer);

	/*
	 ** Interceptor por defecto. Recibe un buffer, pero no hace nada con él.
	 */

	public static final Interceptor DEFAULT = new Interceptor() {

		public void consume(ByteBuffer buffer) {

			// Todo esto no debería estar:
			System.out.print(">>> Consume(" + buffer + ")\n\t= [");
			for (int i = buffer.position(); i < buffer.limit(); ++i) {

				char character = (char) buffer.array()[i];
				if (character == '\n') {

					if (i + 1 == buffer.limit())
						System.out.print("\\n");
					else
						System.out.println("\\n");
				} else if (character == '\r')
					System.out.print("\\r");
				else
					System.out.print(character);
			}
			System.out.println("]");
		}
	};
}
