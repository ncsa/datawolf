package edu.illinois.ncsa.incore.dao;

import java.io.Serializable;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.dao.IDao;

public abstract class AbstractIncoreDao<T, ID extends Serializable> implements IDao<T, ID> {
    @Inject
    @Named("incore.server")
    private String server;

    /**
     * IN-CORE Service endpoint
     * 
     * @return IN-CORE service endpoint
     */
    public String getServer() {
        if (!server.endsWith("/")) {
            this.server += "/";
        }

        return this.server;
    }

}
