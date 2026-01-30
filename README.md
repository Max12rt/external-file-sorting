# External File Sorting in Java

## 📌 Overview
This project implements **external file sorting algorithms** in Java for datasets that do not fit entirely in memory.
Disk tapes are simulated using files, and all disk operations are performed through a dedicated **block-based I/O abstraction layer**.

The project was developed and compiled using **IntelliJ IDEA**.

---

## ⚙️ Implemented Algorithms
- Natural Merge Sort
- Merge Sort with Large Buffers
- (Optional) Polyphase Merge Sort

---

## 🧱 Architecture

### 1️⃣ Block I/O Layer
A separate layer responsible for simulating disk operations:
- Reading a single record
- Writing a single record
- Counting disk page reads and writes

This layer fully abstracts file access from the sorting logic.

### 2️⃣ Sorting Layer
Implements external sorting algorithms using only the I/O layer interface.
The sorting algorithms do not access files directly.

---

## ✨ Features
- Sorting records stored in disk files (tape simulation)
- Random record generation
- Manual record input from the keyboard
- Loading test data from a file
- Displaying file contents:
  - Before sorting
  - After sorting
  - After each sorting phase
- Detailed execution statistics:
  - Number of sorting phases
  - Number of disk reads
  - Number of disk writes

---

## 🧪 Experimental Analysis
An experimental evaluation was conducted to analyze performance.

### Experiment Scope:
- At least **5 significantly different dataset sizes**
- Measurement of:
  - Number of sorting phases
  - Disk page read/write operations
- Comparison of **theoretical vs practical results**
- Visualization of results using charts (linear and logarithmic scales)

Observed differences between theoretical and practical results are discussed in the report.

---

## 🛠 Technologies
- **Java**
- File I/O (tape simulation)
- External sorting algorithms
- IntelliJ IDEA

---

## ▶️ How to Run

### Using IntelliJ IDEA
1. Open the project in **IntelliJ IDEA**
2. Build the project (`Build → Build Project`)
3. Run the `Main` class

### Using Command Line
```bash
javac src/**/*.java
java Main
