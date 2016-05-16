
	package ar.edu.itba.protos.transport.state;

	public class NetworkState {

		protected String IP = "0.0.0.0";
		protected int port = 8080;

		protected long timeout = 10000;

		public NetworkState() {

			return;
		}

		public String getIP() {

			return IP;
		}

		public int getPort() {

			return port;
		}

		public long getTimeout() {

			return timeout;
		}

		public void setIP(String IP) {

			this.IP = IP;
		}

		public void setPort(short port) {

			this.port = port;
		}

		public void setTimeout(int timeout) {

			this.timeout = timeout;
		}
	}
