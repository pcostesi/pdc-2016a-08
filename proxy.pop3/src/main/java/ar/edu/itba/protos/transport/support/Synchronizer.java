
	package ar.edu.itba.protos.transport.support;

	import java.nio.channels.SelectionKey;
	import java.util.concurrent.ConcurrentHashMap;
	import java.util.concurrent.ConcurrentMap;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

		/**
		* Mantiene el estado de las claves (SelectionKey),
		* y ofrece mecanismos para modificar las mismas de
		* forma sincronizada entre múltiples threads. Este
		* componente hace las veces de repositorio de claves.
		*/

	@Singleton
	public final class Synchronizer {

		// El repositorio global de claves:
		private ConcurrentMap<SelectionKey, Integer> keys = null;

		@Inject
		private Synchronizer() {

			// Solo se instancia una vez:
			keys = new ConcurrentHashMap<>();
		}

		public void desynchronize() {

			keys.clear();
		}
	}
