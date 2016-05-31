package ar.edu.itba.protos.config;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    public Map<String, Upstream> userMappings = new ConcurrentHashMap<>();

    @XmlElement(name = "default-upstream")
    public Upstream defaultUpstream;

    public Optional<Upstream> getMappingForUsername(final String username) {
        return Optional.ofNullable(userMappings.get(username));
    }

    public void mapUserToUpstream(final String username, final Upstream upstream) {
        userMappings.put(username, upstream);
    }

    public void mapUserToUpstream(final String username, final String host, final int port) {
        userMappings.put(username, new Upstream(host, port));
    }

}
