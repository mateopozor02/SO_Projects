import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

public class Shell {
    // Hash Map to store the commands
    private static Map<Integer, String> history;
    // Keep track of the number of commands entered by the user
    private static int commandNumber;
    // Keep track of the current directory
    private static String currentDirectory; 
    // Keep track of the previous directory
    private static String previousDirectory;

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String commandLine;
        history = new HashMap<>();
        commandNumber = 0;
        currentDirectory = System.getProperty("user.dir");

        try {
            while (true) {
                System.out.print(">>");

                // Read input from user
                commandLine = reader.readLine();

                // Remove leading and trailing whitespaces
                commandLine = commandLine.trim();

                if (commandLine.contains("&")) {
                    // Store the command and the command number in history
                    history.put(commandNumber + 1, commandLine);
                    commandNumber++;
                    runMultipleCommands(commandLine);
                } else {
                    // Split the given command
                    String[] command = commandLine.split(" ");

                    // Check if the command is not related to history
                    if (!(command[0].charAt(0) == '!')) {
                        // Save the command and the command number in history
                        history.put(commandNumber + 1, commandLine);
                        commandNumber++;
                    }

                    runCommand(command);
                }
                // Reset command number if it reaches 20
                if (commandNumber == 20) {
                    commandNumber = 0;
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the command");
            e.printStackTrace();
        }
    }

    private static void runCommand(String[] command) {
        try {
            // Check if the command is exit
            if (command[0].equals("exit")) {
                if (command.length > 1) {
                    System.out.println("Invalid command, invalid number of arguments");
                    return;
                }
                System.exit(0);
            } else if (command[0].equals("history")){
                // Print the history of commands
                printHistory();
                return;
            } else if (command[0].charAt(0) == '!'){
                // Check if the user wants to run the last command (!#) entered
                if (command[0].charAt(1) == '#'){
                    // Get the last command entered by the user
                    String lastCommand = history.get(commandNumber);
                    // Check if last command is a multiple command
                    if (lastCommand.contains("&")){
                        runMultipleCommands(lastCommand);
                        return; // Exit the function
                    } else {
                        // Split the last command
                        String[] lastCommandSplit = lastCommand.split(" ");
                        // Store last command in the command array
                        command = lastCommandSplit; 
                    }
                } else {
                    // Get the command number 
                    int commandNumber = Integer.parseInt(command[0].substring(1));
                    // Check if command number is valid
                    if (history.get(commandNumber) != null){
                        // Get the command from the history
                        String lastCommand = history.get(commandNumber);
                        // Check if last command is a multiple command
                        if (lastCommand.contains("&")){
                            runMultipleCommands(lastCommand);
                            return; // Exit the function
                        } else {
                            // Split the last command
                            String[] lastCommandSplit = lastCommand.split(" ");
                            // Store last command in the command array
                            command = lastCommandSplit;
                        }
                    } else {
                        System.out.println("Invalid command number");
                        return;
                    }
                }
            }

            // Check if the query is a history command
            if (command[0].equals("history")) {
                printHistory();
                return;
            }

            // Validate allowed commands
            if (!isValidCommand(command[0])) {
                System.out.println("Invalid command: " + command[0]);
                return;
            }

            // Handle built-in commands: cd, ping, ifconfig/ipconfig
            if (command[0].equals("cd")) {
                if (command.length != 2) {
                    System.out.println("Invalid cd usage. Usage: cd <directory>");
                    return;
                }

                // Print the name of the command with its arguments
                for (int i = 0; i < command.length; i++) {
                    if (i < command.length - 1) System.out.print(command[i] + " ");
                    else System.out.print(command[i]);
                }
                System.out.print(":\n");

                changeDirectory(command[1]);
                
                return;
            }

            if (command[0].equals("ping")) {
                if (!validatePing(command)) {
                    return;
                }
            }

            if (command[0].equals("ifconfig") || command[0].equals("ipconfig")) {
                if (!handleNetworkConfig(command[0])) {
                    return;
                }
            }

            // Create a process to run the command
            ProcessBuilder pb = new ProcessBuilder(command);
            // Set the working directory
            pb.directory(Paths.get(currentDirectory).toFile());
            Process process = pb.start();
            int exitValue;
            BufferedReader br = null;

            try {
                exitValue = process.waitFor();
                // Check the exit value and assign stdout or stderr to br
                if (exitValue == 0) {
                    br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Read output
            StringBuilder output = new StringBuilder();
            String line;

            // Print the name of the command with its arguments
            for (int i = 0; i < command.length; i++) {
                if (i < command.length - 1) System.out.print(command[i] + " ");
                else System.out.print(command[i]);
            }
            System.out.print(":\n");

            while ((line = br.readLine()) != null) {
                output.append(line).append('\n');
                System.out.println(line);
            }

        } catch (IOException e) {
            System.out.println("Command execution failed");
            e.printStackTrace();
        }
    }

    private static boolean isValidCommand(String command) {
        return command.equals("ls") || command.equals("cd") || command.equals("echo") || command.equals("ping")
                || command.equals("ifconfig") || command.equals("ipconfig") || command.equals("exit") || command.equals("pwd");
    }

    private static void changeDirectory(String path) {
        try {
            if (path.equals("~")) {
                path = System.getProperty("user.home");
            } else if (path.equals("..")) {
                // Get the parent directory of the current directory
                java.nio.file.Path parentDir = Paths.get(currentDirectory).getParent();
                if (parentDir != null) {
                    path = parentDir.toString();
                } else {
                    System.out.println("Already at the root directory.");
                    return;
                }
            }

            // Resolve the path relative to the current directory
            Path newPath = Paths.get(currentDirectory).resolve(path).normalize();

            // Check if the new path is a valid directory
            if (!Files.exists(newPath) || !Files.isDirectory(newPath)) {
                System.out.println("Directory does not exist: " + newPath);
                return;
            }
    
            // Update directories
            previousDirectory = currentDirectory;
            currentDirectory = newPath.toString();
            System.setProperty("user.dir", currentDirectory);
            System.out.println("Directory changed to " + currentDirectory);
        } catch (Exception e) {
            System.out.println("Failed to change directory: " + e.getMessage());
        }
    }
    

    private static boolean validatePing(String[] command) {
        if (command.length != 4 || !command[1].equals("-c")) {
            System.out.println("Invalid ping usage. Usage: ping -c <count> <address>");
            return false;
        }
        return true;
    }

    private static boolean handleNetworkConfig(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        if (command.equals("ifconfig") && os.contains("win")) {
            System.out.println("Use 'ipconfig' for Windows");
            return false;
        }
        if (command.equals("ipconfig") && !os.contains("win")) {
            System.out.println("Use 'ifconfig' for Linux/macOS");
            return false;
        }
        return true;
    }

    private static void printHistory() {
        // Check if the history is empty
        if (history.isEmpty()) {
            System.out.println("No commands entered yet");
            return;
        }
        // Print the history of commands
        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + 1 + ". " + history.get(i + 1));
        }
    }

    private static void runMultipleCommands(String concatenatedCommand) {
        // Split the given command
        String[] commands = concatenatedCommand.split("&");
        for (String cmd : commands) {
            // Trim spaces and split by spaces
            String[] commandSplit = cmd.trim().split("\\s+");
            if (commandSplit.length > 0) {
                runCommand(commandSplit);
            }
        }
    }
}
