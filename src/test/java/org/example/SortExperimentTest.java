package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SortExperimentTest {

    private static final int BLOCK_SIZE = 4096;
    private static final int RECORD_SIZE = Record.RECORD_SIZE;
    private static PrintWriter resultsWriter;

    // Цей метод виконається ОДИН РАЗ перед усіма тестами
    @BeforeAll
    public static void setup() throws IOException {
        resultsWriter = new PrintWriter(new FileWriter("test_results.csv"));
        resultsWriter.println("N,r,F_prakt,O_prakt,F_teor,K,O_teor,IsSorted,TimeMs");
    }

    // Цей метод виконається ОДИН РАЗ після всіх тестів
    @AfterAll
    public static void teardown() {
        resultsWriter.close();
        System.out.println("Результати тестів збережено у test_results.csv");
    }

    // Це серце нашого тесту. Він запуститься 6 разів,
    // по одному для кожного значення N у @CsvSource.
    @ParameterizedTest
    @CsvSource({"1000", "5000", "10000", "20000", "50000", "100000"})
    @Order(1) // Виконуємо всі ці тести по порядку
    public void runSortExperiment(int N) throws IOException {
        String filename = "test_file_" + N + ".bin";
        System.out.println("--- Running test for N = " + N + " ---");

        // 1. Генерація файлу
        generateFile(filename, N);

        // 2. Підрахунок 'r' (початкові серії)
        int r = countInitialRuns(filename);

        // 3. Сортування та отримання статистики
        NaturalMergeSorter sorter = new NaturalMergeSorter(RECORD_SIZE, BLOCK_SIZE);
        SortStats stats = sorter.sort(filename); // Виклик оновленого методу

        // 4. Перевірка, чи файл відсортовано (найважливіший тест!)
        boolean sorted = isFileSorted(filename);

        // 5. Розрахунок теоретичних значень
        int b = BLOCK_SIZE / RECORD_SIZE; // Записів у блоці
        int K = (int) Math.ceil((double) N / b);
        int F_teor = (int) Math.ceil(Math.log(r) / Math.log(2));
        long O_teor = (long) F_teor * 2 * K;

        long O_prakt = stats.diskReads() + stats.diskWrites();

        // 6. Запис результатів у CSV
        String resultLine = String.format("%d,%d,%d,%d,%d,%d,%d,%b,%d",
                N, r, stats.phases(), O_prakt, F_teor, K, O_teor, sorted, stats.durationMs());
        resultsWriter.println(resultLine);

        // 7. Фінальна перевірка
        // Якщо цей тест впаде, щось пішло не так
        assertTrue(sorted, "Файл " + filename + " НЕ відсортовано!");
    }

    // --- Допоміжні методи (скопійовані з Main.java) ---

    private void generateFile(String filename, int numRecords) throws IOException {
        Tape tape = new Tape(filename, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("rw");
        tape.truncate();
        Random rand = new Random();
        for (int i = 0; i < numRecords; i++) {
            tape.writeRecord(new Record(
                    rand.nextInt(10) + 1, rand.nextInt(4) + 1,
                    rand.nextInt(5) + 1, rand.nextInt(5) + 1,
                    rand.nextInt(20) + 1));
        }
        tape.close();
    }

    private int countInitialRuns(String filename) throws IOException {
        Tape tape = new Tape(filename, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("r");
        int runCount = 0;
        Record last = null;
        Record current;
        while ((current = tape.readRecord()) != null) {
            if (last != null && current.compareTo(last) < 0) {
                runCount++;
            }
            last = current;
        }
        tape.close();
        return (last == null) ? 0 : runCount + 1;
    }

    private boolean isFileSorted(String filename) throws IOException {
        Tape tape = new Tape(filename, RECORD_SIZE, BLOCK_SIZE);
        tape.reset("r");
        Record last = null;
        Record current;
        while ((current = tape.readRecord()) != null) {
            if (last != null && current.compareTo(last) < 0) {
                tape.close();
                System.err.println("Помилка сортування! " + current.getGValue() + " < " + last.getGValue());
                return false; // Знайдено помилку
            }
            last = current;
        }
        tape.close();
        return true; // Помилок не знайдено
    }
}
