package ar.edu.itba.protos.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class UserUpstreamPair {
    @XmlAttribute
    public String user;
    @XmlElement
    public Upstream upstream;

    public UserUpstreamPair() {
    }

    public UserUpstreamPair(final String user, final Upstream upstream) {
        this.user = user;
        this.upstream = upstream;
    }

}
