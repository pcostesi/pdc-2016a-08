
package ar.edu.itba.protos.transport.concrete;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.protocol.admin.AdminProtocolParser;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;
import ar.edu.itba.protos.protocol.admin.command.CommandResult;
import ar.edu.itba.protos.transport.reactor.Event;
import ar.edu.itba.protos.transport.support.Attachment;
import ar.edu.itba.protos.transport.support.Interceptor;

/**
 * Este 'attachment' se utiliza para manipular el protocolo de administración,
 * por el cual se permite que un host remoto configure el funcionamiento y los
 * parámetros de ejecución de este servidor. Además, se expone sobre el mismo,
 * un sistema de recolección de métricas y estadísticas.
 */

public final class AdminAttachment extends Attachment implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(AdminAttachment.class);

    // El buffer de entrada y salida:
    private final ByteBuffer inboundBuffer = ByteBuffer.allocate(AdminAttachmentFactory.BUFFER_SIZE);
    private final CommandExecutor executor;
    private final AdminProtocolParser parser;
    private final Deque<ByteBuffer> outboundResults = new LinkedList<>();
    private int starterPosition = 0;

    @Inject
    public AdminAttachment(final CommandExecutor executor, final AdminProtocolParser parser) {
        this.executor = executor;
        this.parser = parser;
    }

    @Override
    public ByteBuffer getInboundBuffer() {
        return inboundBuffer;
    }

    private ByteBuffer getFirstOutboundBuffer() {
        ByteBuffer outbound = outboundResults.peek();

        if (outbound == null) {
            return ByteBuffer.wrap(new byte[] {});
        }

        while (outbound != null && outbound.remaining() == 0) {
            try {
                outbound = outboundResults.pop();
            } catch (final NoSuchElementException e) {
                return ByteBuffer.wrap(new byte[] {});
            }
        }

        return outbound;
    }

    @Override
    public boolean hasOutboundData() {
        final ByteBuffer outbound = getFirstOutboundBuffer();

        final int tempPosition = inboundBuffer.position();
        inboundBuffer.limit(inboundBuffer.position());
        inboundBuffer.position(starterPosition);
        inboundBuffer.compact();
        inboundBuffer.position(tempPosition - starterPosition);
        starterPosition = 0;
        return outbound.remaining() > 0;
    };

    @Override
    public ByteBuffer getOutboundBuffer() {
        final ByteBuffer outbound = getFirstOutboundBuffer();

        outbound.compact();
        outbound.limit(outbound.position());

        return outbound;
    }

    @Override
    public Interceptor getInterceptor() {
        return this;
    }

    @Override
    public void setDownstream(final SelectionKey downstream) {
        // Realiza un loop-back con el cliente:
        super.setDownstream(downstream);
        setUpstream(downstream);
    }

    @Override
    public void onUnplug(final Event event) {
        logger.debug("> Admin.onUnplug({})", event);
    }

    private static ByteBuffer serializeResult(final CommandResult result) {
        return ByteBuffer.wrap(result.getMessage().getBytes(StandardCharsets.US_ASCII));
    }

    private List<String[]> parseBuffer(final ByteBuffer buffer) {
        final int wtfPosition = buffer.position();
        buffer.position(starterPosition);
        final List<String[]> commands = parser.parse(buffer);
        starterPosition = buffer.position();
        buffer.position(wtfPosition);
        return commands;
    }


    /*
     ** Este método se ejecuta cada vez que un nuevo flujo de bytes se presenta
     * en el canal asociado (inbound). Este método puede modificar el buffer
     * libremente, inclusive modificando el límite, siempre y cuando este no se
     * reduzca por debajo de la posición recibida.
     */
    @Override
    public void consume(final ByteBuffer buffer) {
        final List<String[]> commands = parseBuffer(buffer);
        final List<ByteBuffer> results = commands.stream()
                .map(executor::execute)
                .map(AdminAttachment::serializeResult)
                .collect(Collectors.toList());
        outboundResults.addAll(results);
    }
}
