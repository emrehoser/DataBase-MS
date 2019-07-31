package dbSystem;

import java.util.ArrayList;
import java.util.List;


public class DatabaseInstruction {
    private String command = "";
    private String type = "";
    private String name = "";
    private List<String> arguments = new ArrayList<>();

    public DatabaseInstruction() {

    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getArgument(int index) {
        return arguments.get(index);
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addArgument(String arg) {
        this.arguments.add(arg);
    }
}
