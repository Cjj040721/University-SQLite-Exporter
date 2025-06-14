# University LSH Index Exporter

A Java-based tool to export **SQLite database schema and data** into separate `.sql` files, preserving table dependencies via **foreign key-aware topological sorting**. Designed for use with university schema and LSH-indexed databases.

---

## 📁 Project Structure

```

university-lsh-index/
├── src/                 # Java source code (includes DatabaseExporter.java)
├── lib/                 # External libraries (.jar files if any)
├── data/                # Input SQLite databases
├── tests/               # Sample .db files for testing
├── scripts/             # Shell scripts to restore test DBs
├── docs/                # Reports, schema diagrams, etc.
├── .gitignore           # Git ignore rules
└── README.md            # This file

````

---

## 🚀 How to Use

### 1. Compile the Project

```bash
mkdir out
javac -d out -cp "lib/*" src/DatabaseExporter.java
````

> If using an IDE like IntelliJ or Eclipse, just open the project and build.

### 2. Prepare Databases

Ensure you have SQLite `.db` files placed in the `data/` folder:

* `data/university.db`
* `data/LSH.db`
* `data/Chinook.db`
* `data/Northwind.db`
* `data/test1.db`
* `data/test2.db`

Or run:

```bash
./scripts/restore-university.sh
./scripts/restore-lsh.sh
```

### 3. Run the Exporter

```bash
java -cp "out:lib/*" DatabaseExporter
```

This will generate the following output files in the `output/` folder:

* `output/university_schema.sql`
* `output/university_data.sql`
* (and so on for each database)

---

## ✨ Features

* ✅ Export `CREATE TABLE`, `PRIMARY KEY`, `FOREIGN KEY`, and `INDEX` statements
* ✅ Export `INSERT` statements with proper escaping
* ✅ Handles **foreign key dependencies** with **topological sorting**
* ✅ Outputs schema and data separately

---

## 🧪 Sample Databases

| File Name            | Description                  |
| -------------------- | ---------------------------- |
| `test_university.db` | Example university schema    |
| `test_LSH.db`        | LSH-indexed data (test only) |
| `Northwind.db`       | Classic e-commerce dataset   |
| `Chinook.db`         | Music store sample DB        |

---

## 📄 License

This project is provided for **educational purposes only**.
Do not redistribute without permission.

---

## 👤 Author

**Cao Junjia**
May 28, 2025

---
