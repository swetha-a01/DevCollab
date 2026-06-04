DevCollab 🚀 | Peer-to-Peer Campus Skill & Token Exchange Hub
DevCollab is a full-stack desktop application designed for university campuses. It establishes a decentralized peer-to-peer (P2P) network where students can broadcast technical help requests, collaborate via live workspace chat channels, and exchange knowledge using an incentivized token economy backed by a secure Escrow Transaction System.

🛠️ Key Architectural Features
Secure Escrow Token Ledger: Prevents token fraud. When a user creates a request, their wallet balance is checked. When a peer claims it, no tokens move. Only when the creator verifies the solution by clicking "Mark as Completed" does an atomic transaction fire to deduct tokens from the poster and credit the helper simultaneously.

Asynchronous Live Chat Rooms: Every task creates a private room. Powered by a background Timeline polling engine running on a distinct lifecycle thread, users can send and receive coordination messages in real-time without blocking or freezing the main UI thread.

Real-Time Analytics Dashboard: Aggregates database parameters using SQL aggregation functions (COUNT, SUM, COALESCE) to compute user statistics dynamically (Posted, Solved, Earned, Spent).

Automated Testing Shield: Integrated UI automation testing suite using TestFX and JUnit to simulate user interactions and guarantee backend query reliability.

📊 Complete System Blueprint (Database Schemas & Logic Codes)
Part A: Relational Database Schemas (MySQL)
SQL
-- 1. Students Table Profile Info & Balances
CREATE TABLE Students (
    student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    tokens INT DEFAULT 5
);

-- 2. Tasks Table Life Cycle Status Management
CREATE TABLE Tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    helper_id VARCHAR(20),
    title VARCHAR(255) NOT NULL,
    tokens_offered INT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (helper_id) REFERENCES Students(student_id)
);

-- 3. Messages Table Workspace Chat Logs
CREATE TABLE Messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    sender_id VARCHAR(20) NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES Tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES Students(student_id)
);
Part B: Transaction ACID Boundaries (Java Backend)
Java
// Logic snippet showing ACID transaction boundary inside TaskDAO.java
conn.setAutoCommit(false); 

// 1. Deduct tokens from the task creator
// 2. Add tokens to the peer specialist helper
// 3. Update task status state to 'COMPLETED'

conn.commit(); // Executed only if all operations succeed flawlessly!
🚀 How to Run the System
Database Setup:

Ensure your local MySQL server instance is running.

Execute the table creation schemas provided above inside your target database.

Compile and Launch:

Open the project inside Eclipse.

Ensure standard JavaFX and MySQL Connector libraries are mapped into your build path.

Run Launcher.java to start the application.
