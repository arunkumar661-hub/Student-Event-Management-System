import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for performing CRUD operations on `event_details` table.
 */
public class EventDAO {

    /**
     * Add a new event record.
     */
    public boolean addEvent(Event event) {
        String sql = "INSERT INTO event_details (student_name, student_register_number, event_name, event_date, event_place, prize_won) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getStudentName());
            stmt.setString(2, event.getStudentRegisterNumber());
            stmt.setString(3, event.getEventName());
            stmt.setString(4, event.getEventDate());
            stmt.setString(5, event.getEventPlace());
            stmt.setString(6, event.getPrizeWon());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Error adding event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve all event records from database into an ArrayList.
     */
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event_details ORDER BY event_date DESC, id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event e = new Event(
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("student_register_number"),
                        rs.getString("event_name"),
                        rs.getString("event_date"),
                        rs.getString("event_place"),
                        rs.getString("prize_won")
                );
                events.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving events: " + e.getMessage());
            e.printStackTrace();
        }
        return events;
    }

    /**
     * Get event details by ID.
     */
    public Event getEventById(int id) {
        String sql = "SELECT * FROM event_details WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Event(
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("student_register_number"),
                            rs.getString("event_name"),
                            rs.getString("event_date"),
                            rs.getString("event_place"),
                            rs.getString("prize_won")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting event by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an event record.
     */
    public boolean updateEvent(Event event) {
        String sql = "UPDATE event_details SET student_name = ?, student_register_number = ?, event_name = ?, event_date = ?, event_place = ?, prize_won = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getStudentName());
            stmt.setString(2, event.getStudentRegisterNumber());
            stmt.setString(3, event.getEventName());
            stmt.setString(4, event.getEventDate());
            stmt.setString(5, event.getEventPlace());
            stmt.setString(6, event.getPrizeWon());
            stmt.setInt(7, event.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete an event by ID.
     */
    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM event_details WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search events by Event Name, Student Name, or Register Number.
     */
    public List<Event> searchEvents(String keyword) {
        List<Event> result = new ArrayList<>();
        String sql = "SELECT * FROM event_details WHERE event_name LIKE ? OR student_name LIKE ? OR student_register_number LIKE ? OR event_place LIKE ? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);
            stmt.setString(3, term);
            stmt.setString(4, term);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Event e = new Event(
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("student_register_number"),
                            rs.getString("event_name"),
                            rs.getString("event_date"),
                            rs.getString("event_place"),
                            rs.getString("prize_won")
                    );
                    result.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching events: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get total count of events registered.
     */
    public int getTotalEventCount() {
        String sql = "SELECT COUNT(*) FROM event_details";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total event count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get count of specific prize types ('1st Prize', '2nd Prize', '3rd Prize', 'Participation').
     */
    public int getPrizeCount(String prizeType) {
        String sql = "SELECT COUNT(*) FROM event_details WHERE prize_won = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prizeType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting prize count for " + prizeType + ": " + e.getMessage());
        }
        return 0;
    }
}
