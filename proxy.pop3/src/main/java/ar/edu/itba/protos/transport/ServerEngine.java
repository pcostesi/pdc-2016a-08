
	package ar.edu.itba.protos.transport;

	import java.io.IOException;
	import java.net.InetSocketAddress;
	import java.nio.channels.Channel;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.Selector;
	import java.nio.channels.ServerSocketChannel;
	import java.util.Iterator;
	import java.util.Set;

	import ar.edu.itba.protos.transport.state.NetworkState;

	public final class ServerEngine {

		private ServerSocketChannel serverSocket;
		private NetworkHandler handler;
		private Selector selector;
		private NetworkState state;

		public ServerEngine(NetworkHandler handler) {

			this.handler = handler;

			try {

				selector = Selector.open();
				handler.setSelector(selector);
				state = handler.getState();
			}
			catch (IOException exception) {

				exception.printStackTrace();
			}
		}

		public void raise() throws IOException {

			InetSocketAddress address = new InetSocketAddress(state.getIP(), state.getPort());

			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(address);

			// El ServerSocket no posee 'Attachment':
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			handler.setServerSocket(serverSocket);
		}

		public void dispatch() throws IOException {

			while (true) {

				if (0 < selector.select(state.getTimeout())) {

					Set<SelectionKey> keys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();

					while (iterator.hasNext()) {

						SelectionKey key = iterator.next();
						System.out.println("Select: " + key);

						if (key.isValid() && key.isAcceptable()) handler.onAccept(key);
						if (key.isValid() && key.isConnectable()) handler.onConnect(key);
						if (key.isValid() && key.isReadable()) handler.onRead(key);
						if (key.isValid() && key.isWritable()) handler.onWrite(key);

						iterator.remove();
					}
				}
				else {

					System.out.println("Selector Timeout");
					return;
				}
			}
		}

		public void shutdown() throws IOException {

			Set<SelectionKey> keys = selector.keys();

			for (SelectionKey key : keys) {

				/**/System.out.println(key);
				key.cancel();
				Channel channel = key.channel();
				if (channel.isOpen()) channel.close();
			}

			if (serverSocket.isOpen()) serverSocket.close();
			if (selector.isOpen()) selector.close();
		}
	}
