import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class providing both a Java Console CLI menu 
 * and an embedded JDK Web HTTP Server with JDBC connectivity.
 */
public class Main {

    private static final int PORT = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final EventDAO eventDAO = new EventDAO();

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("      STUDENT EVENT-MANAGEMENT SYSTEM (Java + JDBC + MySQL)      ");
        System.out.println("==================================================================");

        // Auto-initialize MySQL database & tables if they do not exist
        DBConnection.initializeDatabase();

        // Start background Web Server on port 8080 using built-in JDK HttpServer
        startHttpServer();

        // Start Console CLI Interface
        runConsoleCLI();
    }

    /**
     * Starts lightweight JDK HTTP Web Server for serving static web files and REST endpoints.
     */
    private static void startHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Endpoint: /api/stats
            server.createContext("/api/stats", exchange -> {
                addCorsHeaders(exchange);
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    int totalStudents = studentDAO.getTotalStudentCount();
                    int totalEvents = eventDAO.getTotalEventCount();
                    int prize1 = eventDAO.getPrizeCount("1st Prize");
                    int prize2 = eventDAO.getPrizeCount("2nd Prize");
                    int prize3 = eventDAO.getPrizeCount("3rd Prize");
                    int participation = eventDAO.getPrizeCount("Participation");

                    String json = String.format(
                        "{\"totalStudents\":%d,\"totalEvents\":%d,\"firstPrize\":%d,\"secondPrize\":%d,\"thirdPrize\":%d,\"participation\":%d}",
                        totalStudents, totalEvents, prize1, prize2, prize3, participation
                    );
                    sendJsonResponse(exchange, 200, json);
                } else if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            // Endpoint: /api/students
            server.createContext("/api/students", new StudentsHandler());

            // Endpoint: /api/events
            server.createContext("/api/events", new EventsHandler());

            // Endpoint: Static file server (index.html, css, js)
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(null);
            server.start();
            System.out.println(">> Web Application Server started at: http://localhost:" + PORT);
            System.out.println(">> You can open http://localhost:" + PORT + " in your web browser!");
            System.out.println("==================================================================");
        } catch (Exception e) {
            System.err.println("Note: Could not bind HttpServer on port " + PORT + " (" + e.getMessage() + ")");
            System.out.println("You can still use the Console CLI below.");
        }
    }

    /**
     * Interactive Java Console CLI Interface.
     */
    private static void runConsoleCLI() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n---------------- MAIN MENU ----------------");
            System.out.println("1. Student Details Module");
            System.out.println("2. Event Details Module");
            System.out.println("3. View Dashboard Statistics");
            System.out.println("4. Exit Application");
            System.out.print("Select an option (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleStudentMenu(scanner);
                    break;
                case "2":
                    handleEventMenu(scanner);
                    break;
                case "3":
                    displayDashboardStats();
                    break;
                case "4":
                    exit = true;
                    System.out.println("\nThank you for using Student Event-Management System!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice! Please enter a number between 1 and 4.");
            }
        }
        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Student Module Console Handlers
    // -------------------------------------------------------------------------
    private static void handleStudentMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- STUDENT DETAILS MODULE ---");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Edit Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter choice (1-6): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    System.out.print("Enter Student Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Enter Register Number: ");
                    String regNo = scanner.nextLine().trim();
                    System.out.print("Enter Phone Number (10 digits): ");
                    String phone = scanner.nextLine().trim();
                    System.out.print("Enter Email Address: ");
                    String email = scanner.nextLine().trim();

                    // Validations
                    if (name.isEmpty() || regNo.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        System.out.println("[ERROR] All fields are required!");
                        break;
                    }
                    if (!phone.matches("\\d{10}")) {
                        System.out.println("[ERROR] Phone Number must be exactly 10 digits!");
                        break;
                    }
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        System.out.println("[ERROR] Invalid Email Address format!");
                        break;
                    }
                    if (studentDAO.isRegisterNumberExists(regNo, 0)) {
                        System.out.println("[ERROR] Register Number already exists!");
                        break;
                    }

                    Student newStudent = new Student(name, regNo, phone, email);
                    if (studentDAO.addStudent(newStudent)) {
                        System.out.println("[SUCCESS] Student added successfully!");
                    } else {
                        System.out.println("[ERROR] Failed to add student into MySQL database.");
                    }
                    break;

                case "2":
                    List<Student> students = studentDAO.getAllStudents();
                    System.out.println("\n---------------- ALL REGISTERED STUDENTS ----------------");
                    if (students.isEmpty()) {
                        System.out.println("No student records found.");
                    } else {
                        for (Student s : students) {
                            System.out.println(s);
                        }
                    }
                    break;

                case "3":
                    System.out.print("Enter Register Number or Name to search: ");
                    String q = scanner.nextLine().trim();
                    List<Student> searchResult = studentDAO.searchStudents(q);
                    System.out.println("\n---------------- SEARCH RESULTS ----------------");
                    if (searchResult.isEmpty()) {
                        System.out.println("No matching students found.");
                    } else {
                        for (Student s : searchResult) {
                            System.out.println(s);
                        }
                    }
                    break;

                case "4":
                    System.out.print("Enter ID of Student to Edit: ");
                    try {
                        int editId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Updated Student Name: ");
                        String uName = scanner.nextLine().trim();
                        System.out.print("Enter Updated Register Number: ");
                        String uRegNo = scanner.nextLine().trim();
                        System.out.print("Enter Updated Phone Number (10 digits): ");
                        String uPhone = scanner.nextLine().trim();
                        System.out.print("Enter Updated Email Address: ");
                        String uEmail = scanner.nextLine().trim();

                        if (uName.isEmpty() || uRegNo.isEmpty() || uPhone.isEmpty() || uEmail.isEmpty()) {
                            System.out.println("[ERROR] Fields cannot be empty!");
                            break;
                        }
                        if (!uPhone.matches("\\d{10}")) {
                            System.out.println("[ERROR] Phone Number must be exactly 10 digits!");
                            break;
                        }
                        if (!uEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                            System.out.println("[ERROR] Invalid Email Address format!");
                            break;
                        }
                        if (studentDAO.isRegisterNumberExists(uRegNo, editId)) {
                            System.out.println("[ERROR] Register Number belongs to another student!");
                            break;
                        }

                        Student updated = new Student(editId, uName, uRegNo, uPhone, uEmail);
                        if (studentDAO.updateStudent(updated)) {
                            System.out.println("[SUCCESS] Student details updated!");
                        } else {
                            System.out.println("[ERROR] Student ID not found or update failed.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid ID format.");
                    }
                    break;

                case "5":
                    System.out.print("Enter ID of Student to Delete: ");
                    try {
                        int delId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Are you sure you want to delete this student? (Y/N): ");
                        String confirm = scanner.nextLine().trim();
                        if ("Y".equalsIgnoreCase(confirm) || "YES".equalsIgnoreCase(confirm)) {
                            if (studentDAO.deleteStudent(delId)) {
                                System.out.println("[SUCCESS] Student record deleted successfully!");
                            } else {
                                System.out.println("[ERROR] Deletion failed. ID may not exist.");
                            }
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid ID format.");
                    }
                    break;

                case "6":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Event Module Console Handlers
    // -------------------------------------------------------------------------
    private static void handleEventMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- EVENT DETAILS MODULE ---");
            System.out.println("1. Add Event Record");
            System.out.println("2. View All Event Records");
            System.out.println("3. Search Events");
            System.out.println("4. Update Event Record");
            System.out.println("5. Delete Event Record");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter choice (1-6): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    System.out.print("Enter Student Name: ");
                    String sName = scanner.nextLine().trim();
                    System.out.print("Enter Student Register Number: ");
                    String sReg = scanner.nextLine().trim();
                    System.out.print("Enter Event Name: ");
                    String eName = scanner.nextLine().trim();
                    System.out.print("Enter Date of Event (YYYY-MM-DD): ");
                    String eDate = scanner.nextLine().trim();
                    System.out.print("Enter Place of Event: ");
                    String ePlace = scanner.nextLine().trim();

                    System.out.println("Select Prize Won:");
                    System.out.println("1) 1st Prize  2) 2nd Prize  3) 3rd Prize  4) Participation");
                    System.out.print("Choose prize option (1-4): ");
                    String pChoice = scanner.nextLine().trim();
                    String prizeWon = "";
                    if ("1".equals(pChoice)) prizeWon = "1st Prize";
                    else if ("2".equals(pChoice)) prizeWon = "2nd Prize";
                    else if ("3".equals(pChoice)) prizeWon = "3rd Prize";
                    else if ("4".equals(pChoice)) prizeWon = "Participation";

                    if (sName.isEmpty() || sReg.isEmpty() || eName.isEmpty() || eDate.isEmpty() || ePlace.isEmpty() || prizeWon.isEmpty()) {
                        System.out.println("[ERROR] All fields including a valid Prize selection are required!");
                        break;
                    }

                    Event newEvent = new Event(sName, sReg, eName, eDate, ePlace, prizeWon);
                    if (eventDAO.addEvent(newEvent)) {
                        System.out.println("[SUCCESS] Event record added successfully!");
                    } else {
                        System.out.println("[ERROR] Failed to insert event record.");
                    }
                    break;

                case "2":
                    List<Event> events = eventDAO.getAllEvents();
                    System.out.println("\n---------------- ALL EVENT RECORDS ----------------");
                    if (events.isEmpty()) {
                        System.out.println("No event participation records found.");
                    } else {
                        for (Event e : events) {
                            System.out.println(e);
                        }
                    }
                    break;

                case "3":
                    System.out.print("Enter Search Term (Event Name / Reg Number / Student Name): ");
                    String keyword = scanner.nextLine().trim();
                    List<Event> searchResults = eventDAO.searchEvents(keyword);
                    System.out.println("\n---------------- EVENT SEARCH RESULTS ----------------");
                    if (searchResults.isEmpty()) {
                        System.out.println("No matching events found.");
                    } else {
                        for (Event e : searchResults) {
                            System.out.println(e);
                        }
                    }
                    break;

                case "4":
                    System.out.print("Enter Event Record ID to Update: ");
                    try {
                        int editId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Updated Student Name: ");
                        String uSName = scanner.nextLine().trim();
                        System.out.print("Enter Updated Student Register Number: ");
                        String uSReg = scanner.nextLine().trim();
                        System.out.print("Enter Updated Event Name: ");
                        String uEName = scanner.nextLine().trim();
                        System.out.print("Enter Updated Event Date (YYYY-MM-DD): ");
                        String uEDate = scanner.nextLine().trim();
                        System.out.print("Enter Updated Event Place: ");
                        String uEPlace = scanner.nextLine().trim();

                        System.out.println("Select Updated Prize Won:");
                        System.out.println("1) 1st Prize  2) 2nd Prize  3) 3rd Prize  4) Participation");
                        System.out.print("Choose prize option (1-4): ");
                        String pOpt = scanner.nextLine().trim();
                        String uPrize = "";
                        if ("1".equals(pOpt)) uPrize = "1st Prize";
                        else if ("2".equals(pOpt)) uPrize = "2nd Prize";
                        else if ("3".equals(pOpt)) uPrize = "3rd Prize";
                        else if ("4".equals(pOpt)) uPrize = "Participation";

                        if (uSName.isEmpty() || uSReg.isEmpty() || uEName.isEmpty() || uEDate.isEmpty() || uEPlace.isEmpty() || uPrize.isEmpty()) {
                            System.out.println("[ERROR] Fields cannot be empty and valid prize selection is required!");
                            break;
                        }

                        Event updatedEvt = new Event(editId, uSName, uSReg, uEName, uEDate, uEPlace, uPrize);
                        if (eventDAO.updateEvent(updatedEvt)) {
                            System.out.println("[SUCCESS] Event record updated successfully!");
                        } else {
                            System.out.println("[ERROR] Failed to update event record.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid ID format.");
                    }
                    break;

                case "5":
                    System.out.print("Enter ID of Event Record to Delete: ");
                    try {
                        int delId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Are you sure you want to delete this event record? (Y/N): ");
                        String confirm = scanner.nextLine().trim();
                        if ("Y".equalsIgnoreCase(confirm) || "YES".equalsIgnoreCase(confirm)) {
                            if (eventDAO.deleteEvent(delId)) {
                                System.out.println("[SUCCESS] Event record deleted successfully!");
                            } else {
                                System.out.println("[ERROR] Deletion failed. ID not found.");
                            }
                        } else {
                            System.out.println("Deletion cancelled.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid ID format.");
                    }
                    break;

                case "6":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void displayDashboardStats() {
        System.out.println("\n---------------- SYSTEM DASHBOARD METRICS ----------------");
        System.out.println(" Total Registered Students : " + studentDAO.getTotalStudentCount());
        System.out.println(" Total Event Participations: " + eventDAO.getTotalEventCount());
        System.out.println(" 🥇 1st Prize Winners      : " + eventDAO.getPrizeCount("1st Prize"));
        System.out.println(" 🥈 2nd Prize Winners      : " + eventDAO.getPrizeCount("2nd Prize"));
        System.out.println(" 🥉 3rd Prize Winners      : " + eventDAO.getPrizeCount("3rd Prize"));
        System.out.println(" 📜 Participation Certificates: " + eventDAO.getPrizeCount("Participation"));
        System.out.println("---------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // Helper Methods for HttpServer REST Endpoints & CORS
    // -------------------------------------------------------------------------
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // Handler for /api/students
    static class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<Student> students = studentDAO.getAllStudents();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < students.size(); i++) {
                    Student s = students.get(i);
                    json.append(String.format(
                        "{\"id\":%d,\"studentName\":\"%s\",\"registerNumber\":\"%s\",\"phoneNumber\":\"%s\",\"email\":\"%s\"}",
                        s.getId(), escapeJson(s.getStudentName()), escapeJson(s.getRegisterNumber()),
                        escapeJson(s.getPhoneNumber()), escapeJson(s.getEmail())
                    ));
                    if (i < students.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                // Extract parameters from JSON payload
                String name = getJsonVal(body, "studentName");
                String reg = getJsonVal(body, "registerNumber");
                String phone = getJsonVal(body, "phoneNumber");
                String email = getJsonVal(body, "email");

                if (name.isEmpty() || reg.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required fields\"}");
                    return;
                }
                if (studentDAO.isRegisterNumberExists(reg, 0)) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Register Number already exists\"}");
                    return;
                }

                Student s = new Student(name, reg, phone, email);
                boolean ok = studentDAO.addStudent(s);
                sendJsonResponse(exchange, ok ? 201 : 500, "{\"success\":" + ok + "}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                int id = extractIdQuery(query);
                if (id > 0 && studentDAO.deleteStudent(id)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for /api/events
    static class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<Event> events = eventDAO.getAllEvents();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < events.size(); i++) {
                    Event e = events.get(i);
                    json.append(String.format(
                        "{\"id\":%d,\"studentName\":\"%s\",\"studentRegisterNumber\":\"%s\",\"eventName\":\"%s\",\"eventDate\":\"%s\",\"eventPlace\":\"%s\",\"prizeWon\":\"%s\"}",
                        e.getId(), escapeJson(e.getStudentName()), escapeJson(e.getStudentRegisterNumber()),
                        escapeJson(e.getEventName()), escapeJson(e.getEventDate()),
                        escapeJson(e.getEventPlace()), escapeJson(e.getPrizeWon())
                    ));
                    if (i < events.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                String sName = getJsonVal(body, "studentName");
                String sReg = getJsonVal(body, "studentRegisterNumber");
                String eName = getJsonVal(body, "eventName");
                String eDate = getJsonVal(body, "eventDate");
                String ePlace = getJsonVal(body, "eventPlace");
                String prize = getJsonVal(body, "prizeWon");

                if (sName.isEmpty() || sReg.isEmpty() || eName.isEmpty() || eDate.isEmpty() || ePlace.isEmpty() || prize.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid fields\"}");
                    return;
                }

                Event e = new Event(sName, sReg, eName, eDate, ePlace, prize);
                boolean ok = eventDAO.addEvent(e);
                sendJsonResponse(exchange, ok ? 201 : 500, "{\"success\":" + ok + "}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                int id = extractIdQuery(query);
                if (id > 0 && eventDAO.deleteEvent(id)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Static File Handler for Frontend UI
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("d:/Project Files/StudentEventManagementSystem" + path);
            if (!file.exists()) {
                // Try relative execution directory
                file = new File("." + path);
            }

            if (file.exists() && !file.isDirectory()) {
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".png")) contentType = "image/png";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (path.endsWith(".svg")) contentType = "image/svg+xml";

                byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(fileBytes);
                os.close();
            } else {
                String notFound = "<h1>404 Not Found</h1>";
                exchange.sendResponseHeaders(404, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes());
                os.close();
            }
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static String getJsonVal(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private static int extractIdQuery(String query) {
        if (query == null || !query.contains("id=")) return -1;
        try {
            return Integer.parseInt(query.substring(query.indexOf("id=") + 3));
        } catch (Exception e) {
            return -1;
        }
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
