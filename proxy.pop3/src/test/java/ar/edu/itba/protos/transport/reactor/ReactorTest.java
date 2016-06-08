
	package ar.edu.itba.protos.transport.reactor;

	import static org.junit.Assert.*;
	import static org.junit.Assume.*;
	import static org.mockito.Mockito.*;

	import java.nio.channels.SelectionKey;

	import org.junit.AfterClass;
	import org.junit.Before;
	import org.junit.BeforeClass;
	import org.junit.Test;
	import org.mockito.Mockito;

	import com.google.inject.Guice;
	import com.google.inject.Injector;
	import com.google.inject.Stage;

	import ar.edu.itba.protos.DIModule;

	public class ReactorTest {

		private final static Injector injector
			= Guice.createInjector(Stage.PRODUCTION, new DIModule());

		private static Reactor reactor = null;

		private SelectionKey key = null;
		private Handler handler = null;
		private Handler otherHandler = null;
		private Handler readHandler = null;

		@BeforeClass
		public static void injectReactor() {

			reactor = injector.getInstance(Reactor.class);
		}

		@AfterClass
		public static void blockReactor() {

			reactor.block();
		}

		@Before
		public void init() {

			reactor.unplug();

			key = Mockito.mock(SelectionKey.class);
			handler = Mockito.mock(Handler.class);
			otherHandler = Mockito.mock(Handler.class);
			readHandler = Mockito.mock(Handler.class);

			assumeNotNull(
				reactor,
				key,
				handler, otherHandler, readHandler);
		}

		@Test
		public void reactorIsInitiallyEmpty() {

			for (Event event : Event.values())
				assertEquals(0, reactor.getHandlers(event));
		}

		@Test
		public void allEventsAreHandled() {

			assertEquals(Event.values().length, reactor.getEvents());
		}

		@Test
		public void cannotPlugSameHandlerTwice() {

			for (int i = 0; i < 5; ++i)
				for (Event event : Event.values())
					reactor.add(handler, event);

			for (Event event : Event.values())
				assertEquals(1, reactor.getHandlers(event));
		}

		@Test
		public void canPlugAHandlerForAllEvents() {

			int options = 0;
			for (Event event : Event.values())
				options |= event.getOptions();

			reactor.add(handler, options);
			for (Event event : Event.values())
				assertEquals(1, reactor.getHandlers(event));
		}

		@Test
		public void addIsInvertible() {

			for (Event event : Event.values()) {

				reactor.add(handler, event);
				assertEquals(1, reactor.getHandlers(event));
				reactor.remove(handler, event);
				assertEquals(0, reactor.getHandlers(event));
			}
		}

		@Test
		public void removeHasNoSideEffects() {

			reactor.add(handler, -1);
			for (Event event : Event.values()) {

				reactor.remove(handler, event);
				assertEquals(0, reactor.getHandlers(event));

				for (Event otherEvent : Event.values())
					if (otherEvent != event)
						assertEquals(1, reactor.getHandlers(otherEvent));

				reactor.add(handler, event);
				assertEquals(1, reactor.getHandlers(event));
			}
		}

		@Test
		public void canUnplugAHandlerForAllEvents() {

			reactor.add(handler, -1);
			reactor.remove(handler);
			for (Event event : Event.values())
				assertEquals(0, reactor.getHandlers(event));
		}

		@Test
		public void canUnplugAll() {

			reactor
				.add(Mockito.mock(Handler.class), -1)
				.add(Mockito.mock(Handler.class), -1)
				.unplug();

			for (Event event : Event.values())
				assertEquals(0, reactor.getHandlers(event));
		}

		@Test
		public void isOnMethodIsConsistent() {

			doReturn(true).when(key).isValid();

			for (Event event : Event.values()) {

				when(key.readyOps()).thenReturn(event.getOptions());
				int eventsOn = 0;
				for (Event otherEvent : Event.values())
					if (Reactor.isOn(otherEvent, key))
						++eventsOn;
				assertEquals(1, eventsOn);
			}

			doReturn(false).when(key).isValid();
			when(key.readyOps()).thenReturn(-1);
			for (Event event : Event.values())
				assertFalse(Reactor.isOn(event, key));
		}

		@Test
		public void dispatchOnlyOnceForEachHandler() {

			reactor
				.add(handler, -1)
				.add(otherHandler, -1)
				.add(readHandler, Event.READ);

			when(key.isValid()).thenReturn(true);
			for (Event event : Event.values()) {

				when(key.readyOps()).thenReturn(event.getOptions());
				reactor.dispatch(key);
			}

			try {

				/* Debido a que utiliza threads, es necesario
				** darle algo de tiempo para que 'verify' no falle.
				*/
				Thread.sleep(3000);
			}
			catch (InterruptedException spurious) {}

			verify(handler, times(reactor.getEvents())).handle(key);
			verify(otherHandler, times(reactor.getEvents())).handle(key);
			verify(readHandler).handle(key);
		}
	}
