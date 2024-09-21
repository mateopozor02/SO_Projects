import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class Shell {
    // Hash Map to store the commands
    private static Map<Integer, String> history; 
    // Keep track of the number of commands entered by the user
    private static int commandNumber;
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); 
        String commandLine;
        history = new HashMap<>(); 
        commandNumber = 0;

        try {
            while (true) {
                System.out.print(">>");
                
                // Read input from user 
                commandLine = reader.readLine(); 

                // Remove leading and trailing whitespaces
                commandLine = commandLine.trim();

                // Split the given command 
                String[] command = commandLine.split(" "); 

                // Check if the command is not related to history
                if (!(command[0].charAt(0) == '!')){
                    // Save the command and the command number in history
                    history.put(commandNumber + 1, commandLine);
                    commandNumber++;
                }

                // Reset command number if it reaches 20
                if (commandNumber == 20){
                    commandNumber = 0; 
                }

                runCommand(command);
                 
            }
        } catch(IOException e){
            System.out.println("An error ocurred while reading the command");
            e.printStackTrace();
        }
    }

    private static void runCommand(String[] command){
        try {
            // Check if the command is exit
            if (command[0].equals("exit")){
                if (command.length > 1){
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
                    System.out.println(lastCommand);
                    // Split the last command
                    String[] lastCommandSplit = lastCommand.split(" ");
                    // Store the last command in the command array
                    command = lastCommandSplit;
                } else {
                    // Get the command number
                    int commandNumber = Integer.parseInt(command[0].substring(1));
                    // Get the command from the history
                    String lastCommand = history.get(commandNumber);
                    // Split the last command
                    String[] lastCommandSplit = lastCommand.split(" ");
                    // Store the last command in the command array
                    command = lastCommandSplit;
                }
            }
            // Create a process to run the command
            ProcessBuilder pb = new ProcessBuilder(command); 
            Process process = pb.start(); 
            int exitValue = process.exitValue(); 

            // Read output 
            StringBuilder output = new StringBuilder(); 
            BufferedReader br = null; 

            // Check the exit value and assing stdout or stderror to br
            if (exitValue == 0){
                br = new BufferedReader(new InputStreamReader(process.getInputStream())); 
            } else {
                br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            }

            String line = null;

            // Print the name of the command with its arguments
            for (int i = 0; i < command.length; i++){
                if (i < command.length - 1) System.out.print(command[i] + " ");
                else System.out.print(command[i]); 
            }
            System.out.print(":\n"); 

            while ((line = br.readLine()) != null){
                output.append(line).append('\n');
                System.out.println(line);
            }

        } catch(IOException e){
            System.out.println("Command execution failed");
            e.printStackTrace();
        }
    }

    private static void printHistory(){
        // Check if the history is empty
        if (history.isEmpty()){
            System.out.println("No commands entered yet");
            return;
        }
        // Print the history of commands
        for (int i = 0; i < history.size(); i++){
            System.out.println(i + 1 + ". " + history.get(i + 1));
        }
    }
}