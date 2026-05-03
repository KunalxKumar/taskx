public class TestParser {
    public static void main(String[] args) {
        String line = "\"System Idle Process\",\"0\",\"Services\",\"0\",\"8 K\",\"Unknown\",\"NT AUTHORITY\\SYSTEM\",\"1:05:39\",\"N/A\"";
        String[] parts = line.split("\",\"");
        System.out.println("Name: " + parts[0].replace("\"", "").trim());
        System.out.println("PID: " + parts[1].replace("\"", "").trim());
        System.out.println("Mem: " + parts[4].replace("\"", "").trim());
    }
}
