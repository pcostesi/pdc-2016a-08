
	package ar.edu.itba.protos.transport.support;

	import java.nio.channels.SelectionKey;
	import java.util.concurrent.ExecutorService;
	import java.util.concurrent.Executors;
	import java.util.concurrent.TimeUnit;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	import ar.edu.itba.protos.transport.reactor.Handler;

		/**
		* Este componente le ofrece al sistema la posibilidad
		* de ejecutar procesos en paralelo de forma consistente.
		* Para ello, utiliza un contenedor cuya función es
		* sincronizar el contenido de las diferentes tareas a
		* despachar. Adicionalmente, ofrece mecanismos para
		* actualizar el estado de las tareas de forma dinámica.
		*/

	@Singleton
	public final class ThreadingCore {

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(ThreadingCore.class);

		// Tiempo de espera para cancelar threads:
		private static final long AWAIT_TIMEOUT = 1000;

		// El repositorio de claves global:
		private final Synchronizer sync;

		// El pool de workers disponibles:
		private final ExecutorService pool;

		// Cantidad de threads disponibles:
		private int workers = 1;

		@Inject
		private ThreadingCore(final Synchronizer sync) {

			this.sync = sync;

			workers = Runtime.getRuntime().availableProcessors();
			pool = Executors.newFixedThreadPool(workers);
		}

		public int getWorkers() {

			return workers;
		}

		public void shutdown() {

			// Cancela la suscripción de nuevas tareas:
			pool.shutdown();

			try {

				if (!terminate(AWAIT_TIMEOUT)) {

					// Intenta cerrar tareas de forma más agresiva:
					pool.shutdownNow();

					if (!terminate(AWAIT_TIMEOUT))
						logger.debug(Message.CANNOT_TERMINATE.getMessage());
				}
			}
			catch (InterruptedException exception) {

				pool.shutdownNow();
				logger.debug(Message.SHUTDOWN_INTERRUPTED.getMessage());
			}
		}

		public void submit(final Handler handler, final SelectionKey key) {

			// TODO: Hacer esta cosa...
			sync.save(key);
			handler.handle(key);
			sync.restore(key);
		}

		private boolean terminate(final long timeout)
					throws InterruptedException {

			return pool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
		}
	}
