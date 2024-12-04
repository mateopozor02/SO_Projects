# Bankers Algorithm
Este trabajo es una implementacion de Bankers Algorithm. Para ejecutar el algoritmo, se soportan dos tipos 
de input. En el primer caso, se lo hace a través del archivo `bankers_csv.py`, que toma el input de un archivo csv, como en el caso de `ejemplo0.csv, ejemplo1.csv, ejemplo2.csv `. Se puede utilizar el archivo `test.csv` para casos especificos del usuario. 

En el caso del archivo `bankers_algo.py`, el input se toma de consola. Se le pide al usuario los inputs necesarios en cada paso. 

## Explicación de los inputs bankers_csv.py

1. **El número de procesos que van a competir por recursos:**  
   Implícito en el número de filas del archivo CSV.

2. **El número de tipos de recursos con los que cuenta el sistema:**  
   Implícito en el número de columnas del archivo CSV.

3. **El número de instancias de cada tipo de recurso disponibles en su totalidad en el sistema:**  
   Representado en la fila "Total" del archivo CSV. En esta fila, se coloca en cada columna/recurso el total de instancias de cada recurso.

4. **El número máximo de instancias de cada tipo de recurso que podría requerir cada proceso.**
5. **El número de instancias de cada tipo de recurso que ya han sido asignadas a cada proceso por el sistema:**  
   Cada celda `Proceso_i, Recurso_j` contiene dos valores separados por un punto y coma (`;`). El primer valor representa las instancias asignadas del recurso `Recurso_j` al proceso `Proceso_i`. El segundo valor representa el número máximo de instancias del recurso `Recurso_j` que el proceso `Proceso_i` necesitará para completar su tarea.
   Por ejemplo, si tenemos un `Proceso_0` que tiene 7 instancias del `Recurso_0` y el máximo de instancias que necesita para culminar su tarea es 10, entonces en la celda se encontrará el valor: `7;10`.

## Inputs en el programa bankers_algo.py
```bash
>>Enter number of processes: 5
>>Enter number of resource types: 3

>>Enter total instances of each resource type (3 values):
10 5 7

>> Enter maximum resource requirement matrix:
Max for process P0 (3 values): 7 5 3
Max for process P1 (3 values): 3 2 2
Max for process P2 (3 values): 9 0 2
Max for process P3 (3 values): 2 2 2
Max for process P4 (3 values): 4 3 3

>> Enter current allocation matrix:
Allocation for process P0 (3 values): 0 1 0
Allocation for process P1 (3 values): 2 0 0
Allocation for process P2 (3 values): 3 0 2
Allocation for process P3 (3 values): 2 1 1
Allocation for process P4 (3 values): 0 0 2
Processes: ['P0', 'P1', 'P2', 'P3', 'P4']

Output
System is in a safe state.
Safe sequence: ['P1', 'P3', 'P4', 'P0', 'P2']
```