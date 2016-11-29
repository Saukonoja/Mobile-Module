package fi.jamk.signalstrength;

import java.util.UUID;
import java.util.Date;

public class DataLocation {

    // variables for datalocation object
    public UUID block_id;
    public Date insertion_time;
    public double lat;
    public double lon;
    public int gsm;
    public int cdma;
    public int evdo;
    public int lts;

    public DataLocation(){
        super();
    }

    //constructor with variables
    public DataLocation(UUID block_id, Date insertion_time, double lat, double lon, int gsm, int cdma, int evdo, int lts) {
        this.block_id = block_id;
	    this.insertion_time = insertion_time;
        this.lat = lat;
        this.lon = lon;
        this.gsm = gsm;
        this.cdma = cdma;
        this.evdo = evdo;
        this.lts = lts;
    }
    //getters
    public UUID getBlock_id() {
        return block_id;
    }
    public Date getInsertion_time() {
	return insertion_time;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public int getGsm() {
        return gsm;
    }
    public int getCdma() {
        return cdma;
    }
    public int getEvdo() {
        return evdo;
    }
    public int getLts() {
        return lts;
    }



}

