
package ar.edu.itba.protos.transport.concrete;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.protos.protocol.admin.AdminProtocolParser;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;
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
    private final ByteBuffer ioBuffer = ByteBuffer.allocate(AdminAttachmentFactory.BUFFER_SIZE);
    private final CommandExecutor executor;
    private final AdminProtocolParser parser;
    private final List<String> outboundResults = new ArrayList<>();

    @Inject
    public AdminAttachment(final CommandExecutor executor, final AdminProtocolParser parser) {
        this.executor = executor;
        this.parser = parser;
    }

    @Override
    public ByteBuffer getInboundBuffer() {
        return ioBuffer;
    }

    @Override
    public ByteBuffer getOutboundBuffer() {
        return ioBuffer;
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

    /*
     ** Este método se ejecuta cada vez que un nuevo flujo de bytes se presenta
     * en el canal asociado (inbound). Este método puede modificar el buffer
     * libremente, inclusive modificando el límite, siempre y cuando este no se
     * reduzca por debajo de la posición recibida.
     */
    @Override
    public void consume(final ByteBuffer buffer) {
        final List<String[]> commands = parser.parse(buffer);
        final List<String> results = commands.parallelStream()
                .map(executor::execute)
                .collect(Collectors.toList());
        outboundResults.addAll(results);

    }
}
