
	package ar.edu.itba.protos.transport.support;

	import java.nio.channels.SelectionKey;
	import java.util.Comparator;
	import java.util.PriorityQueue;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;

		/**
		* Un 'watchdog-timer' permite manipular el nivel de
		* inactividad de un canal de comunicación, o de un
		* flujo de bytes de manera simple y dinámica. Para
		* cada canal en el umbral de inactividad se ejecuta
		* un subproceso que toma las acciones debidas sobre
		* el mismo (por ejemplo, cerrar un socket).
		* 
		* En esta clase, se realizó una implementación
		* específica para cerrar SocketChannel's.
		*/

	public final class WatchdogTimer {

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(WatchdogTimer.class);

		// La constante de inactividad (en milisegundos):
		private long timeout = Long.MAX_VALUE;

		// Cola de actividades (ordenada por inactividad):
		private final PriorityQueue<Activity> activities
			= new PriorityQueue<>(
					Comparator.comparing(Activity::getInactivity));

		// Repositorio de claves global:
		private Synchronizer sync;

		@Inject
		public WatchdogTimer(final Synchronizer sync) {

			this.sync = sync;
		}

		/*
		** Devuelve la constante de inactividad en milisegundos,
		** la cual indica cuánto tiempo debe permanecer un canal
		** sin actividad para ser manipulado por una acción
		** de cierre sobre el mismo.
		*/

		public long getTimeout() {

			return timeout;
		}

		/*
		** Setea el nuevo umbral (timeout) para cerrar canales
		** inactivos. El valor se debe especificar en milisegundos.
		*/

		public void setTimeout(final long timeout) {

			if (timeout < 0)
				throw new IllegalArgumentException();

			this.timeout = timeout;

			logger.info(
				Message.TIMEOUT_TRIGGER.getMessage(),
				timeout/1000.0);
		}

		/*
		** Agrega una nueva actividad al 'watchdog-timer'. Es
		** importante notar que no se verifica que la clave ya
		** exista en la cola, ya que esto degradaría la complejidad
		** del algoritmo de O(log n) hacia O(n).
		*/

		public void addActivity(SelectionKey key) {

			if (key != null) {

				long inactivity = System.currentTimeMillis();
				Activity activity = new Activity(inactivity, key);
				activities.offer(activity);
			}
		}

		/*
		** Elimina todas las actividades cuyo tiempo de inactividad
		** superó la constante de 'timeout'. Además, para cada una
		** de ellas, cancela su clave asociada.
		*/

		public synchronized void killLazyActivities() {

			long now = System.currentTimeMillis();
			while (!activities.isEmpty()) {

				Activity activity = activities.peek();
				if (timeout < now - activity.getInactivity()) {

					activities.poll();
					close(activity.getKey());
				}
				else break;
			}
		}

		/*
		** Remueve una clave previamente registrada para
		** monitoreo de inactividad. Si la clave se encontró
		** devuelve 'true', sino 'false'.
		*/

		public boolean removeActivity(SelectionKey key) {

			if (key != null)
				return activities.remove(new Activity(0, key));

			return false;
		}

		/*
		** Vacía la cola de prioridades sin cancelar
		** las claves registradas.
		*/

		public void removeAll() {

			activities.clear();
		}

		/*
		** Actualiza el nivel de inactividad de la
		** clave especificada.
		*/

		public synchronized void update(SelectionKey key) {

			removeActivity(key);
			addActivity(key);
		}

		/*
		** Cancela la clave y cierra el canal asociado a la misma.
		*/

		private void close(SelectionKey key) {

			logger.info(
				Message.KILL_BY_LAZY.getMessage(),
				Server.tryToResolveAddress(key));

			Server.close(key);
			sync.delete(key);
		}

		/*
		** El objetivo de esta clase es mantener el estado
		** de inactividad de las claves registradas.
		*/

		private final class Activity {

			// Cantidad de inactividad en milisegundos:
			private long inactivity;

			// Clave del canal asociado a esta actividad:
			private SelectionKey key;

			public Activity(long inactivity, SelectionKey key) {

				this.inactivity = inactivity;
				this.key = key;
			}

			/*
			** Getter's
			*/

			public long getInactivity() {

				return inactivity;
			}

			public SelectionKey getKey() {

				return key;
			}

			@Override
			public boolean equals(Object obj) {

				/*
				** Dos actividades son iguales si sus claves
				** asociadas son iguales. Este método no se
				** corresponde con las definiciones usuales
				** por motivos de performance.
				*/
				if (obj == null) return false;
				Activity activity = (Activity) obj;
				return key.equals(activity.key);
			}
		}
	}
