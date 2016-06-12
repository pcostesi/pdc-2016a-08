
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
		* <p>Este componente le ofrece al sistema la posibilidad
		* de ejecutar procesos en paralelo de forma consistente.
		* Para ello, utiliza un contenedor cuya función es
		* sincronizar el contenido de las diferentes tareas a
		* despachar. Adicionalmente, ofrece mecanismos para
		* actualizar el estado de las tareas de forma dinámica.</p>
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

		/**
		* <p>Permite determinar la cantidad de threads disponibles
		* para despachar trabajos de forma concurrente. Usualmente
		* este parámetro es equivalente a la cantidad de procesadores
		* lógicos disponibles en el sistema.</p>
		*
		* @return La cantidad de threads disponibles para trabajar.
		*/

		public int getWorkers() {

			return workers;
		}

		/**
		* <p> Intenta cerrar por completo el sistema de threads,
		* bloqueando la suscripción de nuevas tareas y esperando
		* hasta que las actividades ya desplegadas finalicen (hasta
		* cierto tiempo).</p>
		*/

		public void shutdown() {

			// Cancela la suscripción de nuevas tareas:
			pool.shutdown();

			try {

				if (!terminate(AWAIT_TIMEOUT)) {

					// Intenta cerrar tareas de forma más agresiva:
					pool.shutdownNow();

					if (!terminate(AWAIT_TIMEOUT))
						logger.info(Message.CANNOT_TERMINATE.getMessage());
				}
			}
			catch (InterruptedException exception) {

				pool.shutdownNow();
				logger.info(Message.SHUTDOWN_INTERRUPTED.getMessage());
			}
		}

		/**
		* <p>Despacha una nueva tarea. Para ello dispone de un thread
		* secundario que se encuentre disponible y ejecuta sobre el
		* mismo el handler asociado al evento activo en la clave
		* recibida. Si no hay un thread disponible el trabajo se
		* encola hasta que se libere algún worker.</p>
		*
		* @param handler
		*	El manejador del evento, que procesará la clave recibida.
		* @param key
		*	La clave a procesar por el handler, dentro de un thread.
		*/

		public void submit(Handler handler, SelectionKey key) {

			// Almacena la clave y suspende sus canales:
			sync.save(key);
			handler.onSubmit(key);

			// Despachar una nueva tarea en algún worker:
			pool.execute(new Runnable() {

				public void run() {

					// Ejecutar manejador y reponer claves:
					handler.handle(key);
					handler.onResume(key);
				}
			});
		}

		/**
		* <p>Espera hasta que las tareas pendientes en el pool
		* de threads finalicen. Si el tiempo de espera supera
		* el umbral especificado, el método retorna inmediatamente.</p>
		*
		* @param timeout
		*	El umbral máximo de espera para todas las tareas en el pool,
		*	en milisegundos.
		*
		* @return <i>True</i>, si todas las tareas finalizaron dentro del
		*	umbral de tiempo especificado (en milisegundos).
		*
		* @throws InterruptedException
		*	Si este método es interrumpido durante la espera.
		*/

		private boolean terminate(final long timeout)
					throws InterruptedException {

			return pool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
		}
	}
