# Student Event-Management System 🎓🏆
URL:https://student-event-management-database-system.onrender.com

A full-stack Student Event-Management System built using **HTML5, CSS3, Vanilla JavaScript, Java (Collections & JDBC)**, and **MySQL Database**.

---

## 📁 Project Structure

```
StudentEventManagementSystem/
│
├── index.html                  # Main Web Dashboard UI
├── css/
│     style.css                 # Responsive Academic Blue & White Theme
├── js/
│     script.js                 # Front-end state engine & form validations
├── java/
│     DBConnection.java         # JDBC MySQL connection manager & auto table initializer
│     Student.java              # Student entity model
│     Event.java                # Event participation model
│     StudentDAO.java           # Student Data Access Object (CRUD)
│     EventDAO.java             # Event Data Access Object (CRUD)
│     Main.java                 # Interactive Java CLI & JDK Web Server
├── database/
│     student_event.sql         # Database schema & sample seed data
├── run.bat                     # ⚡ One-click automated build & run script
└── README.md                   # System documentation & setup guide
```

---

## 🚀 How to Run the Application

### Option A: One-Click Execution (Easiest)
Simply double-click **`run.bat`** or run `run.bat` in your Command Prompt:
```cmd
D:\Project Files\StudentEventManagementSystem> run.bat
```

---

### Option B: Manual Command Prompt Execution (cmd.exe)
In Command Prompt, run these two commands:

```cmd
D:\Project Files\StudentEventManagementSystem> javac -cp .;mysql-connector-j-9.7.0.jar java/*.java
D:\Project Files\StudentEventManagementSystem> java -cp .;java;mysql-connector-j-9.7.0.jar Main
```

---

### Option C: Manual PowerShell Execution
In PowerShell, run:

```powershell
D:\Project Files\StudentEventManagementSystem> javac -cp ".;mysql-connector-j-9.7.0.jar" java/*.java
D:\Project Files\StudentEventManagementSystem> java -cp ".;java;mysql-connector-j-9.7.0.jar" Main
```

---

## 🌐 Web Application Access

Once `Main.java` is running:
- Open **`http://localhost:8080`** in your browser.
- Or double-click [`index.html`](file:///d:/Project%20Files/StudentEventManagementSystem/index.html) to open the interactive dashboard directly.
