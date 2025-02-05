/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BeanImpl extends Validation {
    public enum Day {SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY}

    private Day enum_;
    private String string_;
    private StringBuffer stringBuffer_;
    private int int_;
    private Integer integer_;
    private char char_;
    private Character character_;
    private boolean boolean_;
    private Boolean booleanObject_;
    private byte byte_;
    private Byte byteObject_;
    private double double_;
    private Double doubleObject_;
    private float float_;
    private Float floatObject_;
    private long long_;
    private Long longObject_;
    private short short_ = -24;
    private Short shortObject_;
    private String stringFile_;
    private byte[] bytesFile_;
    private InputStream streamFile_;
    private Date date_;
    private Date dateFormatted_;
    private Date[] datesFormatted_;
    private SerializableParam serializableParam_;
    private SerializableParam[] serializableParams_;

    public void activateValidation() {
        addConstraint(new ConstrainedProperty("character").editable(false));
        addConstraint(new ConstrainedProperty("byte").editable(false));
        addConstraint(new ConstrainedProperty("stringFile"));
        addConstraint(new ConstrainedProperty("bytesFile"));
        addConstraint(new ConstrainedProperty("streamFile"));

        addGroup("somegroup")
            .addConstraint(new ConstrainedProperty("enum"))
            .addConstraint(new ConstrainedProperty("string"))
            .addConstraint(new ConstrainedProperty("int"))
            .addConstraint(new ConstrainedProperty("longObject"))
            .addConstraint(new ConstrainedProperty("short"));

        addGroup("anothergroup")
            .addConstraint(new ConstrainedProperty("double"))
            .addConstraint(new ConstrainedProperty("long"))
            .addConstraint(new ConstrainedProperty("shortObject"));

        SimpleDateFormat sf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
        sf.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
        addConstraint(new ConstrainedProperty("dateFormatted").format(sf));
        addConstraint(new ConstrainedProperty("datesFormatted").format(sf));
    }

    public void setEnum(Day day) {
        enum_ = day;
    }

    public Day getEnum() {
        return enum_;
    }

    public String getString() {
        return string_;
    }

    public void setString(String string) {
        string_ = string;
    }

    public StringBuffer getStringbuffer() {
        return stringBuffer_;
    }

    public void setStringbuffer(StringBuffer stringbuffer) {
        stringBuffer_ = stringbuffer;
    }

    public int getInt() {
        return int_;
    }

    public void setInt(int anInt) {
        int_ = anInt;
    }

    public Integer getInteger() {
        return integer_;
    }

    public void setInteger(Integer integer) {
        integer_ = integer;
    }

    public char getChar() {
        return char_;
    }

    public void setChar(char aChar) {
        char_ = aChar;
    }

    public Character getCharacter() {
        return character_;
    }

    public void setCharacter(Character character) {
        character_ = character;
    }

    public boolean isBoolean() {
        return boolean_;
    }

    public void setBoolean(boolean aBoolean) {
        boolean_ = aBoolean;
    }

    public Boolean getBooleanObject() {
        return booleanObject_;
    }

    public void setBooleanObject(Boolean aBooleanObject) {
        booleanObject_ = aBooleanObject;
    }

    public byte getByte() {
        return byte_;
    }

    public void setByte(byte aByte) {
        byte_ = aByte;
    }

    public Byte getByteObject() {
        return byteObject_;
    }

    public void setByteObject(Byte byteObject) {
        byteObject_ = byteObject;
    }

    public double getDouble() {
        return double_;
    }

    public void setDouble(double aDouble) {
        double_ = aDouble;
    }

    public Double getDoubleObject() {
        return doubleObject_;
    }

    public void setDoubleObject(Double doubleObject) {
        doubleObject_ = doubleObject;
    }

    public float getFloat() {
        return float_;
    }

    public void setFloat(float aFloat) {
        float_ = aFloat;
    }

    public Float getFloatObject() {
        return floatObject_;
    }

    public void setFloatObject(Float floatObject) {
        floatObject_ = floatObject;
    }

    public long getLong() {
        return long_;
    }

    public void setLong(long aLong) {
        long_ = aLong;
    }

    public Long getLongObject() {
        return longObject_;
    }

    public void setLongObject(Long longObject) {
        longObject_ = longObject;
    }

    public short getShort() {
        return short_;
    }

    public void setShort(short aShort) {
        short_ = aShort;
    }

    public Short getShortObject() {
        return shortObject_;
    }

    public void setShortObject(Short shortObject) {
        shortObject_ = shortObject;
    }

    public void setStringFile(String stringFile) {
        stringFile_ = stringFile;
    }

    public String getStringFile() {
        return stringFile_;
    }

    public void setBytesFile(byte[] bytesFile) {
        bytesFile_ = bytesFile;
    }

    public byte[] getBytesFile() {
        return bytesFile_;
    }

    public void setStreamFile(InputStream streamFile) {
        streamFile_ = streamFile;
    }

    public InputStream getStreamFile() {
        return streamFile_;
    }

    public void setDate(Date date) {
        date_ = date;
    }

    public Date getDate() {
        return date_;
    }

    public void setDateFormatted(Date dateFormatted) {
        dateFormatted_ = dateFormatted;
    }

    public Date getDateFormatted() {
        return dateFormatted_;
    }

    public void setDatesFormatted(Date[] datesFormatted) {
        datesFormatted_ = datesFormatted;
    }

    public Date[] getDatesFormatted() {
        return datesFormatted_;
    }

    public void setSerializableParam(SerializableParam serializableParam) {
        serializableParam_ = serializableParam;
    }

    public SerializableParam getSerializableParam() {
        return serializableParam_;
    }

    public void setSerializableParams(SerializableParam[] serializableParams) {
        serializableParams_ = serializableParams;
    }

    public SerializableParam[] getSerializableParams() {
        return serializableParams_;
    }

    public static class SerializableParam implements Serializable {
        private int number_ = -1;
        private String string_ = null;

        public SerializableParam(int number, String string) {
            number_ = number;
            string_ = string;
        }

        public void setNumber(int number) {
            number_ = number;
        }

        public int getNumber() {
            return number_;
        }

        public void setString(String string) {
            string_ = string;
        }

        public String getString() {
            return string_;
        }

        public String toString() {
            return number_ + ":" + string_;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (null == other) {
                return false;
            }

            if (!(other instanceof SerializableParam)) {
                return false;
            }

            SerializableParam other_serializable = (SerializableParam) other;
            if (!other_serializable.getString().equals(getString())) {
                return false;
            }
            if (other_serializable.getNumber() != getNumber()) {
                return false;
            }

            return true;
        }
    }
}

