public class OperatingSystem {
    int timeNewProcess, timeEndProcess, timeSwitchProcess, Quantum;

    OperatingSystem(int timeNewProcess, int timeEndProcess, int timeSwitchProcess, int Quantum) {
        this.timeNewProcess = timeNewProcess;
        this.timeEndProcess = timeEndProcess;
        this.timeSwitchProcess = timeSwitchProcess;
        this.Quantum = Quantum;
    }
}
