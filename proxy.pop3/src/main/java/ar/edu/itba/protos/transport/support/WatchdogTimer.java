
	package ar.edu.itba.protos.transport.support;

	import java.io.IOException;
	import java.nio.channels.Channel;
	import java.nio.channels.SelectionKey;
	import java.util.Comparator;
	import java.util.PriorityQueue;

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

		// La constante de inactividad (en milisegundos):
		private long timeout;

		// El método de comparación de actividades:
		private static final Comparator<Activity> comparator
			= new Comparator<Activity>() {

			/*
			** Este comparador mantiene las actividades con
			** mayor tiempo de inactividad al frente de la cola.
			*/

			public int compare(Activity x, Activity y) {

				if (x.inactivity < y.inactivity) return -1;
				if (x.inactivity > y.inactivity) return 1;
				return 0;
			}
		};

		// Cola de actividades (ordenada por inactividad):
		private final PriorityQueue<Activity> activities
			= new PriorityQueue<Activity>(comparator);

		public WatchdogTimer(long timeout) {

			if (timeout < 0)
				throw new IllegalArgumentException();

			this.timeout = timeout;
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

		public void killLazyActivities() {

			long now = System.currentTimeMillis();
			while (!activities.isEmpty()) {

				Activity activity = activities.peek();
				if (timeout < now - activity.inactivity) {

					activities.poll();
					close(activity.key);
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
		** Cancela la clave y cierra el canal asociado a la misma.
		*/

		private void close(SelectionKey key) {

			key.cancel();
			Channel channel = key.channel();
			if (channel.isOpen()) {

				try {channel.close();}
				catch (IOException spurious) {}
			}
		}

		/*
		** El objetivo de esta clase es mantener el estado
		** de inactividad de las claves registradas.
		*/

		private final class Activity {

			// Cantidad de inactividad en milisegundos:
			public long inactivity;

			// Clave del canal asociado a esta actividad:
			public SelectionKey key;

			public Activity(long inactivity, SelectionKey key) {

				this.inactivity = inactivity;
				this.key = key;
			}

			@Override
			public boolean equals(Object obj) {

				/*
				** Dos actividades son iguales si sus claves
				** asociadas son iguales. Este método no se
				** corresponde con las definiciones usuales
				** por motivos de performance.
				*/
				Activity activity = (Activity) obj;
				return key.equals(activity.key);
			}
		}
	}
