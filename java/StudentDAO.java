import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for performing CRUD operations on `student_details` table.
 */
public class StudentDAO {

    /**
     * Add a new student record into database.
     */
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO student_details (student_name, register_number, phone_number, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getStudentName());
            stmt.setString(2, student.getRegisterNumber());
            stmt.setString(3, student.getPhoneNumber());
            stmt.setString(4, student.getEmail());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch all students from database into an ArrayList.
     */
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM student_details ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("register_number"),
                        rs.getString("phone_number"),
                        rs.getString("email")
                );
                students.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    /**
     * Find student by Register Number.
     */
    public Student getStudentByRegisterNumber(String regNo) {
        String sql = "SELECT * FROM student_details WHERE register_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, regNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("register_number"),
                            rs.getString("phone_number"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by register number: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check if a register number already exists in database.
     */
    public boolean isRegisterNumberExists(String regNo, int excludeId) {
        String sql = "SELECT COUNT(*) FROM student_details WHERE register_number = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, regNo);
            stmt.setInt(2, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking register number uniqueness: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update an existing student's details.
     */
    public boolean updateStudent(Student student) {
        String sql = "UPDATE student_details SET student_name = ?, register_number = ?, phone_number = ?, email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getStudentName());
            stmt.setString(2, student.getRegisterNumber());
            stmt.setString(3, student.getPhoneNumber());
            stmt.setString(4, student.getEmail());
            stmt.setInt(5, student.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete student by ID.
     */
    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM student_details WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search students by name or register number.
     */
    public List<Student> searchStudents(String keyword) {
        List<Student> result = new ArrayList<>();
        String sql = "SELECT * FROM student_details WHERE student_name LIKE ? OR register_number LIKE ? OR email LIKE ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            stmt.setString(3, term);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("register_number"),
                            rs.getString("phone_number"),
                            rs.getString("email")
                    );
                    result.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching students: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get total count of registered students.
     */
    public int getTotalStudentCount() {
        String sql = "SELECT COUNT(*) FROM student_details";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total student count: " + e.getMessage());
        }
        return 0;
    }
}
