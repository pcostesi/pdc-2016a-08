
	package ar.edu.itba.protos.transport.state;

	public final class ProxyState extends NetworkState {

		private int bufferSize = 4096;

		private int incomingConnections = 100;
		private int outgoingConections = 100;

		private String admin = "";

		public ProxyState() {

			return;
		}

		public int getBufferSize() {

			return bufferSize;
		}

		public int getIncomingConnections() {

			return incomingConnections;
		}

		public int getOutgoingConections() {

			return outgoingConections;
		}

		public String getAdmin() {

			return admin;
		}

		public void setBufferSize(int bufferSize) {

			this.bufferSize = bufferSize;
		}

		public void setIncomingConnections(int incomingConnections) {

			this.incomingConnections = incomingConnections;
		}

		public void setOutgoingConections(int outgoingConections) {

			this.outgoingConections = outgoingConections;
		}

		public void setAdmin(String admin) {

			this.admin = admin;
		}
	}
