
	package ar.edu.itba.protos;

	import java.io.IOException;
	import java.net.BindException;

	import ar.edu.itba.protos.transport.ServerEngine;
	import ar.edu.itba.protos.transport.ProxyServer;
	import ar.edu.itba.protos.transport.state.ProxyState;

	public final class MainEntryPoint {

		public static void main(String [] args) {

			System.out.println("Proxy Server");

			ServerEngine engine = new ServerEngine(new ProxyServer(new ProxyState()));
			try {

				engine.raise();
				engine.dispatch();
				engine.shutdown();
			}
			catch (BindException exception) {

				System.out.println("La dirección especificada ya se está usando");
			}
			catch (IOException exception) {

				exception.printStackTrace();
			}

			System.out.println("End.");
		}
	}
