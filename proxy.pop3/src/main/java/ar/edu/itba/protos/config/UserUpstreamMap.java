package ar.edu.itba.protos.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class UserUpstreamMap {

    @XmlElement(name = "map")
    public List<UserUpstreamPair> mapping = new ArrayList<>();

    public UserUpstreamMap(final Map<String, Upstream> map) {
        mapping = map.entrySet().stream()
                .map(e -> new UserUpstreamPair(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public UserUpstreamMap() {
    }

}
