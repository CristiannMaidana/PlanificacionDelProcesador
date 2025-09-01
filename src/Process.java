public class Process {
    String processName;
    int arrivalTime, cantBurstCPU, timeBurstCPU, timerBurstIO, priority;

    Process(String processName, int arrivalTime, int cantBurstCPU, int timerBurstCPU, int timerBurstIO, int priority) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.cantBurstCPU = cantBurstCPU;
        this.timeBurstCPU = timerBurstCPU;
        this.timerBurstIO = timerBurstIO;
        this.priority = priority;
    }

    public Process getProcess() {
        return this;
    }

    @Override
    public String toString() {
        return "Process{" +
                "name='" + processName + '\'' +
                ", arrival=" + arrivalTime +
                ", cantBurstCPU=" + cantBurstCPU +
                ", timeBurstCPU=" + timeBurstCPU +
                ", timeBurstIO=" + timerBurstIO +
                ", priority=" + priority +
                '}';
    }
}
