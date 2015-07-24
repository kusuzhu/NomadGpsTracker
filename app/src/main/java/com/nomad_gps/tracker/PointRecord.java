package com.nomad_gps.tracker;

/**
 * Created by kuandroid on 7/3/15.
 */
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "buffer")
public class PointRecord {
    @DatabaseField(generatedId = true)
    private int id; //ID в локальной базе данных
    @DatabaseField
    private int timestamp; 	// Unix time
    @DatabaseField
    private int priority;
    @DatabaseField
    private int latitude;
    @DatabaseField
    private int longitude;
    @DatabaseField
    private int altitude; 	// meters
    @DatabaseField
    private int angle; 		// degrees
    @DatabaseField
    private int satellites;
    @DatabaseField
    private int speed;
    @DatabaseField
    private int signal;
    @DatabaseField
    private int battery;
    public PointRecord(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getSatellites() {
        return satellites;
    }

    public void setSatellites(int satellites) {
        this.satellites = satellites;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }



    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb
                .append("id:").append(id)
                .append("; timestamp:").append(timestamp)
                .append("; priority:").append(priority)
                .append("; latitude:").append(latitude)
                .append("; longitude:").append(longitude)
                .append("; altitude:").append(altitude)
                .append("; angle:").append(angle)
                .append("; satellites:").append(satellites)
                .append("; speed:").append(speed)
                .append("; battery:").append(battery)
                .append("; signal:").append(signal)
        ;
        return sb.toString();
    }



}