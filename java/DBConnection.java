import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection provides JDBC database connection management 
 * and automatic schema/table initialization for MySQL database.
 * Supports environment variables for cloud deployment (Render, Aiven, Railway, etc.)
 */
public class DBConnection {

    private static final String HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String PORT_NUM = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "student_event_db";
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "root";

    private static final String SERVER_URL = System.getenv("MYSQL_SERVER_URL") != null ? System.getenv("MYSQL_SERVER_URL") :
            "jdbc:mysql://" + HOST + ":" + PORT_NUM + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String URL = System.getenv("MYSQL_URL") != null ? System.getenv("MYSQL_URL") :
            "jdbc:mysql://" + HOST + ":" + PORT_NUM + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    static {
        try {
            Class<?> driverClass = null;
            try {
                driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                driverClass = Class.forName("com.mysql.jdbc.Driver");
            }
            if (driverClass != null) {
                java.sql.Driver driver = (java.sql.Driver) driverClass.getDeclaredConstructor().newInstance();
                DriverManager.registerDriver(driver);
            }
        } catch (Exception e) {
            System.err.println("Note: JDBC Driver registration message: " + e.getMessage());
        }
    }

    /**
     * Obtains a connection to the MySQL database.
     * @return Connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Overloaded method to connect with custom credentials if required.
     */
    public static Connection getConnection(String dbUrl, String dbUser, String dbPass) throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    /**
     * Automatically creates the database and tables if they do not already exist.
     * Also inserts default seed data if the tables are freshly created.
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. Create Database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            // 2. Create student_details table if not exists
            String createStudentTable = "CREATE TABLE IF NOT EXISTS student_details (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_name VARCHAR(100) NOT NULL, " +
                    "register_number VARCHAR(20) NOT NULL UNIQUE, " +
                    "phone_number VARCHAR(10) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createStudentTable);

            // 3. Create event_details table if not exists
            String createEventTable = "CREATE TABLE IF NOT EXISTS event_details (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_name VARCHAR(100) NOT NULL, " +
                    "student_register_number VARCHAR(20) NOT NULL, " +
                    "event_name VARCHAR(150) NOT NULL, " +
                    "event_date DATE NOT NULL, " +
                    "event_place VARCHAR(100) NOT NULL, " +
                    "prize_won ENUM('1st Prize', '2nd Prize', '3rd Prize', 'Participation') NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_student_reg FOREIGN KEY (student_register_number) " +
                    "REFERENCES student_details(register_number) " +
                    "ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createEventTable);

            System.out.println(">> Database & Tables Verified / Created Successfully!");

            // 4. Seed sample data if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM student_details")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println(">> Seeding initial sample data...");
                    stmt.executeUpdate("INSERT INTO student_details (student_name, register_number, phone_number, email) VALUES " +
                            "('Alex Morgan', '21CS001', '9876543210', 'alex.morgan@college.edu'), " +
                            "('Sophia Chen', '21CS002', '9123456789', 'sophia.chen@college.edu'), " +
                            "('Ethan Hunt', '21EC015', '9988776655', 'ethan.hunt@college.edu'), " +
                            "('Maya Lin', '22IT042', '9543210987', 'maya.lin@college.edu'), " +
                            "('David Miller', '22ME009', '9012345678', 'david.miller@college.edu');");

                    stmt.executeUpdate("INSERT INTO event_details (student_name, student_register_number, event_name, event_date, event_place, prize_won) VALUES " +
                            "('Alex Morgan', '21CS001', 'National Hackathon 2026', '2026-03-15', 'Auditorium Hall A', '1st Prize'), " +
                            "('Sophia Chen', '21CS002', 'Web Crafting Symposium', '2026-04-10', 'Tech Park Lab 3', '2nd Prize'), " +
                            "('Ethan Hunt', '21EC015', 'RoboWars Championship', '2026-05-02', 'Indoor Sports Complex', '1st Prize'), " +
                            "('Maya Lin', '22IT042', 'CodeSprint 2.0', '2026-05-20', 'Main Computer Lab', '3rd Prize'), " +
                            "('David Miller', '22ME009', 'CAD Design Challenge', '2026-06-11', 'Mechanical Workshop B', 'Participation');");

                    System.out.println(">> Seed data inserted successfully!");
                }
            }

        } catch (SQLException e) {
            System.err.println("Note: Automatic database/table creation check skipped (" + e.getMessage() + ")");
        }
    }
}
