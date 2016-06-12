
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.SelectionKey;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Map;
	import java.util.Set;

	import javax.inject.Singleton;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.google.inject.Inject;

	import ar.edu.itba.protos.transport.support.Message;
	import ar.edu.itba.protos.transport.support.Server;
	import ar.edu.itba.protos.transport.support.ThreadingCore;

		/**
		* <p>Implementación del patrón Reactor. Este sistema
		* permite forwardear eventos hacia distintas entidades
		* desacopladas de forma eficiente y genérica. En este
		* caso, los eventos tratables se asocian a estados de
		* canales de comunicación.</p>
		*
		* <p>Debido a que cumple con las condiciones de <i>Google Guice</i>
		* para ser injectado como <b>singleton</b> (posee la anotación
		* <i>{@literal @Singleton}</i>, y su constructor es privado, sin
		* parámetros y bajo <i>{@literal @Inject}</i>), la clase debería ser
		* <b>thread-safe</b>. Ya que solo el <i>master-thread</i> accede a
		* su única instancia, no es necesario garantizar dicha condición.</p>
		*
		* @see
		*	<a href = "https://github.com/google/guice/wiki/Scopes
		*		#scopes-and-concurrency">
		*		Google Guice: Scopes and Concurrency
		*	</a>
		*/

	@Singleton
	public final class Reactor {

		// Logger:
		private static final Logger logger
			= LoggerFactory.getLogger(Reactor.class);

		// Mapa de entidades que pueden procesar eventos:
		private final Map<Event, Set<Handler>> handlers;

		// El núcleo de ejecución:
		private final ThreadingCore core;

		@Inject
		private Reactor(final ThreadingCore core) {

			// Permite despachar handlers en paralelo:
			this.core = core;

			// La estructura de búsqueda de eventos es un mapa:
			handlers = new HashMap<>();

			// Para cada evento, se dispone un conjunto de manejadores:
			for (Event event : Event.values())
				handlers.put(event, new HashSet<>());
		}

		/**
		* <p>Especifica cuántos eventos puede manipular este
		* reactor. Es de esperarse que pueda manejar todos los
		* eventos válidos definidos en la clase
		* <b>SelectionKey</b>.</p>
		*
		* @return Devuelve la cantidad de eventos que este
		*	reactor puede despachar.
		*/

		public int getEvents() {

			return handlers.size();
		}

		/**
		* <p>Para cada evento disponible (aquellos que este reactor
		* soporta), especifica la cantidad de manejadores
		* subscriptos.</p>
		*
		* @param event
		*	El tipo de evento para el cual se desea determinar la
		*	cantidad de handlers subscriptos.
		*
		* @return Devuelve la cantidad de manejadores registrados
		*	para un determinado evento.
		*/

		public int getHandlers(Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) return set.size();
			return 0;
		}

		/**
		* <p>Determina si el evento especificado se encuentra activo
		* en la clave indicada.</p>
		*
		* @param event
		*	El evento para el cual se desea determinar su presencia.
		* @param key
		*	La clave a revisar.
		*
		* @return Devuelve <i>true</i> si el evento especificado se
		* encuentra activo en la clave indicada.
		*
		* @throws CancelledKeyException
		*	Si la clave especificada había sido cancelada previamente.
		*/

		public static boolean isOn(Event event, SelectionKey key)
				throws CancelledKeyException {

			return 0 != (event.getOptions() & key.readyOps());
		}

		/**
		* <p>Registra un nuevo manejador, para un evento
		* determinado. Si el manejador ya estaba anotado,
		* este método no tiene efecto.</p>
		*
		* @param handler
		*	El manejador a registrar en el reactor.
		* @param event
		*	El evento para el cual se va a registrar el handler.
		*
		* @return Devuelve este mismo reactor.
		*/

		public Reactor add(Handler handler, Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) set.add(handler);
			return this;
		}

		/**
		* <p>Registra un manejador, pero en este caso, para
		* múltiples eventos. Las opciones especificadas se
		* corresponden con una máscara, donde cada bit
		* representa un evento en particular
		* (ver <b>Event</b>).</p>
		*
		* @param handler
		*	El manejador a registrar en el reactor.
		* @param options
		*	La máscara que especifica todos los eventos para
		*	los cuales registar el handler.
		*
		* @return Devuelve este reactor.
		*/

		public Reactor add(Handler handler, int options) {

			for (Event event : Event.values())
				if (0 != (options & event.getOptions()))
					add(handler, event);

			return this;
		}

		/**
		* <p>Bloquea la ejecución de nuevas tareas por completo, lo
		* que significa que el reactor queda inutilizable. Para ello,
		* apaga el núcleo de procesamiento (pool de threads).</p>
		*/

		public void block() {

			core.shutdown();
		}

		/**
		* <p>Para un determinado evento (el cual se representa
		* mediante una clave), ejecuta todos los manejadores
		* registrados para ese evento.</p>
		*
		* @param key
		*	La clave seleccionada a despachar en sus respectivos
		*	manejadores subscriptos.
		*/

		public void dispatch(SelectionKey key) {

			try {

				for (Event event : Event.values()) {

					Set<Handler> set = handlers.get(event);

					if (set != null && isOn(event, key)) {

						for (Handler handler : set)
							core.submit(handler, key);

						/* Solo un evento por vez, debido a que
						** las claves pueden responder simultáneamente
						** a varios eventos, y esto puede causar una
						** condición de carrera durante la registración
						** en el repositorio de claves.
						*/
						return;
					}
				}
			}
			catch (CancelledKeyException exception) {

				logger.error(
					Message.UNEXPECTED_UNPLUG.getMessage(),
					Server.tryToResolveAddress(key));

				Server.close(key);
			}
		}

		/**
		* <p>Cancelar la subscripción de un manejador sobre
		* cierto evento. El manejador ya no recibirá eventos
		* de este tipo.</p>
		*
		* @param handler
		*	El handler a remover.
		* @param event
		*	El evento para el cual el handler ya no se encargará
		*	de manejar.
		*
		* @return Devuelve este mismo reactor.
		*/

		public Reactor remove(Handler handler, Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) set.remove(handler);
			return this;
		}

		/**
		* <p>Cancela la subscripción de un manejador de todos
		* los eventos en los que esté registrado. El manejador
		* ya no recibirá eventos de ningún tipo.</p>
		*
		* @param handler
		*	El manejador que será desubscripto de todos los eventos
		*	que soporta este reactor.
		*
		* @return Devuelve este mismo reactor.
		*/

		public Reactor remove(Handler handler) {

			for (Event event : Event.values())
				remove(handler, event);

			return this;
		}

		/**
		* <p>Este método permite desubscribir todos los <i>handlers</i>,
		* lo que permite desconectar todos los componentes que,
		* gracias al reactor, se encontraban relacionados. Luego de
		* ejecutar este método, las claves recibidas no serán
		* despachadas hacia ningún handler, aunque como el núcleo de
		* procesamiento sigue activo (pool de threads), es posible
		* registrar nuevos (o incluso los mismos) manejadores.</p>
		*/

		public void unplug() {

			for (Event event : Event.values()) {

				Set<Handler> set = handlers.get(event);
				if (set != null) set.clear();
			}
		}
	}
