
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.SelectionKey;
	import java.nio.channels.Selector;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Map;
	import java.util.Set;

		/**
		** Implementación del patrón Reactor. Este sistema permite
		** forwardear eventos hacia distintas entidades desacopladas
		** de forma eficiente y genérica. En este caso, los eventos
		** tratables se asocian a estados de canales de comunicación.
		*/

	public final class Reactor {

		// Mapa de entidades que pueden procesar eventos:
		private Map<Event, Set<Handler>> handlers = null;

		// El generador de eventos (creo que no va en este lugar):
		// private Selector selector = null;

		public Reactor(Selector selector) {

			// El reactor opera sobre estos canales (tampoco acá):
			// this.selector = selector;

			// La estructura de búsqueda de eventos es un mapa:
			handlers = new HashMap<Event, Set<Handler>>();

			// Para cada evento, se dispone un conjunto de manejadores:
			for (Event event : Event.values()) {

				handlers.put(event, new HashSet<Handler>());
			}
		}

		/*
		** Devuelve la cantidad de eventos que este reactor
		** puede despachar.
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

			return key.isValid() &&
				0 != (event.getOptions() & key.readyOps());
		}

		/*
		** Registra un nuevo manejador, para un evento
		** determinado. Si el manejador ya estaba anotado,
		** este método no tiene efecto.
		*/

		public void add(Handler handler, Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) {

				set.add(handler);
			}
		}

		/*
		** Registra un manejador, pero en este caso, para
		** múltiples eventos. Las opciones especificadas se
		** corresponden con una máscara, donde cada bit
		** representa un evento en particular (ver Event).
		*/

		public void add(Handler handler, int options) {

			for (Event event : Event.values()) {

				if (0 != (options & event.getOptions())) {

					add(handler, event);
				}
			}
		}

		/*
		** Cancelar la subscripción de un manejador sobre
		** cierto evento. El manejador ya no recibirá eventos
		** de este tipo.
		*/

		public void remove(Handler handler, Event event) {

			Set<Handler> set = handlers.get(event);
			if (set != null) {

				set.remove(handler);
			}
		}

		/*
		** Cancela la subscripción de un manejador de todos
		** los eventos en los que esté registrado. El manejador
		** ya no recibirá eventos de ningún tipo.
		*/

		public void remove(Handler handler) {

			for (Event event : Event.values()) {

				remove(handler, event);
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
				if (set != null && isOn(event, key)) {

					for (Handler handler : set) {

						handler.handle(key);
					}
				}
			}
		}
	}
