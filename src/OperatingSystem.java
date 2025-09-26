public class OperatingSystem {
    int timeNewProcess, timeEndProcess, timeSwitchProcess, Quantum;

    OperatingSystem(int timeNewProcess, int timeEndProcess, int timeSwitchProcess, int Quantum) {
        this.timeNewProcess = timeNewProcess;
        this.timeEndProcess = timeEndProcess;
        this.timeSwitchProcess = timeSwitchProcess;
        this.Quantum = Quantum;
    }

    public int getTIP(){
        return timeNewProcess;
    }

    public int getTFP(){
        return timeEndProcess;
    }

    public int getTCP(){
        return timeSwitchProcess;
    }

    public int getQuantum(){
        return Quantum;
    }
}
