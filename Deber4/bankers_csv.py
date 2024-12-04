import pandas as pd
import numpy as np

def load_data(file_path):
    df = pd.read_csv(file_path)
    
    total = df.iloc[0, :].to_numpy()[1:].astype(int)
    df = df.iloc[1:, :]
    
    allocation = []
    max_matrix = []
    process_names = []

    for _, row in df.iterrows():
        process_names.append(row.iloc[0])
        allocation_row = []
        max_row = []
        for i in range(1, len(row)):
            allocation_row.append(row.iloc[i].split(';')[0])
            max_row.append(row.iloc[i].split(';')[1])
        allocation.append(allocation_row)
        max_matrix.append(max_row)

    allocation = np.array(allocation).astype(int)
    max_matrix = np.array(max_matrix).astype(int)
    n_procesos = len(max_matrix)

    # Validar que los valores de asignación y máximos sean correctos
    for i in range(n_procesos):
        if np.any(max_matrix[i] - allocation[i] < 0):
            print(f"El proceso {process_names[i]} tiene un valor inválido de máximo y asignado")
            for r, (alloc, max_value) in enumerate(zip(allocation[i], max_matrix[i])):
                if not (max_value > alloc):
                    print(f"Recurso_{r+1} (allocated_value; max_value): ({alloc}; {max_value})")
            raise ValueError("Valores inválidos encontrados en la matriz de recursos.")

    # Calcular  y validar los recursos disponibles
    allocated = np.sum(allocation, axis=0)
    available = []
    for r, (tot, alloc) in enumerate(zip(total, allocated)):
        diferencia = tot - alloc
        if diferencia >= 0:
            available.append(diferencia)
        else:
            print(f"Instancias insuficientes del Recurso_{r+1}: total = {tot}, total_allocated = {alloc}")
    
    available = np.array(available)
    return process_names, available, max_matrix, allocation

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

if __name__ == "__main__":
    print("Ejemplo 0:")
    process_names, available, max_matrix, allocation = load_data('ejemplo0.csv')
    is_safe_state(process_names, available, max_matrix, allocation)
    print()

    print("Ejemplo 1:")
    process_names, available, max_matrix, allocation = load_data('ejemplo1.csv')
    is_safe_state(process_names, available, max_matrix, allocation)
    print()

    print("Ejemplo 2:")
    process_names, available, max_matrix, allocation = load_data('ejemplo2.csv')
    is_safe_state(process_names, available, max_matrix, allocation)
    print()

    print("Test:")
    process_names, available, max_matrix, allocation = load_data('test.csv')
    is_safe_state(process_names, available, max_matrix, allocation)
    print()