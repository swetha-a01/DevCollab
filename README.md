# DevCollab 🚀 | Peer-to-Peer Campus Skill & Token Exchange Hub

[![Java](https://img.shields.io/badge/Java-93.7%25-ED8936?logo=java)](https://www.java.com)
[![CSS](https://img.shields.io/badge/CSS-6.3%25-1572B6?logo=css3)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Overview

DevCollab is a full-stack desktop application designed for university campuses that establishes a decentralized peer-to-peer (P2P) network where students can broadcast technical help requests, collaborate on projects, and exchange skills using a secure token-based economy. The platform democratizes knowledge sharing by allowing any student to become a peer specialist and earn tokens by solving problems within their community.

**Key Problem Solved:** Traditional tutoring systems are centralized and often expensive. DevCollab enables organic, student-driven skill exchange with built-in trust mechanisms and fraud prevention.

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
  - [Database Schemas](#database-schemas)
  - [Backend Logic](#backend-logic)
- [Project Structure](#project-structure)
- [Setup & Installation](#setup--installation)
- [How to Use](#how-to-use)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Key Features

### 🔐 Secure Escrow Token Ledger
Prevents token fraud with a three-step verification process:
- When a user creates a request, their wallet balance is checked and reserved
- When a peer claims the task, no tokens move immediately
- Only when the creator verifies and approves the solution do tokens transfer atomically to the helper
- All transactions are logged in the ledger with audit trails

### 💬 Asynchronous Live Chat Rooms
- Every task automatically creates a private, encrypted chat room for coordination
- Powered by a background Timeline polling engine running on a distinct lifecycle thread
- Real-time message delivery with persistent chat history
- Users can send and receive coordination messages without blocking the UI

### 📊 Real-Time Analytics Dashboard
- Aggregates database metrics using SQL aggregation functions (COUNT, SUM, COALESCE)
- Dynamically computes user statistics:
  - **Posted:** Number of tasks created
  - **Solved:** Number of tasks completed
  - **Earned:** Total tokens received as a helper
  - **Spent:** Total tokens offered on tasks
- Refreshes in real-time as transactions occur

### 🧪 Automated Testing Shield
- Integrated UI automation testing suite using TestFX and JUnit
- Simulates realistic user interactions to catch regressions
- Guarantees backend query reliability
- Comprehensive test coverage for critical workflows

## Tech Stack

- **Frontend:** JavaFX (UI Framework)
- **Backend:** Java (Core Application Logic)
- **Database:** MySQL 5.7+ (Relational Data Storage)
- **Testing:** JUnit 4+ & TestFX (UI & Unit Tests)
- **Build Tool:** Eclipse IDE
- **Dependencies:** MySQL Connector/J, JavaFX SDK

## System Architecture

### Database Schemas

#### Entity Relationship Diagram
```
┌──────────────┐         ┌─────────────────┐         ┌──────────────────┐
│  Students    │◄────┬───┤  Tasks          │◄────┬───┤  Messages        │
├──────────────┤     │   ├─────────────────┤     │   ├──────────────────┤
│ student_id   │     │   │ task_id         │     │   │ message_id       │
│ name         │     │   │ student_id (FK) │─┐   │   │ task_id (FK)     │
│ password     │     │   │ helper_id (FK)  │─┼───┤   │ sender_id (FK)   │
│ tokens       │     │   │ title           │ │   │   │ message_text     │
└──────────────┘     │   │ tokens_offered  │ │   │   │ sent_at          │
                     │   │ status          │ │   │   └──────────────────┘
                     │   └─────────────────┘ │   │
                     └───────────────────────┴───┘
```

#### SQL Table Definitions

**1. Students Table** - User profiles and token balances
```sql
CREATE TABLE Students (
    student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    tokens INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**2. Tasks Table** - Help requests with lifecycle status
```sql
CREATE TABLE Tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    helper_id VARCHAR(20),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    tokens_offered INT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (helper_id) REFERENCES Students(student_id),
    INDEX (status),
    INDEX (student_id)
);
```

**Task Status Lifecycle:** `OPEN` → `CLAIMED` → `IN_PROGRESS` → `COMPLETED` or `CANCELLED`

**3. Messages Table** - Private chat logs per task
```sql
CREATE TABLE Messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    sender_id VARCHAR(20) NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES Tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES Students(student_id),
    INDEX (task_id),
    INDEX (sent_at)
);
```

### Backend Logic

#### ACID Transaction Boundaries (Java - TaskDAO.java)

Critical token transfer operations are wrapped in ACID transactions to ensure data consistency:

```java
// Logic snippet showing ACID transaction boundary inside TaskDAO.java

conn.setAutoCommit(false);  // Disable auto-commit for explicit transaction control

try {
    // 1. Deduct tokens from the task creator
    String deductSQL = "UPDATE Students SET tokens = tokens - ? WHERE student_id = ?";
    pstmt = conn.prepareStatement(deductSQL);
    pstmt.setInt(1, tokensOffered);
    pstmt.setString(2, creatorId);
    pstmt.executeUpdate();
    
    // 2. Add tokens to the peer specialist helper
    String addSQL = "UPDATE Students SET tokens = tokens + ? WHERE student_id = ?";
    pstmt = conn.prepareStatement(addSQL);
    pstmt.setInt(1, tokensOffered);
    pstmt.setString(2, helperId);
    pstmt.executeUpdate();
    
    // 3. Update task status state to 'COMPLETED'
    String updateTaskSQL = "UPDATE Tasks SET status = 'COMPLETED', helper_id = ?, completed_at = NOW() WHERE task_id = ?";
    pstmt = conn.prepareStatement(updateTaskSQL);
    pstmt.setString(1, helperId);
    pstmt.setInt(2, taskId);
    pstmt.executeUpdate();
    
    conn.commit();  // Executed only if all operations succeed flawlessly!
    
} catch (SQLException e) {
    conn.rollback();  // Undo all changes if any operation fails
    throw e;
}
```

**Why This Matters:**
- All-or-nothing execution ensures tokens are never duplicated or lost
- Automatic rollback prevents partial updates if any step fails
- Prevents race conditions in concurrent environments

## Project Structure

```
DevCollab/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── dao/
│   │   │   │   ├── StudentDAO.java          # Database operations for students
│   │   │   │   ├── TaskDAO.java             # Database operations for tasks
│   │   │   │   └── MessageDAO.java          # Database operations for messages
│   │   │   ├── models/
│   │   │   │   ├── Student.java             # Student entity model
│   │   │   │   ├── Task.java                # Task entity model
│   │   │   │   └── Message.java             # Message entity model
│   │   │   ├── ui/
│   │   │   │   ├── LoginController.java     # Authentication UI
│   │   │   │   ├── DashboardController.java # Analytics UI
│   │   │   │   ├── TaskBoardController.java # Task listing & creation
│   │   │   │   └── ChatController.java      # Live chat UI
│   │   │   ├── service/
│   │   │   │   ├── TokenService.java        # Token ledger logic
│   │   │   │   ├── ChatPoller.java          # Background message polling
│   │   │   │   └── AnalyticsService.java    # Dashboard calculations
│   │   │   └── Launcher.java                # Application entry point
│   │   └── resources/
│   │       └── styles.css                   # JavaFX stylesheets
│   └── test/
│       ├── java/
│       │   ├── DAOTests.java                # Unit tests for database operations
│       │   ├── TransactionTests.java        # ACID transaction validation
│       │   └── UITests.java                 # JavaFX TestFX integration tests
│       └── resources/
│           └── test-data.sql                # Test database fixtures
├── lib/
│   ├── mysql-connector-java-8.x.jar
│   └── javafx-sdk-17/
├── config/
│   └── db-connection.properties             # Database configuration
├── README.md
└── LICENSE
```

## Setup & Installation

### Prerequisites

- **JDK 11 or higher** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **MySQL 5.7 or higher** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Eclipse IDE** (2021-09 or later) - [Download](https://www.eclipse.org/downloads/)
- **JavaFX SDK 17** - [Download](https://gluonhq.com/products/javafx/)
- **Git** - [Download](https://git-scm.com/)

### Step 1: Database Setup

1. **Start your MySQL server:**
   ```bash
   # macOS (if installed via Homebrew)
   mysql.server start
   
   # Linux
   sudo systemctl start mysql
   
   # Windows - MySQL runs as a service automatically
   ```

2. **Log into MySQL:**
   ```bash
   mysql -u root -p
   ```

3. **Create the database and tables:**
   ```sql
   CREATE DATABASE devcollab;
   USE devcollab;
   
   -- Paste all SQL schemas from the "Database Schemas" section above
   ```

4. **(Optional) Seed sample data:**
   ```sql
   INSERT INTO Students (student_id, name, password, tokens) VALUES
   ('CS001', 'Alice Johnson', 'pass123', 10),
   ('CS002', 'Bob Smith', 'pass456', 5),
   ('CS003', 'Carol White', 'pass789', 15);
   ```

### Step 2: Project Setup in Eclipse

1. **Clone the repository:**
   ```bash
   git clone https://github.com/swetha-a01/DevCollab.git
   cd DevCollab
   ```

2. **Open Eclipse and import the project:**
   - File → Import → Existing Projects into Workspace
   - Select the `DevCollab` folder
   - Click Finish

3. **Add MySQL Connector JAR:**
   - Right-click project → Build Path → Configure Build Path
   - Add External Archives → Select `mysql-connector-java-8.x.jar`
   - Apply and Close

4. **Add JavaFX SDK to build path:**
   - Right-click project → Build Path → Configure Build Path
   - Add External JARs → Navigate to `javafx-sdk-17/lib`
   - Select all `.jar` files
   - Apply and Close

5. **Configure VM arguments for JavaFX:**
   - Run → Run Configurations → Arguments tab
   - Add to VM arguments:
     ```
     --module-path /path/to/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml
     ```

6. **(Optional) Update database connection:**
   - Edit `config/db-connection.properties`:
     ```properties
     db.host=localhost
     db.port=3306
     db.name=devcollab
     db.user=root
     db.password=your_password
     ```

### Step 3: Compile and Launch

1. **Build the project:**
   - Right-click project → Build Project

2. **Run the application:**
   - Right-click `Launcher.java` → Run As → Java Application
   - The login window should appear ✓

## How to Use

### For Task Creators (Students Seeking Help)

1. **Sign Up / Login:**
   - Enter your student ID and create a password
   - You start with 5 tokens

2. **Create a Task:**
   - Click "New Task" button
   - Fill in: Title, Description, Tokens Offered
   - Submit to broadcast to the network

3. **Review Proposals:**
   - See helpers who claimed your task
   - Open the chat room to discuss details
   - Verify the solution provided

4. **Verify & Complete:**
   - Once satisfied, click "Verify Solution"
   - Tokens automatically transfer to the helper
   - Task marked as COMPLETED

### For Helpers (Peer Specialists)

1. **Browse Available Tasks:**
   - Open the task board to see all open requests
   - Filter by topic, token amount, or difficulty

2. **Claim a Task:**
   - Click "Claim Task" on any open request
   - Task status changes to CLAIMED
   - Private chat opens with the task creator

3. **Collaborate:**
   - Exchange messages in the chat room
   - Share solutions, code snippets, explanations
   - Discuss progress in real-time

4. **Submit Solution:**
   - Mark task as "Solution Submitted"
   - Await creator verification
   - Receive tokens upon approval

### Dashboard Metrics

Your dashboard displays:
- **Posted:** Tasks you've created
- **Solved:** Tasks you've helped complete
- **Earned:** Total tokens received
- **Spent:** Total tokens offered
- **Token Balance:** Current wallet

## Testing

### Running Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TransactionTests

# Run with coverage
mvn test jacoco:report
```

### Key Test Scenarios

1. **Authentication Tests**
   - Valid login succeeds
   - Invalid password fails
   - New student signup creates profile

2. **Token Transaction Tests**
   - Token deduction works correctly
   - Token addition works correctly
   - Rollback on failure prevents partial updates

3. **Task Lifecycle Tests**
   - Task status transitions correctly
   - Chat room creates with each task
   - Messages persist properly

4. **Concurrency Tests**
   - Multiple helpers can't claim the same task
   - Race conditions in token transfers are prevented

5. **UI Tests (TestFX)**
   - Login screen appears on launch
   - Dashboard loads user statistics
   - Task creation form validates inputs

## Future Enhancements

- [ ] User reputation/rating system
- [ ] Advanced search and filtering
- [ ] Task categories and skill matching
- [ ] Mobile companion app
- [ ] Real-time notifications
- [ ] Task deadline/timer functionality
- [ ] Peer reviews and testimonials
- [ ] Blockchain-based token system (optional)

## Troubleshooting

### "Cannot connect to database"
- Verify MySQL is running: `mysql -u root -p`
- Check connection properties in `db-connection.properties`
- Ensure database `devcollab` exists

### "JavaFX not found"
- Verify JavaFX SDK is added to build path
- Check VM arguments include `--module-path` and `--add-modules`

### "Port 3306 already in use"
- Change MySQL port in connection properties
- Or kill the existing MySQL process

### Tests failing
- Ensure test database is populated with `test-data.sql`
- Check that MySQL user has sufficient permissions
- Run tests with clean database: `mvn clean test`

## Contributing

We welcome contributions! To contribute:

1. **Fork the repository**
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes:**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to your branch:**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request** with a clear description

### Code Style Guidelines

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Write unit tests for new features
- Update README for significant changes
- Use meaningful commit messages

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contact & Support

- **Repository:** [swetha-a01/DevCollab](https://github.com/swetha-a01/DevCollab)
- **Issues:** [Open an issue](https://github.com/swetha-a01/DevCollab/issues)

**Happy Collaborating! 🎓✨**
