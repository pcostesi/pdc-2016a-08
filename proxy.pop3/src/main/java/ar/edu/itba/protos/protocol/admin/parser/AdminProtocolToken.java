package ar.edu.itba.protos.protocol.admin.parser;

import java.util.Arrays;

public enum AdminProtocolToken {
    ERR(""),
    SET_FILTER("set-filter", "filter-name"), UNSET_FILTER("unset-filter", "filter-name"),
    GET_ACTIVE_FILTERS("filters?"),
    SHOW_STATS_REPORT("report"),
    SHUTDOWN("shutdown"),
    MAP_USER("map", "user", "host", "port"), UNMAP_USER("unmap", "user"), GET_MAPPING_FOR_USER("map?", "user"),
    GET_DEFAULT_MAPPING("map-default?"),
    SET_DEFAULT_MAPPING("map-default", "host", "port"),
    SAVE_MAPPINGS("save-mappings");


    private final String token;
    private final String[] params;

    private AdminProtocolToken(final String token, final String... params) {
        this.token = token;
        this.params = params;
    }

    public String[] getParams() {
        return Arrays.copyOf(params, params.length);
    }

    public static AdminProtocolToken isCommand(final String token) {
        return Arrays.stream(AdminProtocolToken.values())
                .filter(c -> c.token.toLowerCase().equals(token.toLowerCase()))
                .findFirst()
                .orElse(ERR);
    }
}
