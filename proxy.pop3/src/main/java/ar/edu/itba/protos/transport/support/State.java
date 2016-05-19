
	package ar.edu.itba.protos.transport.support;

		/**
		** Más que una clase que provee comportamiento, su función
		** es la de actuar como contenedor de la configuración actual
		** del servidor, en general. Algunos de estos parámetros
		** pueden modificarse en tiempo de ejecución.
		*/

	public final class State {

		// Tamaño global de los buffers utilizados (4 Kb.):
		private int bufferSize = 4096;

		public State() {

			return;
		}

		/*
		** Getter's
		*/

		public int getBufferSize() {

			return bufferSize;
		}

		/*
		** Setter's
		*/

		public void setBufferSize(int bufferSize) {

			this.bufferSize = bufferSize;
		}
	}
