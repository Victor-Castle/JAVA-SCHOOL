package network;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RequestType {
        LOGIN, GET_ALL_USERS, GET_ALL_ARCHIVES, SEARCH_USER, SEARCH_ARCHIVE,
        INSERT_USER, UPDATE_USER, DELETE_USER, INSERT_ARCHIVE, UPDATE_ARCHIVE, DELETE_ARCHIVE
    }

    private RequestType type;
    private Object[] parameters;

    public Request(RequestType type, Object... parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public RequestType getType() { return type; }
    public Object[] getParameters() { return parameters; }
}
