# Joshua-Dupati-CS-3502-Project-2

# CPU Scheduling Simulator

## Author
Joshua Dupati  
(Spring 2025 - CPU Scheduling Simulation Project)


# Overview
This project simulates and compares multiple CPU scheduling algorithms in Java.  
It calculates performance metrics such as:
- Average Waiting Time (AWT)
- Average Turnaround Time (ATT)
- CPU Utilization
- Throughput
- Context Switches

Each scheduling algorithm is tested on a sample workload and results are printed clearly.

---

#Implemented Algorithms
 FCFS (First Come First Serve)- Processes are scheduled in the order they arrive. 
 SRTF(Shortest Remaining Time First) - choosing the job with the least remaining burst time. 
 Priority Preemptive - Always runs the process with the lowest priority value. Preemptive. 
 Round Robin (RR) -  Each process gets a fixed time slice (quantum) to execute in cyclic order. 
 HRRN(Highest Response Ratio Next) - Non-preemptive algorithm choosing the process with the highest response ratio. 
 MLFQ (Multilevel Feedback Queue) | Processes are scheduled across multiple queues with different priorities and quantums, dynamically adjusting based on behavior. |

## How to Run
1. Compile the program:

    ```bash
    javac CPU_Project.java
    ```

2. Or Download the program and run it

## Project Structure
- `Process`: Class representing each process.
- `SchedulerCPU`: Class implementing different scheduling algorithms.
- `printMetrics()`: Method for calculating and displaying statistics.
- `cloneProcessList()`: Utility method for testing algorithms independently.



## Notes
- Context switches are counted whenever a different process starts running.
- CPU utilization = active CPU time over the full timeline.
- Round Robin and MLFQ accept configurable time quantum(s).


