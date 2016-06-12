
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
		private final ConcurrentMap<SelectionKey, MutableInt> keys;

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

		public void delete(SelectionKey key) {

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

		public synchronized void disable(SelectionKey key, Event event) {

			System.out.println("sync.Disable() -> " + event);
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

		public synchronized void enable(SelectionKey key, Event event) {

			System.out.println("sync.Enable() -> " + event);
			if (key == null || event == null)
				throw new IllegalArgumentException();

			MutableInt options = keys.get(key);
			if (options != null) {

				int oldOptions = options.intValue();
				options.setValue(oldOptions | event.getOptions());
			}
		}

		/**
		* <p>Este método actualiza la máscara de opciones de interés
		* en el repositorio de claves, peo en lugar de hacerlo evento
		* por evento, aplica un cambio directo en todas las opciones
		* almacenadas.</p>
		*
		* <p>Este método es más cómodo cuando se desean modificar
		* varias opciones en simultáneo (y de forma atómica).</p>
		*
		* @param key
		*	La clave para la cual se aplicarán las nuevas opciones.
		* @param options
		*	La nueva máscara de eventos (opciones).
		*
		* @throws IllegalArgumentException
		*	En caso de que la clave sea <i>null</i>.
		*/

		public synchronized void enable(SelectionKey key, int options) {

			if (key == null)
				throw new IllegalArgumentException();

			MutableInt oldOptions = keys.get(key);

			if (oldOptions != null)
				oldOptions.setValue(options);
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

		public synchronized void restore(SelectionKey key) {

			System.out.println("RESTORE: " + key);
			if (key == null)
				throw new IllegalArgumentException();

			MutableInt options = keys.get(key);
			System.out.println(	"RESTORE value: " + options);
			try {

				if (options != null)
					key.interestOps(options.intValue());
			}
			catch (CancelledKeyException exception) {

				System.out.println("########################RESTORE cancelled");
				keys.remove(key);
			}
			System.out.println("Ending restore...");
		}

		/**
		* <p>Intenta almacenar el estado de la clave en el repositorio
		* interno. Si la clave ya existía en el repositorio, se
		* actualiza el estado de la misma. Esto permite modificar las
		* opciones de interés sobre la misma de manera segura, atómica,
		* y concurrente.</p>
		*
		* <p>Adicionalmente deshabilita todas las opciones de interés
		* de la clave especificada, lo que impide que la clave sea
		* seleccionada nuevamente, hasta que alguno de sus eventos sea
		* rehabilitado, ya sea de forma manual, o a través de una
		* llamada al método <b>restore</b>.</p>
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

		public synchronized void save(SelectionKey key) {

			System.out.println("SAVE: " + key);

			if (key == null)
				throw new IllegalArgumentException();

			try {

				MutableInt options = new MutableInt(key.interestOps());
				System.out.println("	SAVE value: " + options);
				keys.put(key, options);
				key.interestOps(0);
			}
			catch (CancelledKeyException exception) {

				System.out.println("########################save cancelled");
				keys.remove(key);
			}
			System.out.println("Ending saving...");
		}
	}
