package ar.edu.itba.protos.config;

import java.security.KeyException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class UserMappingAdapter extends XmlAdapter<UserUpstreamMap, Map<String, Upstream>> {

    @Override
    public Map<String, Upstream> unmarshal(final UserUpstreamMap v) throws Exception {
        final Map<String, Upstream> map = new ConcurrentHashMap<>();
        for (final UserUpstreamPair mapping : v.mapping) {
            if (map.containsKey(mapping.user)) {
                throw new KeyException("key exists " + mapping.user);
            }
            map.put(mapping.user, mapping.upstream);
        }
        return map;
    }

    @Override
    public UserUpstreamMap marshal(final Map<String, Upstream> v) throws Exception {
        return new UserUpstreamMap(v);
    }

}
