import java.util.*;

public class CPU_Project {

    static class Process {
        int PID, arrivaltime, bursttime, remaining, priority;
        int completiontime, startTime = -1, response = -1;
        // Constructor for the Process Class
        public Process(int PID, int arrivaltime, int bursttime, int priority) {
            this.PID = PID; this.arrivaltime = arrivaltime;
            this.bursttime = bursttime; this.remaining = bursttime;
            this.priority = priority;
        }
    }

    public static class SchedulerCPU {
        List<Process> processes;
        int Switches = 0; // Context Switchers Count

        public SchedulerCPU(List<Process> processes) {
            this.processes = processes;
        }

        // First Come First Serve Scheduling Algorithm
        public void FCFS() {
            processes.sort(Comparator.comparingInt(p -> p.arrivaltime)); // Sort by arrival time
            int time = 0;
            for (Process p : processes) {
                if (time < p.arrivaltime){
                    time = p.arrivaltime;
                }
                p.startTime = time; // Set start time
                time += p.bursttime;
                p.completiontime = time; // Set completion time
            }
            printMetrics("FCFS");
        }

        // Shortest Remaining Time First (Preemptive)
        public void STF() {
            int time = 0, finised = 0;
            Process present = null;

            while (finised < processes.size()) {
                Process shorter = null;
                // Finds the process with the shortest time remaining
                for (Process p : processes) {
                    if (p.arrivaltime <= time && p.remaining > 0 && (shorter == null || p.remaining < shorter.remaining)) {
                        shorter = p;
                    }
                }
                if (shorter == null) {
                    time++;
                    continue;
                }
                if (present != shorter) {
                    Switches++; // Context switch when changing process
                    present = shorter;
                }
                if (shorter.startTime == -1) shorter.startTime = time;
                shorter.remaining--; // Execute process for 1 unit
                time++;
                if (shorter.remaining == 0) {
                    shorter.completiontime = time;
                    finised++;
                }
            }
            printMetrics("SRTF");
        }

        // Priority Preemptive Scheduling
        public void PP() {
            int time = 0, completed = 0;
            Process current = null;
            while (completed < processes.size()) {
                Process highest = null;
                // Finds the process with the highest priority
                for (Process p : processes) {
                    if (p.arrivaltime <= time && p.remaining > 0 && (highest == null || p.priority < highest.priority)) {
                        highest = p;
                    }
                }
                if (highest == null) {
                    time++;
                    continue;
                }
                if (current != highest) {Switches++; current = highest;} // Context switch
                if (highest.startTime == -1) {highest.startTime = time;}
                highest.remaining--;
                time++;
                if (highest.remaining == 0) {
                    highest.completiontime = time;
                    completed++;
                }
            }
            printMetrics("Priority Preemptive");
        }

        // Round Robin Scheduling
        public void RR(int Q) {
            Queue<Process> queue = new LinkedList<>();
            int time = 0, completed = 0; // Current time and number of completed processes
            Set<Integer> seen = new HashSet<>();
            Process current = null;
            while (completed < processes.size()) {
                for (Process p : processes) {
                    if (p.arrivaltime <= time && !seen.contains(p.PID)) {
                        queue.offer(p);
                        seen.add(p.PID);
                    }
                }
                // If no process is ready, CPU is idle for one unit
                if (queue.isEmpty()) {
                    time++;
                    continue;
                }
                Process p = queue.poll(); // Pick the next process
                if (current != p) {Switches++; current = p;}
                if (p.startTime == -1) p.startTime = time;
                // Execute the process for either Q units or remaining time, whichever is smaller
                int ec = Math.min(Q, p.remaining);
                p.remaining -= ec;
                time += ec; // Move time forward


                for (Process other : processes) {
                    if (other.arrivaltime <= time && !seen.contains(other.PID)) {
                        queue.offer(other);
                        seen.add(other.PID);
                    }
                }
                // If the process still has remaining burst time add it to queue
                if (p.remaining > 0) {
                    queue.offer(p);
                } else {
                    p.completiontime = time;
                    completed++;
                }
            }
            printMetrics("Round Robin (q=" + Q + ")");
        }

        // Highest Response Ratio Next (Non-Preemptive)
        public void HRRN() {
            List<Process> readyQueue = new ArrayList<>();
            int time = 0, completed = 0;
            while (completed < processes.size()) {
                // Add newly arrived processes to the ready queue
                for (Process p : processes) {
                    if (p.arrivaltime <= time && !readyQueue.contains(p) && p.remaining > 0) {
                        readyQueue.add(p);
                    }
                }
                if (readyQueue.isEmpty()) {
                    time++;
                    continue;
                }
                int finalTime = time;
                // Chooses the process with the highest response ratio
                Process best = readyQueue.stream()
                        .max(Comparator.comparingDouble(p -> (1.0 + (finalTime - p.arrivaltime) * 1.0 / p.bursttime)))
                        .orElse(null);
                if (best != null) {
                    if (best.startTime == -1) best.startTime = time;
                    time += best.bursttime; // Execute fully (non-preemptive)
                    best.completiontime = time;
                    best.remaining = 0;
                    readyQueue.remove(best);
                    completed++;
                }
            }
            printMetrics("HRRN");
        }

