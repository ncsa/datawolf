package edu.illinois.ncsa.datawolf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration properties external services such as the Data tillin service
 * 
 * @author Chris Navarro
 *
 */
public class DataWolf {
    private static final Logger log         = LoggerFactory.getLogger(DataWolf.class);

    private static DataWolf     instance;
    private String              datawolfURI = "";
    private String              dtsURI      = "";
    private String              dtsUser     = "";

    private DataWolf() {}

    public static DataWolf getInstance() {
        if (instance == null) {
            instance = new DataWolf();
        }

        return instance;
    }

    public String getDatawolfURI() {
        return datawolfURI;
    }

    public void setDatawolfURI(String datawolfURI) {
        this.datawolfURI = datawolfURI;
    }

    public String getDtsURI() {
        return dtsURI;
    }

    public void setDtsURI(String dtsURI) {
        this.dtsURI = dtsURI;
    }

    public String getDtsUser() {
        return dtsUser;
    }

    public void setDtsUser(String dtsUser) {
        this.dtsUser = dtsUser;
    }

}
