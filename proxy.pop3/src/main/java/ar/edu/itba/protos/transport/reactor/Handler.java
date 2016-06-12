
	package ar.edu.itba.protos.transport.reactor;

	import java.nio.channels.SelectionKey;

		/**
		* <p>Representa un manejador de eventos. Para cada
		* evento manejable, el <b>handler</b> debería subscribirse
		* en el reactor de interés, e implementar estos métodos de
		* acuerdo a su especificación.</p>
		*/

	public interface Handler {

		/**
		* <p>Procesa el evento para el cual está subscripto, sobre
		* la clave recibida. Este procedimiento se ejecuta en un
		* thread particular, y por lo tanto no se garantiza que las
		* operaciones que realice sean seguras frente a la
		* concurrencia de múltiples procesos.</p>
		*
		* <p>El reactor garantiza que durante toda la ejecución de
		* este método, no se procesarán eventos adicionales sobre la
		* misma clave (es decir, sobre el mismo canal).</p>
		*
		* @param key
		*	La clave asociada al evento para el cual este handler
		*	está subscripto y fué despachado.
		*/

		public void handle(SelectionKey key);

		/**
		* <p>Este método se ejecuta antes de que el handler sea
		* despachado, es decir, justo antes de que se ejecute
		* el método <b>handle</b>. Esto permite ejecutar una
		* subtarea inicial, antes del procesamiento principal.</p>
		*
		* <p>Se garantiza que la ejecución de este método en
		* los handlers subscriptos en un reactor se ejecutan de
		* manera secuencial en el <i>master thread</i>, y por lo
		* tanto, este método se ejecuta en un entorno
		* <b>thread-safe</b>.</p>
		*
		* @param key
		*	La clave asociada al evento para el cual este handler
		*	está subscripto y fué despachado.
		*/

		public void onSubmit(SelectionKey key);

		/**
		* <p>Este método se ejecuta justo después de que el
		* proceso principal (<i>handle</i>), finaliza. Permite
		* realizar tareas adicionales, como la liberación de
		* recursos, o la actualización de las claves a través del
		* repositorio global.</p>
		*
		* <p>La ejecución de este método se realiza dentro del
		* mismo thread que ejecutó el método <b>handle</b>, por lo
		* que se deben tomar los recaudos necesarios.</p>
		*
		* @param key
		*	La clave asociada al evento para el cual este handler
		*	está subscripto y fué despachado. Es la misma clave que
		*	recibieron los métodos <i>handle</i> y <i>onSubmit</i>.
		*/

		public void onResume(SelectionKey key);
	}
