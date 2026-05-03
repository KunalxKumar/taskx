package model;

public class ProcessInfo {
    private long pid;
    private String name;
    private String user;
    private long memory;
    private String command;

    public long getPid() { return pid; }
    public void setPid(long pid) { this.pid = pid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public long getMemory() { return memory; }
    public void setMemory(long memory) { this.memory = memory; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    @Override
    public String toString() {
        return "ProcessInfo{" +
                "pid=" + pid +
                ", name='" + name + '\'' +
                ", user='" + user + '\'' +
                ", command='" + command + '\'' +
                "}";
    }
}
