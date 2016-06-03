
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.SelectionKey;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Map;
	import java.util.Set;

	import javax.inject.Inject;
	import javax.inject.Singleton;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

		/**
		* Implementación del patrón Reactor. Este sistema
		* permite forwardear eventos hacia distintas entidades
		* desacopladas de forma eficiente y genérica. En este
		* caso, los eventos tratables se asocian a estados de
		* canales de comunicación.
		*/

	@Singleton
	public final class Reactor {

		// Logger:
		private final static Logger logger
			= LoggerFactory.getLogger(Reactor.class);

		// Mapa de entidades que pueden procesar eventos:
		private Map<Event, Set<Handler>> handlers = null;

		@Inject
		public Reactor() {

			// La estructura de búsqueda de eventos es un mapa:
			handlers = new HashMap<Event, Set<Handler>>();

			// Para cada evento, se dispone un conjunto de manejadores:
			for (Event event : Event.values())
				handlers.put(event, new HashSet<Handler>());
		}

		/*
		** Devuelve la cantidad de eventos que este
		** reactor puede despachar.
		*/

		public int getEvents() {

			return handlers.size();
		}

		/*
		** Devuelve la cantidad de manejadores registrados
		** para un determinado evento.
		*/

		public int getHandlers(Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) return set.size();
			return 0;
		}

		/*
		** Devuelve 'true' si el evento especificado se
		** encuentra activo en la clave indicada. Además,
		** verifica que la clave sea válida.
		*/

		public boolean isOn(Event event, SelectionKey key) {

			return key.isValid()
				&& 0 != (event.getOptions() & key.readyOps());
		}

		/*
		** Registra un nuevo manejador, para un evento
		** determinado. Si el manejador ya estaba anotado,
		** este método no tiene efecto.
		*/

		public Reactor add(Handler handler, Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) set.add(handler);
			return this;
		}

		/*
		** Registra un manejador, pero en este caso, para
		** múltiples eventos. Las opciones especificadas se
		** corresponden con una máscara, donde cada bit
		** representa un evento en particular (ver Event).
		*/

		public Reactor add(Handler handler, int options) {

			for (Event event : Event.values())
				if (0 != (options & event.getOptions()))
					add(handler, event);

			return this;
		}

		/*
		** Este método permite desubscribir todos los 'handlers',
		** lo que permite desconectar todos los componentes que,
		** gracias al reactor, se encontraban relacionados.
		*/

		public void unplug() {

			logger.debug("Unplugging all events from Reactor {}", this);

			for (Event event : Event.values()) {

				Set<Handler> set = handlers.get(event);
				if (set != null) set.clear();
			}
		}

		/*
		** Para un determinado evento (el cual se representa
		** mediante una clave), ejecuta todos los manejadores
		** registrados para ese evento.
		*/

		public void dispatch(SelectionKey key) {

			for (Event event : Event.values()) {

				Set<Handler> set = handlers.get(event);

				if (set != null && isOn(event, key))
					for (Handler handler : set)
						handler.handle(key);
			}
		}

		/*
		** Cancelar la subscripción de un manejador sobre
		** cierto evento. El manejador ya no recibirá eventos
		** de este tipo.
		*/

		public Reactor remove(Handler handler, Event event) {

			logger.debug(
				"Removing handler {} for event {}",
				handler,
				event);

			Set<Handler> set = handlers.get(event);
			if (set != null) set.remove(handler);
			return this;
		}

		/*
		** Cancela la subscripción de un manejador de todos
		** los eventos en los que esté registrado. El manejador
		** ya no recibirá eventos de ningún tipo.
		*/

		public Reactor remove(Handler handler) {

			logger.debug("Removing handler {}", handler);

			for (Event event : Event.values())
				remove(handler, event);

			return this;
		}
	}
