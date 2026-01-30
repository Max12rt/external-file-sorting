package org.example;

import java.io.IOException;

public class NaturalMergeSorter {

    private final int recordSize;
    private final int blockSize;

    private long totalReads = 0;
    private long totalWrites = 0;
    private int phases = 0;

    public NaturalMergeSorter(int recordSize, int blockSize) {
        this.recordSize = recordSize;
        this.blockSize = blockSize;
    }

    public SortStats sort(String inputFileName) throws IOException {
        String TAPE_A = inputFileName;
        String TAPE_B = "tape_b.bin";
        String TAPE_C = "tape_c.bin";

        Tape tapeA = new Tape(TAPE_A, recordSize, blockSize);
        Tape tapeB = new Tape(TAPE_B, recordSize, blockSize);
        Tape tapeC = new Tape(TAPE_C, recordSize, blockSize);

        System.out.println("Rozpoczęto sortowanie pliku: " + inputFileName);
        long startTime = System.currentTimeMillis();

        totalReads = 0;
        totalWrites = 0;
        phases = 0;

        while (true) {
            phases++;
            System.out.println("Faza " + phases + ": Dystrybucja");

            tapeA.reset("r");
            tapeB.reset("rw");
            tapeC.reset("rw");
            tapeB.truncate();
            tapeC.truncate();

            int distributedRuns = distribute(tapeA, tapeB, tapeC);

            totalReads += tapeA.getDiskReads() + tapeB.getDiskReads() + tapeC.getDiskReads();
            totalWrites += tapeA.getDiskWrites() + tapeB.getDiskWrites() + tapeC.getDiskWrites();

            tapeA.close();
            tapeB.close();
            tapeC.close();

            System.out.println("Rozdzielono " + distributedRuns + " serii.");

            if (distributedRuns <= 1) {
                break;
            }

            System.out.println("Faza " + phases + ": Scalanie");

            tapeA.reset("rw");
            tapeB.reset("r");
            tapeC.reset("r");
            tapeA.truncate();

            merge(tapeA, tapeB, tapeC);

            totalReads += tapeA.getDiskReads() + tapeB.getDiskReads() + tapeC.getDiskReads();
            totalWrites += tapeA.getDiskWrites() + tapeB.getDiskWrites() + tapeC.getDiskWrites();

            tapeA.close();
            tapeB.close();
            tapeC.close();
        }

        long endTime = System.currentTimeMillis();

        /*System.out.println("\nSortowanie zakończone");
        System.out.println("Całkowity czas: " + (endTime - startTime) + " ms");
        System.out.println("Liczba faz: " + phases);
        System.out.println("Całkowita liczba odczytów z dysku: " + totalReads);
        System.out.println("Całkowita liczba zapisów na dysk: " + totalWrites);*/

        return new SortStats(phases, totalReads, totalWrites, (endTime - startTime));
    }

    private int distribute(Tape in, Tape out1, Tape out2) throws IOException {
        int runCount = 0;
        Record lastRecord = null;
        Record currentRecord;
        Tape currentOutput = out1;

        while ((currentRecord = in.readRecord()) != null) {
            if (lastRecord != null && currentRecord.compareTo(lastRecord) < 0) {
                currentOutput = (currentOutput == out1) ? out2 : out1;
                runCount++;
            }

            currentOutput.writeRecord(currentRecord);
            lastRecord = currentRecord;
        }

        return (lastRecord == null) ? 0 : runCount + 1;
    }

    private void merge(Tape out, Tape in1, Tape in2) throws IOException {
        Record r1 = in1.readRecord();
        Record r2 = in2.readRecord();
        Record last1 = null;
        Record last2 = null;

        while (r1 != null || r2 != null) {
            while (r1 != null && r2 != null) {
                boolean run1Ended = (last1 != null && r1.compareTo(last1) < 0);
                boolean run2Ended = (last2 != null && r2.compareTo(last2) < 0);

                if (run1Ended || run2Ended) {
                    break;
                }

                if (r1.compareTo(r2) <= 0) {
                    out.writeRecord(r1);
                    last1 = r1;
                    r1 = in1.readRecord();
                } else {
                    out.writeRecord(r2);
                    last2 = r2;
                    r2 = in2.readRecord();
                }
            }

            while (r1 != null && (last1 == null || r1.compareTo(last1) >= 0)) {
                out.writeRecord(r1);
                last1 = r1;
                r1 = in1.readRecord();
            }

            while (r2 != null && (last2 == null || r2.compareTo(last2) >= 0)) {
                out.writeRecord(r2);
                last2 = r2;
                r2 = in2.readRecord();
            }

            last1 = null;
            last2 = null;
        }
    }
}
