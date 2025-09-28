import java.util.*;

public abstract class SimulatorStrategy {

    //Estados
    protected enum SOPhase { NONE, TIP, TCP, TFP }

    //Clases de datos
    protected final OperatingSystem OS;
    protected final Metrics M = new Metrics();
    protected final List<String> LOG = new ArrayList<>();

    //Variables
    protected int t=0, soBusy=0, cpuRemaining=0;
    protected SOPhase soPhase= SOPhase.NONE;
    protected Process running=null;
    protected int qRemaining=0; // quantum restante del que corre

    //Cola de procesos
    protected final Deque<Process> field = new ArrayDeque<>();
    protected final List<Process> admissionBuffer = new ArrayList<>();
    protected final List<Process> admittingNow = new ArrayList<>();

    protected final List<BlockedItem> blocked = new ArrayList<>();
    protected final List<Process> toReadyNextTick = new ArrayList<>();

    // Señales
    protected boolean burstJustFinished=false, tcpJustFinished=false, tipJustFinished=false, tfpJustFinished=false;
    protected boolean wantPreempt=false;
    protected boolean quantumExpired=false; // expropiación por quantum
    protected Process finishingProcess=null, nextDispatch=null;

    SimulatorStrategy(OperatingSystem os, List<Process> batch){
        this.OS = os;
        batch.sort(Comparator.comparingInt((Process p)->p.getArrivalTime()));
        for(Process p: batch){
            field.addLast(p);
            M.onCreate(p);
        }
    }

    protected abstract void run();

    protected void log(String fmt, Object... args){
        LOG.add(String.format(fmt,args));
    }

    protected boolean done(){
        return M.finish.size()==M.arrival.size();
    }

    public Metrics getMetrics() {
        return M;
    }
}
