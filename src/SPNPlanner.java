import java.util.*;

public class SPNPlanner extends SimulatorStrategy{

    final PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.getTimerBurstCPU())
                    .thenComparingInt(p -> p.getArrivalTime())
    );

    SPNPlanner(OperatingSystem os, List<Process> batch){
        super(os, batch);
    }

    @Override
    public void run() {
        while (!done() || soPhase != SOPhase.NONE || running != null || !ready.isEmpty() || !blocked.isEmpty() || !field.isEmpty() || !admissionBuffer.isEmpty() || !admittingNow.isEmpty()) {

            while (!field.isEmpty() && field.peekFirst().getArrivalTime() == t) {
                admissionBuffer.add(field.removeFirst());
            }

            // Corriendo -> Terminado (TFP)
            if (burstJustFinished && running != null && running.remainingBursts == 1) {
                log("t=%d | CORRIENDO→TERMINADO: %s", t, running.getProcessName());
                // TFP
                soPhase = SOPhase.TFP;
                soBusy = OS.getTFP();
                finishingProcess = running;
                // limpio proceso de CPU
                running = null;
                cpuRemaining = 0;
                burstJustFinished = false;
            }
            // Corriendo -> Bloqueado (I/O)
            else if (burstJustFinished && running != null) {
                log("t=%d | CORRIENDO→BLOQUEADO: %s", t, running.getProcessName());
                blocked.add(new BlockedItem(running, running.getTimerBurstIO()));
                running.setRemainingBursts(running.getRemainingBursts() - 1);
                running = null;
                cpuRemaining = 0;
                burstJustFinished = false;
            }


            // Bloqueado -> Listo
            if (!toReadyNextTick.isEmpty()) {
                for (Process p : toReadyNextTick) {
                    log("t=%d | BLOQUEADO→LISTO: %s", t, p.getProcessName());
                    ready.add(p);
                }
                toReadyNextTick.clear();
            }

            // Nuevo->Listo  (TIP)
            if (tipJustFinished) {
                for (Process p : admittingNow) {
                    log("t=%d | NUEVO→LISTO (tras TIP): %s", t, p.getProcessName());
                    ready.add(p);
                }
                admittingNow.clear();
                tipJustFinished = false;
            }
            if (soPhase == SOPhase.NONE && !admissionBuffer.isEmpty() && admittingNow.isEmpty()) {
                // TIP
                admittingNow.addAll(admissionBuffer);
                admissionBuffer.clear();
                soPhase = SOPhase.TIP;
                soBusy = OS.getTIP();
            }

            // Despacho READY->CORRIENDO (TCP)
            if (tcpJustFinished) {
                // Fin TCP
                if (nextDispatch != null) {
                    running = nextDispatch;
                    cpuRemaining = running.getTimerBurstCPU();
                    log("t=%d | LISTO→CORRIENDO: %s", t, running.getProcessName());
                    nextDispatch = null;
                }
                tcpJustFinished = false;
            }
            if (soPhase == SOPhase.NONE && running == null && !ready.isEmpty()) {
                nextDispatch = ready.poll();
                soPhase = SOPhase.TCP;
                soBusy = OS.getTCP();
            }

            // Consumir 1 tick de trabajo
            if (soPhase != SOPhase.NONE) {
                soBusy--;
                M.cpuSO++;
                if (soBusy == 0) {
                    if (soPhase == SOPhase.TIP)
                        tipJustFinished = true;
                    else if (soPhase == SOPhase.TFP) {
                        tfpJustFinished = true;
                    }
                    else if (soPhase == SOPhase.TCP)
                        tcpJustFinished = true;
                    soPhase = SOPhase.NONE;
                }
            }
            else if (running != null) {
                cpuRemaining--;
                M.cpuProc++;
                M.addCpuUsed(running);
                if (cpuRemaining == 0) {
                    burstJustFinished = true;
                }
            }
            else {
                M.cpuIdle++;
            }

            // Descontar I/O
            for (Iterator<BlockedItem> it = blocked.iterator(); it.hasNext(); ) {
                BlockedItem bi = it.next();
                if (bi.ioRemaining > 0)
                    bi.ioRemaining--;
                if (bi.ioRemaining == 0) {
                    toReadyNextTick.add(bi.p);
                    it.remove();
                }
            }

            M.addReadyWait(ready);

            // Cerrar TFP
            if (tfpJustFinished && finishingProcess != null) {
                M.onFinish(finishingProcess, t + 1);
                finishingProcess = null;
                tfpJustFinished = false;
            }
            
            t++;
        }

        M.printReport(LOG);
    }
}