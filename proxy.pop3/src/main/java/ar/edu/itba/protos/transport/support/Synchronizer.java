
	package ar.edu.itba.protos.transport.support;

	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.SelectionKey;
	import java.util.concurrent.ConcurrentHashMap;
	import java.util.concurrent.ConcurrentMap;

	import org.apache.commons.lang3.mutable.MutableInt;

	import com.google.inject.Inject;
	import com.google.inject.Singleton;

	import ar.edu.itba.protos.transport.reactor.Event;

		/**
		* <p>Mantiene el estado de las claves (SelectionKey),
		* y ofrece mecanismos para modificar las mismas de
		* forma sincronizada entre múltiples threads. Este
		* componente hace las veces de repositorio de claves.</p>
		*
		* <p>Esta clase es <b>thread-safe</b>.</p>
		*/

	@Singleton
	public final class Synchronizer {

		// El repositorio global de claves:
		private ConcurrentMap<SelectionKey, MutableInt> keys = null;

		@Inject
		private Synchronizer() {

			// Solo se instancia una vez:
			keys = new ConcurrentHashMap<>();
		}

		/**
		* <p>Remueve una clave del repositorio. Esto implica que,
		* la clave debe dejar de usarse, o bien, se desea limpiar
		* por completo (descartar) el estado actual de la misma
		* en este repositorio.</p>
		*
		* @param key
		* 	La clave a eliminar del repositorio.
		*
		* @throws IllegalArgumentException
		*	En caso de que la clave sea <i>null</i>.
		*/

		public void delete(final SelectionKey key) {

			if (key == null)
				throw new IllegalArgumentException();

			keys.remove(key);
		}

		/**
		* <p>Realiza la desincronización de todas las claves del
		* repositorio (hasta el momento). El estado almacenado de
		* todas las claves se pierde para siempre.</p>
		*/

		public void desynchronize() {

			keys.clear();
		}

		/**
		* <p>Deshabilita la respuesta de una clave ha cierto evento
		* especificado. La modificación se realiza de manera temporal
		* en un repositorio de claves interno, y se aplica una vez que
		* se llama al método <b>restore</b> sobre la clave en
		* cuestión.</p>
		*
		* @param key
		*	La clave en la cual se deshabilitará el evento especificado.
		* @param event
		*	El evento a deshabilitar en la clave.
		*
		* @throws IllegalArgumentException
		*	En caso de que la clave o el evento sean <i>null</i>.
		*/

		public synchronized void disable(
				final SelectionKey key, final Event event) {

			if (key == null || event == null)
				throw new IllegalArgumentException();

			MutableInt options = keys.get(key);
			if (options != null) {

				int oldOptions = options.intValue();
				options.setValue(oldOptions & (~event.getOptions()));
			}
		}

		/**
		* <p>Habilita la respuesta de una clave ha cierto evento
		* especificado. La modificación se realiza de manera temporal
		* en un repositorio de claves interno, y se aplica una vez que
		* se llama al método <b>restore</b> sobre la clave en
		* cuestión.</p>
		* 
		* @param key
		*	La clave en la cual se habilitará el evento especificado.
		* @param event
		*	El evento a habilitar en la clave.
		*
		* @throws IllegalArgumentException
		*	En caso de que la clave o el evento sean <i>null</i>.
		*/

		public synchronized void enable(
				final SelectionKey key, final Event event) {

			if (key == null || event == null)
				throw new IllegalArgumentException();

			MutableInt options = keys.get(key);
			if (options != null) {

				int oldOptions = options.intValue();
				options.setValue(oldOptions | event.getOptions());
			}
		}

		/**
		* <p>Intenta recuperar el estado de la clave almacenado en
		* el repositorio interno. El estado almacenado puede tanto,
		* habilitar como deshabilitar eventos, por lo cual el
		* estado final de la clave puede modificarse por completo.</p>
		*
		* <p>En caso de que la clave se encuentre inválida (debido a que
		* la misma fue cancelada, o que el canal asociado a ella fue
		* cerrado), el estado no se repone, y la clave se elimina del
		* repositorio.</p>
		*
		* @param key
		* 	La clave para la cual se repondrá el estado actualizado.
		*
		* @throws IllegalArgumentException
		*	En caso de que la clave sea <i>null</i>.
		*/

		public synchronized void restore(final SelectionKey key) {

			if (key == null)
				throw new IllegalArgumentException();

			MutableInt options = keys.get(key);
			try {

				if (options != null)
					key.interestOps(options.intValue());
			}
			catch (CancelledKeyException exception) {

				keys.remove(key);
			}
		}

		/**
		* <p>Intenta almacenar el estado de la clave en el repositorio
		* interno. Si la clave ya existía en el repositorio, se
		* actualiza el estado de la misma. Esto permite modificar las
		* opciones de interés sobre la misma de manera segura, atómica,
		* y concurrente.</p>
		*
		* <p>En caso de que la clave se encuentre inválida (debido a que
		* la misma fue cancelada, o que el canal asociado a ella fue
		* cerrado), el estado no se almacena ni se actualiza, y la clave
		* se elimina del repositorio (si se encontraba en él).</p>
		*
		* @param key
		* 	La clave que se almacenará o actualizará en el repositorio.
		*
		* @throws IllegalArgumentException
		*	En caso de que la clave sea <i>null</i>.
		*/

		public synchronized void save(final SelectionKey key) {

			if (key == null)
				throw new IllegalArgumentException();

			try {

				keys.put(key, new MutableInt(key.interestOps()));
			}
			catch (CancelledKeyException exception) {

				keys.remove(key);
			}
		}
	}
