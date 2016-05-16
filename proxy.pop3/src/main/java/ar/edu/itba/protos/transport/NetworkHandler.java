
	package ar.edu.itba.protos.transport;

	import java.io.IOException;
	import java.nio.channels.SelectionKey;
	import java.nio.channels.Selector;
	import java.nio.channels.ServerSocketChannel;

	import ar.edu.itba.protos.transport.state.NetworkState;

	public interface NetworkHandler {

		NetworkState getState();

		void setSelector(Selector selector);
		void setServerSocket(ServerSocketChannel serverSocket);

		void onAccept(SelectionKey key) throws IOException;
		void onConnect(SelectionKey key) throws IOException;
		void onRead(SelectionKey key) throws IOException;
		void onWrite(SelectionKey key) throws IOException;
	}
