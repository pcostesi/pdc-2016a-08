
package ar.edu.itba.protos.transport.concrete;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.edu.itba.protos.protocol.admin.AdminProtocolParser;
import ar.edu.itba.protos.protocol.admin.CommandExecutor;
import ar.edu.itba.protos.transport.support.Attachment;
import ar.edu.itba.protos.transport.support.AttachmentFactory;

/**
 * <p>El objetivo de esta fábrica es generar los <b>attachment</b>
 * asociados a conexiones de administración, es decir, a las
 * conexiones hacia este servidor, cuyo objetivo es modificar
 * los parámetros de configuración del mismo, o bien, obtener
 * las estadísticas y métricas del estado actual del sistema.</p>
 */

@Singleton
public final class AdminAttachmentFactory implements AttachmentFactory {

    // TODO: Debería obtenerse por configuración:
    public static final int BUFFER_SIZE = 8192;
    private final CommandExecutor executor;

    @Inject
    public AdminAttachmentFactory(final CommandExecutor executor) {
        this.executor = executor;
    }

    /**
     * <p>Genera un nuevo <i>attachment</i> de administración.</p>
     *
     * @return Devuelve un objeto <b>AdminAttachment</b>, el cual
     *	se encarga de gestionar la administración remota del
     *	servidor.
     */
    @Override
    public Attachment create() {
        return new AdminAttachment(executor, new AdminProtocolParser());
    }
}
