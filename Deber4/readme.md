# Explicación de los inputs

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
