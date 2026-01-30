package org.example;

import java.io.*;

public class Tape {

    private final String fileName;
    private RandomAccessFile file;
    private final int recordSize;
    private final int blockSize;

    private final byte[] readBuffer;
    private int readBufferPos = 0;
    private int readBufferLimit = 0;

    private final byte[] writeBuffer;
    private int writeBufferPos = 0;

    private long diskReads = 0;
    private long diskWrites = 0;

    public Tape(String fileName, int recordSize, int blockSize) throws IOException {
        this.fileName = fileName;
        this.recordSize = recordSize;
        this.blockSize = Math.max(blockSize, recordSize);

        this.readBuffer = new byte[this.blockSize];
        this.writeBuffer = new byte[this.blockSize];

        File f = new File(fileName);
        if (!f.exists()) {
            f.createNewFile();
        }
    }

    private boolean fillReadBuffer() throws IOException {
        int leftover = readBufferLimit - readBufferPos;
        if (leftover > 0) {
            System.arraycopy(readBuffer, readBufferPos, readBuffer, 0, leftover);
        }

        readBufferPos = 0;
        int bytesRead = file.read(readBuffer, leftover, blockSize - leftover);

        if (bytesRead > 0) {
            diskReads++;
            readBufferLimit = leftover + bytesRead;
        } else {
            readBufferLimit = leftover;
        }

        return readBufferLimit >= recordSize;
    }

    public Record readRecord() throws IOException {
        if (readBufferPos + recordSize > readBufferLimit) {
            if (!fillReadBuffer()) {
                return null;
            }
        }

        if (readBufferPos + recordSize > readBufferLimit) {
            return null;
        }

        DataInputStream dis = new DataInputStream(
                new ByteArrayInputStream(readBuffer, readBufferPos, recordSize)
        );

        Record record = new Record();
        if (!record.read(dis)) {
            return null;
        }

        readBufferPos += recordSize;
        return record;
    }

    public void writeRecord(Record record) throws IOException {
        if (writeBufferPos + recordSize > blockSize) {
            flush();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(recordSize);
        record.write(new DataOutputStream(baos));
        byte[] recordBytes = baos.toByteArray();

        System.arraycopy(recordBytes, 0, writeBuffer, writeBufferPos, recordBytes.length);
        writeBufferPos += recordBytes.length;
    }

    public void flush() throws IOException {
        if (writeBufferPos > 0) {
            file.write(writeBuffer, 0, writeBufferPos);
            diskWrites++;
            writeBufferPos = 0;
        }
    }

    public void close() throws IOException {
        flush();
        if (file != null) {
            file.close();
            file = null;
        }
    }

    public void reset(String mode) throws IOException {
        if (file != null) {
            close();
        }
        file = new RandomAccessFile(fileName, mode);
        file.seek(0);
        readBufferPos = 0;
        readBufferLimit = 0;
        writeBufferPos = 0;
    }

    public void truncate() throws IOException {
        if (file != null) {
            file.setLength(0);
        }
    }

    public long getDiskReads() { return diskReads; }
    public long getDiskWrites() { return diskWrites; }
    public String getFileName() { return fileName; }
}
