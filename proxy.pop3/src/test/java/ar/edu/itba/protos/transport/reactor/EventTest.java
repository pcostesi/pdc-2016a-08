
	package ar.edu.itba.protos.transport.reactor;

	import static org.junit.Assert.*;
	import static org.mockito.Mockito.*;

	import java.nio.channels.CancelledKeyException;
	import java.nio.channels.SelectionKey;

	import org.junit.Before;
	import org.junit.BeforeClass;
	import org.junit.Test;
	import org.mockito.Mockito;
	import org.mockito.invocation.InvocationOnMock;
	import org.mockito.stubbing.Answer;

	public class EventTest {

		// Una clave válida:
		private static final SelectionKey validKey
			= Mockito.mock(SelectionKey.class);

		// Una clave que fue cancelada y es inválida:
		private static final SelectionKey cancelledKey
			= Mockito.mock(SelectionKey.class);

		// Opciones de interés para una clave:
		private static int options = 0;

		@BeforeClass
		public static void mockCancelledKey() {

			when(cancelledKey.interestOps(anyInt()))
				.thenThrow(CancelledKeyException.class);
			when(cancelledKey.interestOps())
				.thenThrow(CancelledKeyException.class);
			when(cancelledKey.isValid())
				.thenReturn(false);
		}

		@BeforeClass
		public static void mockValidKey() {

			doAnswer(new Answer<SelectionKey>() {

				@Override
				public SelectionKey answer(InvocationOnMock invocation) {

					options = invocation.getArgument(0);
					return validKey;
				}
			}).when(validKey).interestOps(anyInt());

			doAnswer(new Answer<Integer>() {

				@Override
				public Integer answer(InvocationOnMock invocation) {

					return options;
				}
			}).when(validKey).interestOps();

			when(validKey.isValid())
				.thenReturn(true);
		}

		@Before
		public void init() {

			/*
			** No está bien modificar un campo estático desde
			** un método de instancia, pero para simplificar la
			** definición del mock 'validKey', lo hacemos de todas
			** formas.
			**
			** FindBugs reporta ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD,
			** pero el mismo puede suprimirse mediante @SuppressFBWarnings
			*/
			options = 0;
		}

		@Test
		public void allEventsAreDisjoint() {

			int options = 0;
			for (Event event : Event.values())
				options |= event.getOptions();

			assertTrue(Event.values().length == Integer.bitCount(options));
		}

		@Test
		public void allEventsAreSingleBits() {

			for (Event event : Event.values())
				assertEquals(1, Integer.bitCount(event.getOptions()));
		}

		@Test
		public void enableIsInvertible() {

			for (Event event : Event.values()) {

				Event.enable(validKey, event.getOptions());
				assertEquals(1, Integer.bitCount(validKey.interestOps()));
				Event.disable(validKey, event.getOptions());
				assertEquals(0, validKey.interestOps());
			}
		}

		@Test
		public void disableHasNoSideEffects() {

			validKey.interestOps(-1);
			for (Event event : Event.values()) {

				Event.disable(validKey, event.getOptions());
				assertEquals(
					Integer.SIZE - 1,
					Integer.bitCount(validKey.interestOps()));
				Event.enable(validKey, event.getOptions());
				assertEquals(-1, validKey.interestOps());
			}
		}

		@Test(expected = CancelledKeyException.class)
		public void enableThrowsExceptions() {

			for (Event event : Event.values())
				Event.enable(cancelledKey, event.getOptions());
		}

		@Test(expected = CancelledKeyException.class)
		public void disableThrowsExceptions() {

			for (Event event : Event.values())
				Event.disable(cancelledKey, event.getOptions());
		}
	}
