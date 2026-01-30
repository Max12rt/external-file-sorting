package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final String FILENAME = "data_43.bin";
    private static final int BLOCK_SIZE = 4096;
    private static final int RECORD_SIZE = Record.RECORD_SIZE;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            displayMenu();
            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Błędny format");
                continue;
            }

            try {
                switch (choice) {
                    case 0:
                        System.out.println("Wyjście");
                        System.exit(0);
                        break;
                    case 1:
                        writeFromKeyboard(scanner);
                        break;
                    case 2:
                        importFromFile(scanner);
                        break;
                    case 3:
                        generateRandomRecord(scanner);
                        break;
                    case 4:
                        readTape(scanner);
                        break;
                    case 5:
                        displayInfoTape();
                        break;
                    case 6:
                        sortTape(false);
                        break;
                    case 7:
                        sortTape(true);
                        break;
                    default:
                        System.err.println("Błędna opcja");
                }
            } catch (IOException e) {
                System.err.println("Błąd IO " + e.getMessage());
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\nMenu");
        System.out.println("Wybierz opcję:");
        System.out.println("1) Zapis z klawiatury");
        System.out.println("2) Import z pliku tekstowego");
        System.out.println("3) Generowanie rekordów losowych");
        System.out.println("4) Odczyt taśmy");
        System.out.println("5) Odczyt informacji o taśmie");
        System.out.println("6) Sortowanie taśmy");
        System.out.println("7) Sortowanie taśmy z odczytem");
        System.out.println("0) Wyjście");
        System.out.print("Wpisz: ");
    }

    private static void writeFromKeyboard(Scanner scanner) throws IOException {
        Tape tape = new Tape(FILENAME, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("rw");
        tape.truncate();

        while (true) {
            try {
                System.out.println("Wprowadź 5 liczb całkowitych (a, y, c, z, x) oddzielonych spacją:");
                String[] parts = scanner.nextLine().split("\\s+");
                int a = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int c = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                int x = Integer.parseInt(parts[4]);

                tape.writeRecord(new Record(a, y, c, z, x));

                System.out.print("Dodać kolejny rekord? (t/n): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("n")) {
                    break;
                }
            } catch (Exception e) {
                System.err.println("Błąd wprowadzania " + e.getMessage());
            }
        }
        tape.close();
        System.out.println("Dane zapisane.");
    }
    private static void importFromFile(Scanner scanner) throws IOException {
        System.out.print("Podaj nazwę pliku źródłowego (np. 'data.txt'): ");
        String sourceFileName = scanner.nextLine();

        File file = new File(sourceFileName);
        if (!file.exists()) {
            System.err.println("Błąd: Plik " + sourceFileName + " nie znaleziony.");
            return;
        }

        Tape tape = new Tape(FILENAME, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("rw");
        tape.truncate();

        Scanner fileScanner = null;
        int recordsWritten = 0;
        try {
            fileScanner = new Scanner(file);
            while (fileScanner.hasNextInt()) {
                int a = fileScanner.nextInt();
                int y = fileScanner.nextInt();
                int c = fileScanner.nextInt();
                int z = fileScanner.nextInt();
                int x = fileScanner.nextInt();
                tape.writeRecord(new Record(a, y, c, z, x));
                recordsWritten++;
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas odczytu pliku");
        } finally {
            if (fileScanner != null) {
                fileScanner.close();
            }
            tape.close();
        }
        System.out.println(" Zapisano " + recordsWritten + " rekordów.");
    }

    private static void generateRandomRecord(Scanner scanner) throws IOException {
        System.out.print("Ile rekordów wygenerować? ");
        int numRecords = Integer.parseInt(scanner.nextLine());
        if (numRecords <= 0) {
            System.err.println("Liczba musi być > 0.");
            return;
        }
        generateFile(FILENAME, numRecords);
    }

    private static void readTape(Scanner scanner) throws IOException {
        System.out.print("Pokazać tylko wartości g() (t) czy pełne rekordy (n)? (t/n): ");
        boolean gOnly = scanner.nextLine().trim().equalsIgnoreCase("t");
        printFile(FILENAME, gOnly);
    }

    private static void displayInfoTape() throws IOException {
        System.out.println("Analiza taśmy " + FILENAME);
        Tape tape = new Tape(FILENAME, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("r");

        int recordCount = 0;
        int runCount = 0;
        Record last = null;
        Record current;

        while ((current = tape.readRecord()) != null) {
            recordCount++;
            if (last != null && current.compareTo(last) < 0) {
                runCount++;
            }
            last = current;
        }
        tape.close();

        if (recordCount > 0) {
            runCount++;
        }

        System.out.println("Informacje o taśmie");
        System.out.println("Całkowita liczba rekordów: " + recordCount);
        System.out.println("Całkowita liczba serii (run-ów): " + runCount);
        System.out.println("koniec taśmie");
    }

    private static void sortTape(boolean showAfter) throws IOException {
        NaturalMergeSorter sorter = new NaturalMergeSorter(RECORD_SIZE, BLOCK_SIZE);
        SortStats stats = sorter.sort(FILENAME);

        System.out.println("\nSortowanie zakończone");
        System.out.println("Całkowity czas: " + stats.durationMs() + " ms");
        System.out.println("Liczba faz: " + stats.phases());
        System.out.println("Całkowita liczba odczytów z dysku: " + stats.diskReads());
        System.out.println("Całkowita liczba zapisów na dysk: " + stats.diskWrites());

        if (showAfter) {
            System.out.println("\n Wynik sortowania (" + FILENAME + ")");
            printFile(FILENAME, true);
        }
    }

    public static void generateFile(String filename, int numRecords) throws IOException {
        System.out.println("Generowanie pliku: " + filename + " z " + numRecords + " rekordami");
        Tape tape = new Tape(filename, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("rw");
        tape.truncate();

        Random rand = new Random();

        for (int i = 0; i < numRecords; i++) {
            int a = rand.nextInt(10) + 1;
            int y = rand.nextInt(4) + 1;
            int c = rand.nextInt(5) + 1;
            int z = rand.nextInt(5) + 1;
            int x = rand.nextInt(20) + 1;

            tape.writeRecord(new Record(a, y, c, z, x));
        }

        tape.close();
        System.out.println("Generowanie zakończone.");
    }
    public static void printFile(String filename, boolean printGValueOnly) throws IOException {
        System.out.println("\nZawartość pliku: " + filename );
        Tape tape = new Tape(filename, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("r");

        Record record;
        int count = 0;

        while ((record = tape.readRecord()) != null) {
            if (printGValueOnly) {
                System.out.printf("Rekord %d: g() = %.2f\n", count, record.getGValue());
            } else {
                System.out.println("Rekord " + count + ": " + record);
            }
            count++;
        }

        tape.close();
        System.out.println("Koniec pliku " + filename + " łącznie " + count + " rekordów");
    }
}
