
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.SelectionKey;

		/**
		* <p>Representa el conjunto de eventos posibles, que un
		* reactor debe demultiplexar. Debido a que la clase
		* <b>SelectionKey</b> declara los eventos utilizando constantes
		* individuales, es necesario construir este <i>Enum</i> para
		* recorrerlos de forma sistemática.</p>
		*/

	public enum Event {

		ACCEPT		(SelectionKey.OP_ACCEPT),
		CONNECT		(SelectionKey.OP_CONNECT),
		READ		(SelectionKey.OP_READ),
		WRITE		(SelectionKey.OP_WRITE);

		// La máscara que identifica el evento:
		private final int options;

		private Event(final int options) {

			this.options = options;
		}

		/**
		* <p>Permite obtener la máscar subyacente definida por
		* la clase <b>SelectionKey</b>.</p>
		*
		* @return Devuelve la máscara asociada a cada evento,
		*	la cual se obtiene de la clase SelectionKey.
		*/

		public int getOptions() {

			return options;
		}

		/**
		* <p>Habilita nuevos eventos en la clave especificada
		* sin modificar el resto de ellos.</p>
		*
		* @param key
		*	La clave en la cual se van a habilitar los eventos.
		* @param options
		*	La máscara de opciones con los eventos a habilitar.
		*
		* @throws CancelledKeyException
		*	Si la clave especificada había sido cancelada.
		*/

		public static void enable(SelectionKey key, int options)
				throws CancelledKeyException {

			if (key != null)
				key.interestOps(key.interestOps() | options);
		}

		/**
		* <p>Deshabilita los eventos en la clave especificada
		* sin modificar el resto de ellos.</p>
		*
		* @param key
		*	La clave en la cual se van a deshabilitar los eventos.
		* @param options
		*	La máscara de opciones con los eventos a deshabilitar.
		*
		* @throws CancelledKeyException
		*	Si la clave especificada había sido cancelada.
		*/

		public static void disable(SelectionKey key, int options)
				throws CancelledKeyException {

			if (key != null)
				key.interestOps(key.interestOps() & (~options));
		}
	}
