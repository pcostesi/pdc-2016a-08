
package ar.edu.itba.protos.transport.support;

/**
 * <p>Esta interfaz permite instalar un <b>attachment</b>
 * dentro de un canal especificado de forma genérica,
 * delegando de esta forma el proceso de instanciación
 * del mismo hacia una subclase.<p>
 */

@FunctionalInterface
public interface AttachmentFactory {

    /**
     * <p>El método fábrica. Su función es generar un nuevo
     * <i>attachment</i>, el cual será asociado a cada nuevo
     * canal establecido en alguna de las interfaces del
     * servidor principal.</p>
     *
     * @return Devuelve un nuevo <i>attachment</i>.
     */
    public Attachment create();
}
