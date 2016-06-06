
	package ar.edu.itba.protos.transport.support;

	import java.util.concurrent.ExecutorService;
	import java.util.concurrent.Executors;

		/**
		* Este componente le ofrece al sistema la posibilidad
		* de ejecutar procesos en paralelo de forma consistente.
		* Para ello, utiliza un contenedor cuya función es
		* sincronizar el contenido de las diferentes tareas a
		* despachar. Adicionalmente, ofrece mecanismos para
		* actualizar el estado de las tareas de forma dinámica.
		*/

	public final class ThreadingCore {

		// Cantidad de threads disponibles:
		private int workers = 1;

		// El pool de workers disponibles:
		private ExecutorService pool = null;

		public ThreadingCore(final int workers) {

			if (workers < 1)
				throw new IllegalArgumentException();

			this.workers = workers;
			pool = Executors.newFixedThreadPool(workers);
		}

		public int getWorkers() {

			return workers;
		}

		public void shutdown() {

			pool.shutdown();
		}
	}
