package ar.edu.itba.protos.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

@Singleton
public class ConfigurationLoader {
    private ProxyConfiguration proxyConfig = new ProxyConfiguration();
    private UserMapping userMapping = new UserMapping();

    public ProxyConfiguration loadProxyConfig(final String filename) throws JAXBException {
        try {
            proxyConfig = loadResource(filename, ProxyConfiguration.class);
        } catch (final IOException e) {
        }
        return proxyConfig;
    }

    public UserMapping loadUserMapping(final String filename) throws JAXBException {
        try {
            userMapping = loadResource(filename, UserMapping.class);
        } catch (final IOException e) {
        }
        return userMapping;
    }

    public UserMapping getUserMapping() {
        return userMapping;
    }

    public void setUserMapping(final String filename, final UserMapping newMapping)
            throws IOException, JAXBException {
        saveResource(filename, newMapping);
        userMapping = newMapping;
    }

    public ProxyConfiguration getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(final String filename, final ProxyConfiguration proxyConfig)
            throws IOException, JAXBException {
        saveResource(filename, proxyConfig);
        this.proxyConfig = proxyConfig;
    }

    public void setProxyConfig(final String filename) throws IOException, JAXBException {
        setProxyConfig(filename, proxyConfig);
    }


    private <T> void saveResource(final String filename, final T object) throws IOException, JAXBException {
        final File file = setFile(filename);
        final JAXBContext context = JAXBContext.newInstance(object.getClass());
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(object, file);
    }

    @SuppressWarnings("unchecked")
    private <T> T loadResource(final String filename, final Class<T> cls) throws IOException, JAXBException {
        final File file = findFile(filename).orElseThrow(() -> new NoSuchFileException(filename));
        final JAXBContext context = JAXBContext.newInstance(cls);
        final Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();

        return (T) jaxbUnmarshaller.unmarshal(file);
    }

    /**
     * Perform a hierarchical file search for config files,
     * @param <T>
     * @throws NoSuchFileException
     */
    private Optional<File> findFile(final String configName) {
        return Arrays.stream(getConfigPaths(configName))
                .map(Path::toFile)
                .filter(File::canRead)
                .findFirst();
    }

    private File setFile(final String configName) throws NoSuchFileException {
        for (final Path p : getConfigPaths(configName)) {
            final File f = p.toFile();
            try {
                f.createNewFile();
                if (f.canWrite()) {
                    return f;
                }
            } catch (final IOException e) {}
        }
        throw new NoSuchFileException(configName);
    }

    private Path[] getConfigPaths(final String configName) {
        return new Path[]{
                Paths.get(System.getProperty("user.dir"), configName),
                Paths.get(System.getProperty("user.home"), "." + configName),
        };
    }
}
