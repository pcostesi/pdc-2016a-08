
	package ar.edu.itba.protos.transport.support;

	import java.nio.channels.SelectionKey;
	import java.util.Comparator;
	import java.util.PriorityQueue;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

		/**
		* <p>Un <b>watchdog-timer</b> permite manipular el nivel de
		* inactividad de un canal de comunicación, o de un
		* flujo de bytes de manera simple y dinámica. Para
		* cada canal en el umbral de inactividad se ejecuta
		* un subproceso que toma las acciones debidas sobre
		* el mismo (por ejemplo, cerrar un socket).</p>
		* 
		* <p>En esta clase, se realizó una implementación
		* específica para cerrar <i>SocketChannel's</i>.</p>
		*/

	@Singleton
	public final class WatchdogTimer {

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(WatchdogTimer.class);

		// Cola de actividades (ordenada por inactividad):
		private final PriorityQueue<Activity> activities
			= new PriorityQueue<>(
					Comparator.comparing(Activity::getInactivity));

		// Repositorio de claves global:
		private final Synchronizer sync;

		// La constante de inactividad (en milisegundos):
		private long timeout = Long.MAX_VALUE;

		@Inject
		private WatchdogTimer(final Synchronizer sync) {

			this.sync = sync;
		}

		/**
		* <p>Devuelve la constante de inactividad en milisegundos,
		* la cual indica cuánto tiempo debe permanecer un canal
		* sin actividad para ser manipulado por una acción
		* de cierre sobre el mismo.</p>
		*
		* @return El umbral máximo de inactividad, en milisegundos.
		*/

		public synchronized long getTimeout() {

			return timeout;
		}

		/**
		* <p>Setea el nuevo umbral (timeout) para cerrar canales
		* inactivos. El valor se debe especificar en milisegundos.</p>
		*
		* @param timeout
		*	El nuevo umbral de inactividad en milisegundos.
		*
		* @throws IllegalArgumentException
		*	Si el umbral especificado es negativo.
		*/

		public synchronized void setTimeout(final long timeout) {

			if (timeout < 0)
				throw new IllegalArgumentException();

			this.timeout = timeout;

			logger.info(
				Message.TIMEOUT_TRIGGER.getMessage(),
				timeout/1000.0);
		}

		/**
		* <p>Agrega una nueva actividad al <i>watchdog-timer</i>. Es
		* importante notar que no se verifica que la clave ya
		* exista en la cola, ya que esto degradaría la complejidad
		* del algoritmo de <i>O(log n)</i> hacia <i>O(n)</i>.</p>
		*
		* @param key
		*	La clave a monitorear.
		*/

		public synchronized void addActivity(SelectionKey key) {

			if (key != null) {

				long inactivity = System.currentTimeMillis();
				Activity activity = new Activity(inactivity, key);
				activities.offer(activity);
			}
		}

		/**
		* <p>Elimina todas las actividades cuyo tiempo de inactividad
		* superó la constante de <b>timeout</b>. Además, para cada una
		* de ellas, cancela su clave asociada, y cierra el canal.</p>
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

		/**
		* <p>Remueve una clave previamente registrada para
		* monitoreo de inactividad.</b>
		*
		* @param key
		*	La clave a remover del monitor de inactividad.
		*
		* @return Devuelve <i>true</i> si la clave existía en el
		*	monitor, o <i>false</i> sino.
		*/

		public synchronized boolean removeActivity(SelectionKey key) {

			if (key != null)
				return activities.remove(new Activity(0, key));

			return false;
		}

		/**
		* <p>Vacía la cola de prioridades sin cancelar
		* las claves registradas, ni cerrar sus canales.</p>
		*/

		public void removeAll() {

			activities.clear();
		}

		/**
		* <p>Actualiza el nivel de inactividad de la
		* clave especificada, lo que evita que la misma sea
		* destruída debido a inactividad.</p>
		*
		* @param key
		*	La clave a actualizar en el monitor.
		*/

		public void update(SelectionKey key) {

			removeActivity(key);
			addActivity(key);
		}

		/**
		* <p>Cancela la clave y cierra el canal asociado a la misma.
		* Además, elimina su estado del repositorio global de claves,
		* lo que impide la sedimentación de claves inválidas.</p>
		*
		* @param key
		*	La clave a cancelar, para la cual se cerrará su canal.
		*/

		private void close(SelectionKey key) {

			logger.info(
				Message.KILL_BY_LAZY.getMessage(),
				Server.tryToResolveAddress(key));

			Server.close(key);
			sync.delete(key);
		}

		/**
		* <p>El objetivo de esta clase es mantener el estado
		* de inactividad de las claves registradas.</p>
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
