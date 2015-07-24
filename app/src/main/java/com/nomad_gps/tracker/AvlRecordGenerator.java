package com.nomad_gps.tracker;

/**
 * Created by kuandroid on 7/3/15.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AvlRecordGenerator {
    public List<Byte> generateAvlRecord(PointRecord record) {
        List<Byte> result = new ArrayList<Byte>();

        result.addAll(avlTimeStamp(record));
        result.addAll(avlPriority(record));
        result.addAll(avlLongitude(record));
        result.addAll(avlLatitude(record));
        result.addAll(avlAltitude(record));
        result.addAll(avlAngle(record));
        result.addAll(avlSatellitesCount(record));
        result.addAll(avlSpeed(record));
        result.addAll(avlIOEvent());
        result.addAll(avlIOCount());
        result.addAll(avlION1());
        result.addAll(avlIOBatteryId());
        result.addAll(avlIOBattery(record));
        result.addAll(avlIOSignalId());
        result.addAll(avlIOSignal(record));
        result.add((byte) 0x00);
        result.add((byte) 0x00);
        result.add((byte) 0x00);

        return result;
    }

    private List<Byte> avlTimeStamp(PointRecord record) {
        return Converter.intToByteArrayList((long)record.getTimestamp() * 1000, 8);
    }

    private Collection<? extends Byte> avlPriority(PointRecord record) {
        return Converter.intToByteArrayList(record.getPriority(), 1);
    }

    private Collection<? extends Byte> avlLatitude(PointRecord record) {
        return Converter.intToByteArrayList(record.getLatitude(), 4);
    }

    private Collection<? extends Byte> avlLongitude(PointRecord record) {
        return Converter.intToByteArrayList(record.getLongitude(), 4);
    }

    private Collection<? extends Byte> avlAltitude(PointRecord record) {
        return Converter.intToByteArrayList(record.getAltitude(), 2);
    }

    private Collection<? extends Byte> avlAngle(PointRecord record) {
        return Converter.intToByteArrayList(record.getAngle(), 2);
    }

    private Collection<? extends Byte> avlSatellitesCount(PointRecord record) {
        return Converter.intToByteArrayList(record.getSatellites(), 1);
    }

    private Collection<? extends Byte> avlSpeed(PointRecord record) {
        return Converter.intToByteArrayList(record.getSpeed(), 2);
    }
    private Collection<? extends Byte> avlIOEvent() {
        return Converter.intToByteArrayList(0, 1);
    }
    private Collection<? extends Byte> avlIOCount() {
        return Converter.intToByteArrayList(2, 1);
    }
    private Collection<? extends Byte> avlION1() {
        return Converter.intToByteArrayList(2, 1);
    }

    private Collection<? extends Byte> avlIOBatteryId() {
        return Converter.intToByteArrayList(67, 1);
    }
    private Collection<? extends Byte> avlIOBattery(PointRecord record) {
        return Converter.intToByteArrayList(record.getBattery(), 1);
    }

    private Collection<? extends Byte> avlIOSignalId() {
        return Converter.intToByteArrayList(100, 1);
    }
    private Collection<? extends Byte> avlIOSignal(PointRecord record) {
        return Converter.intToByteArrayList(record.getSignal(), 1);
    }
}
