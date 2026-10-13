/**
 * Student Model Class representing a student entity in the system.
 */
public class Student {
    private int id;
    private String studentName;
    private String registerNumber;
    private String phoneNumber;
    private String email;

    // Default Constructor
    public Student() {
    }

    // Constructor without ID (used for insertion)
    public Student(String studentName, String registerNumber, String phoneNumber, String email) {
        this.studentName = studentName;
        this.registerNumber = registerNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Constructor with ID (used for updates and database retrievals)
    public Student(int id, String studentName, String registerNumber, String phoneNumber, String email) {
        this.id = id;
        this.studentName = studentName;
        this.registerNumber = registerNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
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

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Name: %s | Reg No: %s | Phone: %s | Email: %s",
                id, studentName, registerNumber, phoneNumber, email);
    }
}
