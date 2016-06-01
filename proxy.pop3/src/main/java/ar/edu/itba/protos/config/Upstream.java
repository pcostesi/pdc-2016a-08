package ar.edu.itba.protos.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Upstream {
    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @XmlValue
    private String host;

    @XmlAttribute(name = "port")
    private int port = 110;

    public Upstream(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public Upstream() {
    }
}