/**
 * Event Model Class representing an event participation record.
 */
public class Event {
    private int id;
    private String studentName;
    private String studentRegisterNumber;
    private String eventName;
    private String eventDate; // Format: YYYY-MM-DD
    private String eventPlace;
    private String prizeWon;  // 1st Prize, 2nd Prize, 3rd Prize, Participation

    // Default Constructor
    public Event() {
    }

    // Constructor without ID (used for insertion)
    public Event(String studentName, String studentRegisterNumber, String eventName, String eventDate, String eventPlace, String prizeWon) {
        this.studentName = studentName;
        this.studentRegisterNumber = studentRegisterNumber;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventPlace = eventPlace;
        this.prizeWon = prizeWon;
    }

    // Constructor with ID (used for updates and database retrievals)
    public Event(int id, String studentName, String studentRegisterNumber, String eventName, String eventDate, String eventPlace, String prizeWon) {
        this.id = id;
        this.studentName = studentName;
        this.studentRegisterNumber = studentRegisterNumber;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventPlace = eventPlace;
        this.prizeWon = prizeWon;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRegisterNumber() {
        return studentRegisterNumber;
    }

    public void setStudentRegisterNumber(String studentRegisterNumber) {
        this.studentRegisterNumber = studentRegisterNumber;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventPlace() {
        return eventPlace;
    }

    public void setEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public String getPrizeWon() {
        return prizeWon;
    }

    public void setPrizeWon(String prizeWon) {
        this.prizeWon = prizeWon;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Student: %s (%s) | Event: %s | Date: %s | Place: %s | Prize: %s",
                id, studentName, studentRegisterNumber, eventName, eventDate, eventPlace, prizeWon);
    }
}
