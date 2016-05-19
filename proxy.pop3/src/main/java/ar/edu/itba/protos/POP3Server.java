
	package ar.edu.itba.protos;

	import java.io.IOException;
	import java.net.BindException;

	import ar.edu.itba.protos.transport.Server;

	/*
	** [transport] -> "Capa de Transporte":
	**
	** Inicialmente, el servidor expone los servicios en la red. Para ello
	** debe generar un ServerSocket en una <ip> y un <port> especificados.
	** Luego comienza a aceptar conexiones entrantes. Para cada una de ellas
	** registra un nuevo Socket con su correspondiente Attachment. Por ahora
	** no es importante lo que este Attachment contiene.
	**
	** Cada socket debe registrarse para lectura y escritura. En ambos lados
	** el buffer de lectura debe estar registrado siempre (esto permite leer
	** siempre que haya algo, también permite PIPELINING y manejo de errores).
	**
	** En el caso de la escritura (onWrite), debe habilitarse primero del lado
	** del servidor para recibir el 'greeting-banner', luego del lado del
	** cliente para comenzar a recibir comandos. Debido a que inicialmente no
	** existe un servidor, es necesario tener una interfaz que defina como extraer
	** un <domain|ip> basado en el parsing del flujo de bytes.
	**
	** No es necesario escribir en ningún buffer si no se recibieron datos. Al
	** recibir bytes del cliente, se puede (potencialmente) conectar al
	** 'origin-server', luego de realizar un parsing previo. Este es el primer
	** mensaje que puede despacharse a través del Reactor (o de una interfaz).
	**
	** Luego de recibir bytes de un servidor, estos se forwardean realizando
	** escrituras en el socket del cliente, pero siempre que se haya leído algo.
	**
	** En cualquiera de los 2 casos se verifica que, para realizar una esritura,
	** el buffer luego del parsing NO DEBE ser vacío. Ahora bien, este parsing...
	**
	** ¿Se realiza solo del lado del cliente, o también del servidor? Debido a que
	** es necesario parsear mensajes SMTP, el parsing se debe realizar en los dos
	** extremos de la conexión.
	**
	** Es necesario manipular los errores debido a un cierre de conexión.
	** Si el cliente se desconecta, se cierra su conexión y la del origin-server.
	** Si el servidor desconecta, entonces se envía un comando -ERR al cliente y se
	** cierran los sockets. De forma similar frente a un <timeout>.
	*/

		/**
		** Ciclo principal de ejecución (master thread). Su función es
		** levantar el servidor en las direcciones y puertos especificados
		** y comenzar a recibir conexiones entrantes, las cuales serán
		** despachadas entre los 'workers' disponibles.
		*/

	public final class POP3Server {

		public static void main(String [] args) {

			System.out.println("POP-3 Proxy Server");

			// Se instancia un nuevo servidor:
			Server pop3 = new Server();

			// Se intenta aplicar 'binding' en la dirección especficada:
			if (!pop3.addListener("0.0.0.0", 110, null)) {

				System.out.println("No se pudo agregar el 'listener'.");
			}

			try {

				pop3.dispatch();
				pop3.shutdown();
			}
			catch (BindException exception) {

				System.out.println("La dirección especificada ya se está usando.");
			}
			catch (IOException exception) {

				exception.printStackTrace();
			}

			System.out.println("End.");
		}
	}
