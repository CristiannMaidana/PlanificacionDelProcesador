public class Process {
    String processName;
    int arrivalTime, cantBurstCPU, timeBurstCPU, timerBurstIO, priority;

    int remainingBursts;
    int remCPU;
    int curPriority;                

    Process(String processName, int arrivalTime, int cantBurstCPU, int timerBurstCPU, int timerBurstIO, int priority) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.cantBurstCPU = cantBurstCPU;
        this.timeBurstCPU = timerBurstCPU;
        this.timerBurstIO = timerBurstIO;
        this.priority = priority;

        this.remainingBursts = cantBurstCPU;
        this.remCPU = timerBurstCPU;
        this.curPriority = priority;
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

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getCantBurstCPU() {
        return cantBurstCPU;
    }

    public int getTimerBurstCPU() {
        return timeBurstCPU;
    }

    public int getTimerBurstIO() {
        return timerBurstIO;
    }

    public int getPriority() {
        return priority;
    }

    public int getRemainingBursts() {
        return remainingBursts;
    }

    public int getRemCPU() {
        return remCPU;
    }

    public int getCurPriority() {
        return curPriority;
    }

    public String getProcessName() {
        return processName;
    }

    public void setCantBurstCPU(int cantBurstCPU) {
        this.cantBurstCPU = cantBurstCPU;
    }

    public void setCurPriority(int curPriority) {
        this.curPriority = curPriority;
    }

    public void setRemCPU(int remCPU) {
        this.remCPU = remCPU;
    }

    public void setRemainingBursts(int remainingBursts) {
        this.remainingBursts = remainingBursts;
    }
}
