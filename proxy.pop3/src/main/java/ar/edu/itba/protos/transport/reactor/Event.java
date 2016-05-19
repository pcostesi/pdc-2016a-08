
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.SelectionKey;

	/*
	** Representa el conjunto de eventos posibles, que un
	** reactor debe demultiplexar. Debido a que la clase
	** SelectionKey declara los eventos utilizando constantes
	** individuales, es necesario construir este Enum para
	** recorrerlos de forma sistemática.
	*/

	public enum Event {

		ACCEPT(SelectionKey.OP_ACCEPT),
		CONNECT(SelectionKey.OP_CONNECT),
		READ(SelectionKey.OP_READ),
		WRITE(SelectionKey.OP_WRITE);

		// La máscara que identifica el evento:
		private int options;

		private Event(int options) {

			this.options = options;
		}

		/*
		** Devuelve la máscara asociada a cada evento,
		** la cual se obtiene de la clase SelectionKey.
		*/

		public int getOptions() {

			return options;
		}
	}
