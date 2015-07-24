package com.nomad_gps.tracker;

/**
 * Created by kuandroid on 7/3/15.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeltonikaEmulator {

    private String imei;
    private AvlRecordGenerator generator;
    private static final int RECORDS_LIMIT = 3350;
    private static final int CODEC_ID =(byte) 0x08;
    private static final Byte[] PACKAGE_HEADER = new Byte[]  {(byte)0x00,(byte) 0x00, (byte)0x00, (byte)0x00 };

    public TeltonikaEmulator(String imei, AvlRecordGenerator generator) {
        this.imei = imei;
        this.generator = generator;
    }

    public TeltonikaEmulator(String imei) {
        this(imei, new AvlRecordGenerator());
    }

    //упаковка imei в List<Byte>
    public List<Byte> getImei() {
        Byte [] imeiByte = new Byte[imei.length()];
        for (int i = 0; i < imeiByte.length; i++) {
            imeiByte[i] = (byte) imei.charAt(i);
        }
        return new ArrayList<Byte>(Arrays.asList(imeiByte));
    }

    //аутентификация по imei
    public List<Byte> getAuthentication() {
        List<Byte> data = new ArrayList<Byte>();
        data.add((byte) 0x00);
        int length = imei.length();
        data.add((byte) length);//длина imei
        List<Byte> imei = getImei();
        data.addAll(imei);//сам imei
        return data;
    }

    public List<Byte> generatePackage(List<PointRecord> list) {
        if(list.size() > RECORDS_LIMIT) return new ArrayList<Byte>();
        //add header
        List<Byte> data = new ArrayList<Byte>();
        data.addAll(new ArrayList<Byte>(Arrays.asList( PACKAGE_HEADER )));
        //insert avl data
        List<Byte> avlData = getAvlData(list);
        data.addAll(Converter.intToByteArrayList(avlData.size(), 4));
        data.addAll(avlData);
        data.addAll(Converter.intToByteArrayList(getCrc(avlData), 4));

        return data;
    }

    private List<Byte> getAvlData(List<PointRecord> list) {
        List<Byte> avlData = new ArrayList<Byte>();
        avlData.add((byte) CODEC_ID);
        avlData.add((byte) list.size());
        for (int i = 0; i < list.size(); i++) {
            avlData.addAll(generator.generateAvlRecord(list.get(i)));
        }
        avlData.add((byte) list.size());
        return avlData;
    }

    private int getCrc(List<Byte> avlData) {
        byte[] avlDataByteArray = new byte[avlData.size()];
        for (int i = 0; i < avlDataByteArray.length; i++) {
            avlDataByteArray[i] = avlData.get(i);
        }
        CRC crc = new CRC();
        crc.update(avlDataByteArray);
        return crc.getValue();
    }
}