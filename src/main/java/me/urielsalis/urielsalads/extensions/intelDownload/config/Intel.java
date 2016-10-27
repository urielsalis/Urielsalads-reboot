package me.urielsalis.urielsalads.extensions.intelDownload.config;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by cecis on 27/10/2016.
 */
public class Intel {
    String version;
    Date lastPartialUpdate;
    Date lastFullUpdate;
    String name;
    ArrayList<Driver> drivers;
    ArrayList<Ark> families;

    class Driver {
        String name;
        int epmID;
        ArrayList<Download> downloads;
    }
    
    class Ark {
        String name;
        ArrayList<Download> download;
    }

}
