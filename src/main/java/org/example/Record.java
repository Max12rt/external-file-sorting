package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;

public class Record implements Comparable<Record> {

    private int a;
    private int y;
    private int c;
    private int z;
    private int x;

    public static final int RECORD_SIZE = 5 * Integer.BYTES;

    private double gValue;

    public Record() {
        this.gValue = 0;
    }

    public Record(int a, int y, int c, int z, int x) {
        this.a = a;
        this.y = y;
        this.c = c;
        this.z = z;
        this.x = x;
        calculateG();
    }


    public final void calculateG() {
        double x2 = Math.pow(x, 2);
        double c3 = Math.pow(c, 3);
        double z4 = Math.pow(z, 4);
        double y7 = Math.pow(y, 7);

        this.gValue = 10.0 * a * (x2 + 3.0 * c3 * z4 - 5.0 * y7);
    }
    
    public double getGValue() {
        return this.gValue;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(a);
        dos.writeInt(y);
        dos.writeInt(c);
        dos.writeInt(z);
        dos.writeInt(x);
    }


    public boolean read(DataInputStream dis) throws IOException {
        try {
            a = dis.readInt();
            y = dis.readInt();
            c = dis.readInt();
            z = dis.readInt();
            x = dis.readInt();

            calculateG();
            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    @Override
    public int compareTo(Record other) {
        return Double.compare(this.gValue, other.getGValue());
    }

    @Override
    public String toString() {

        return String.format("a=%d, y=%d, c=%d, z=%d, x=%d | g=%.2f",
                a, y, c, z, x, getGValue());
    }
}
