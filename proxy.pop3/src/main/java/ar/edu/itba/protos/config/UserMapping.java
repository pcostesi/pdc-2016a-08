package ar.edu.itba.protos.config;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mapping-config")
public class UserMapping {

    @XmlElement(name = "mappings")
    @XmlJavaTypeAdapter(UserMappingAdapter.class)
    private final Map<String, Upstream> userMappings = new ConcurrentHashMap<>();

    @XmlElement(name = "default-upstream")
    private Upstream defaultUpstream;

    public Upstream getMappingForUsername(final String username) {
        final Upstream mapping = userMappings.get(username);
        if (mapping == null) {
            return defaultUpstream;
        }
        return mapping;
    }

    public void mapUserToUpstream(final String username, final Upstream upstream) {
        userMappings.put(username, upstream);
    }

    public void mapUserToUpstream(final String username, final String host, final int port) {
        userMappings.put(username, new Upstream(host, port));
    }

    public void unmapUser(final String username) {
        userMappings.remove(username);
    }

    public UserUpstreamPair[] getAllMappings() {
        return userMappings.entrySet().stream()
                .map(e -> new UserUpstreamPair(e.getKey(), e.getValue()))
                .toArray(i -> new UserUpstreamPair[i]);
    }

    public void setDefaultUpstream(final String host, final int port) {
        defaultUpstream = new Upstream(host, port);
    }

    public Upstream getDefaultUpstream() {
        return defaultUpstream;
    }

    @Override
    public String toString() {
        return String.format("(default) %s", getDefaultUpstream()) +
                Arrays.stream(getAllMappings())
                        .map(p -> "- " + p.toString())
                        .collect(Collectors.joining("\n"));
    }
}
