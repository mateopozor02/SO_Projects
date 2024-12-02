import numpy as np

def is_safe_state(processes, available, max_matrix, allocation):
    num_processes = len(processes)
    num_resources = len(available)
    
    # Calculate the need matrix
    need = np.subtract(max_matrix, allocation)
    
    # Initialize work (available resources) and finished processes
    work = available.copy()
    finished = [False] * num_processes
    safe_sequence = []
    
    while len(safe_sequence) < num_processes:
        progress_made = False
        
        for i in range(num_processes):
            # If the process is not finished and its needs can be satisfied with the current work resources
            if not finished[i] and all(need[i][j] <= work[j] for j in range(num_resources)):
                # Add resources back to work vector
                for j in range(num_resources):
                    work[j] += allocation[i][j]
                finished[i] = True
                safe_sequence.append(processes[i])
                progress_made = True
        
        if not progress_made:
            print("System is in an unsafe state.")
            return False, []
    
    print(f"System is in a safe state.\nSafe sequence: {safe_sequence}")
    return True, safe_sequence

def main():
    # Example input values
    num_processes = int(input("Enter number of processes: "))
    num_resources = int(input("Enter number of resource types: "))

    processes = [f"P{i}" for i in range(num_processes)]
    
    print(f"\nEnter total instances of each resource type ({num_resources} values):")
    total_resources = np.array(list(map(int, input().split())))
    
    # Initialize matrices
    max_matrix = np.zeros((num_processes, num_resources), dtype=int)
    allocation = np.zeros((num_processes, num_resources), dtype=int)

    print("\nEnter maximum resource requirement matrix:")
    for i in range(num_processes):
        print(f"Max for process {processes[i]} ({num_resources} values):", end=" ")
        max_matrix[i] = np.array(list(map(int, input().split())))
    
    print("\nEnter current allocation matrix:")
    for i in range(num_processes):
        print(f"Allocation for process {processes[i]} ({num_resources} values):", end=" ")
        allocation[i] = np.array(list(map(int, input().split())))
    
    # Validate input
    if np.any(allocation > max_matrix):
        print("Error: Allocation cannot be greater than maximum need")
        return
        
    # Calculate available resources
    available = total_resources - np.sum(allocation, axis=0)

    # Check if the system is in a safe state
    print("Processes:", processes)
    is_safe, sequence = is_safe_state(processes, available, max_matrix, allocation)

if __name__ == "__main__":
    main()