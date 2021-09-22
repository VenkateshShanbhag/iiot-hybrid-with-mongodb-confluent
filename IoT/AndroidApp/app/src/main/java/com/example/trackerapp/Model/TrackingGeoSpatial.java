package com.example.trackerapp.Model;

import com.example.trackerapp.Model.TrackingGeoSpatial_location;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import java.util.Date;
import org.bson.types.ObjectId;

public class TrackingGeoSpatial extends RealmObject {
    @PrimaryKey
    private ObjectId _id;

    private Date Timestamp;

    private String city;

    private TrackingGeoSpatial_location location;

    private String owner;

    private String partition_key;

    private String reg_num;

    // Standard getters & setters
    public ObjectId get_id() { return _id; }
    public void set_id(ObjectId _id) { this._id = _id; }

    public Date getTimestamp() { return Timestamp; }
    public void setTimestamp(Date Timestamp) { this.Timestamp = Timestamp; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public TrackingGeoSpatial_location getLocation() { return location; }
    public void setLocation(TrackingGeoSpatial_location location) { this.location = location; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getPartition_key() { return partition_key; }
    public void setPartition_key(String partition_key) { this.partition_key = partition_key; }

    public String getReg_num() { return reg_num; }
    public void setReg_num(String reg_num) { this.reg_num = reg_num; }

    public TrackingGeoSpatial() {}
    @Override
    public String toString() {
        return owner + " - " + reg_num;
    }
}
