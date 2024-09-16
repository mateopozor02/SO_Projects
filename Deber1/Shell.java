import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class Shell {
    // Hash Map to store the commands
    private static Map<Integer, String> history; 
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); 
        String commandLine;
        history = new HashMap<>(); 

        try {
            while (true) {
                System.out.print(">>");
                
                // Read input from user 
                commandLine = reader.readLine(); 
                // Split the given command 
                String[] command = commandLine.split(" "); 

                // Process the exit command to end the shell
                if (command[0].equals("exit") && command.length == 1){
                    break; 
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
            // Create a process to run the command
            ProcessBuilder pb = new ProcessBuilder(command); 
            Process process = pb.start(); 

            // Read output 
            StringBuilder output = new StringBuilder(); 
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())); 
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
}