        // Multilevel Feedback Queue Scheduling
        public void MLFQ(int[] quantums) {
            Queue<Process>[] queues = new LinkedList[quantums.length];
            for (int i = 0; i < quantums.length; i++){ queues[i] = new LinkedList<>();}
            int time = 0, completed = 0;
            Set<Integer> seen = new HashSet<>();
            Process current = null; // For counting context switches

            while (completed < processes.size()) {
                // Add arrived processes to the highest queue
                for (Process p : processes) {
                    if (p.arrivaltime <= time && !seen.contains(p.PID)) {
                        queues[0].offer(p);
                        seen.add(p.PID);
                    }
                }
                boolean found = false;
                // Iterate through queues from high to low priority
                for (int i = 0; i < quantums.length; i++) {
                    Queue<Process> q = queues[i];
                    if (!q.isEmpty()) {
                        Process p = q.poll();
                        if (current != p) {Switches++; current = p;}
                        if (p.startTime == -1) {p.startTime = time;}
                        int exec = Math.min(quantums[i], p.remaining);
                        p.remaining -= exec;
                        time += exec;
                        // Add any newly arriving processes after execution
                        for (Process other : processes) {
                            if (other.arrivaltime <= time && !seen.contains(other.PID)) {
                                queues[0].offer(other);
                                seen.add(other.PID);
                            }
                        }
                        if (p.remaining > 0 && i + 1 < quantums.length) queues[i + 1].offer(p);
                        else if (p.remaining > 0) queues[i].offer(p);
                        else {
                            p.completiontime = time;
                            completed++;
                        }
                        found = true;
                        break;
                    }
                }
                if (!found){ time++;}
            }
            printMetrics("MLFQ");
        }
        // Print Metrics for the Scheduler
        public void printMetrics(String algorithm) {
            double totalWT = 0, totalTAT = 0;
            int lastCompletion = 0;
            int totalBurst = 0;
            System.out.println("\n--- Results for " + algorithm + " ---");
            System.out.printf("%-5s %-8s %-6s %-8s %-8s %-8s\n", "PID", "Arrival", "Burst", "Waiting", "TurnT", "Start");
            for (Process p : processes) {
                int TAT = p.completiontime - p.arrivaltime; // Turnaround Time
                int wt = TAT - p.bursttime; // Waiting Time
                totalWT += wt;
                totalTAT += TAT;
                totalBurst += p.bursttime;
                if (p.completiontime > lastCompletion) {
                    lastCompletion = p.completiontime;
                }
                System.out.printf("%-5d %-8d %-6d %-8d %-8d %-8d\n", p.PID, p.arrivaltime, p.bursttime, wt, TAT, p.startTime);
            }
            double avgWT = totalWT / processes.size();
            double avgTAT = totalTAT / processes.size();
            double throughput = (double) processes.size() / lastCompletion * 1000;
            double cpuUtil = (double) totalBurst / lastCompletion * 100;
            System.out.printf("\nAverage Waiting Time: %.2f\n", avgWT);
            System.out.printf("Average Turnaround Time: %.2f\n", avgTAT);
            System.out.printf("Throughput (per 1000 units): %.2f\n", throughput);
            System.out.printf("CPU Utilization: %.2f%%\n", cpuUtil);
            System.out.printf("Context Switches: %d\n", Switches);
        }
    }

    // Main method
    public static void main(String[] args) {
        List<Process> testProcesses = List.of(
                new Process(1, 0, 5, 3),
                new Process(2, 2, 3, 1),
                new Process(3, 4, 1, 4),
                new Process(4, 6, 2, 2)
        );
        runAllAlgorithms(testProcesses);
    }
    // Run all the scheduling algorithms one at a time
    private static void runAllAlgorithms(List<Process> testProcesses) {
        SchedulerCPU scheduler;
        scheduler = new SchedulerCPU(cloneProcessList(testProcesses));
        scheduler.FCFS();
        scheduler = new SchedulerCPU(cloneProcessList(testProcesses));
        scheduler.STF();
        scheduler = new SchedulerCPU(cloneProcessList(testProcesses));
        scheduler.PP();
        scheduler = new SchedulerCPU(cloneProcessList(testProcesses));
        scheduler.RR(2);
        scheduler = new SchedulerCPU(cloneProcessList(testProcesses));
        scheduler.HRRN();
        scheduler = new SchedulerCPU(cloneProcessList(testProcesses));
        scheduler.MLFQ(new int[]{2, 4, 6});
    }
    // Clone the process list, so it can start fresh
    private static List<Process> cloneProcessList(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process p : original) {
            copy.add(new Process(p.PID, p.arrivaltime, p.bursttime, p.priority));
        }
        return copy;
    }
}