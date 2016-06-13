
package ar.edu.itba.protos.transport.concrete;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ar.edu.itba.protos.transport.support.Attachment;
import ar.edu.itba.protos.transport.support.AttachmentFactory;

/**
 * <p>El objetivo de esta fábrica es generar los <b>attachment</b>
 * asociados a conexiones de administración, es decir, a las
 * conexiones hacia este servidor, cuyo objetivo es modificar
 * los parámetros de configuración del mismo, o bien, obtener
 * las estadísticas y métricas del estado actual del sistema.</p>
 */

public final class AdminAttachmentFactory implements AttachmentFactory {

    // TODO: Debería obtenerse por configuración:
    public static final int BUFFER_SIZE = 8192;
    private final Injector injector = Guice.createInjector();
    /**
     * <p>Genera un nuevo <i>attachment</i> de administración.</p>
     *
     * @return Devuelve un objeto <b>AdminAttachment</b>, el cual
     *	se encarga de gestionar la administración remota del
     *	servidor.
     */
    @Override
    public Attachment create() {
        return injector.getInstance(AdminAttachment.class);
    }
}
