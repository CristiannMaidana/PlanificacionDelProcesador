import java.util.*;

public class RoundRobinPlanner extends SimulatorStrategy {

    final ArrayDeque<Process> ready = new ArrayDeque<>();

    RoundRobinPlanner(OperatingSystem os, List<Process> batch){
        super(os, batch);
    }

    @Override
    public void run(){
            while(!done() || soPhase!=SOPhase.NONE || running!=null || !ready.isEmpty() || !blocked.isEmpty() || !field.isEmpty() || !admissionBuffer.isEmpty() || !admittingNow.isEmpty()){

                while(!field.isEmpty() && field.peekFirst().getArrivalTime() == t){
                    admissionBuffer.add(field.removeFirst());
                }

                // CORRIENDO -> TERMINADO (TFP)
                if(burstJustFinished && running!=null && running.remainingBursts==1){
                    log("t=%d | CORRIENDO→TERMINADO: %s", t, running.getProcessName());
                    soPhase=SOPhase.TFP;
                    soBusy=OS.getTFP();
                    finishingProcess=running;
                    running=null;
                    burstJustFinished=false;
                    qRemaining=0;
                }
                // CORRIENDO -> BLOQUEADO
                else if(burstJustFinished && running!=null){
                    log("t=%d | CORRIENDO→BLOQUEADO: %s", t, running.getProcessName());
                    blocked.add(new BlockedItem(running, running.getTimerBurstIO()));
                    running.setRemainingBursts(running.getRemainingBursts() - 1);
                    running.setRemCPU(running.getTimerBurstCPU());
                    running=null;
                    burstJustFinished=false;
                    qRemaining=0;
                }

                // CORRIENDO -> LISTO (EXPROPIACIÓN por quantum)
                if(soPhase==SOPhase.NONE && running!=null && quantumExpired){
                    log("t=%d | CORRIENDO→LISTO (quantum agotado): %s", t, running.getProcessName());
                    // vuelve al final de la cola con el remanente de la MISMA ráfaga
                    ready.addLast(running);
                    running=null;
                    quantumExpired=false;

                    nextDispatch = ready.isEmpty()? null : ready.pollFirst();
                    if(nextDispatch!=null){
                        soPhase=SOPhase.TCP;
                        soBusy=OS.getTCP();
                    }
                }

                // BLOQUEADO -> LISTO
                if(!toReadyNextTick.isEmpty()){
                    for(Process p: toReadyNextTick){
                        log("t=%d | BLOQUEADO→LISTO: %s", t, p.getProcessName());
                        p.setRemCPU(p.getTimerBurstCPU());
                        ready.addLast(p);
                    }
                    toReadyNextTick.clear();
                }

                // NUEVO -> LISTO (TIP)
                if(tipJustFinished){
                    for(Process p: admittingNow){
                        log("t=%d | NUEVO→LISTO (tras TIP): %s", t, p.getProcessName());
                        p.remCPU = p.getTimerBurstCPU();
                        ready.addLast(p);
                    }
                    admittingNow.clear();
                    tipJustFinished=false;
                }
                if(soPhase==SOPhase.NONE && !admissionBuffer.isEmpty() && admittingNow.isEmpty()){
                    admittingNow.addAll(admissionBuffer);
                    admissionBuffer.clear();
                    soPhase=SOPhase.TIP;
                    soBusy=OS.getTIP();
                }

                // DESPACHO (TCP)
                if(tcpJustFinished){
                    if(nextDispatch!=null){
                        running=nextDispatch;
                        qRemaining = OS.getQuantum();
                        log("t=%d | LISTO→CORRIENDO: %s", t, running.getProcessName());
                        nextDispatch=null;
                    }
                    tcpJustFinished=false;
                }
                if(soPhase==SOPhase.NONE && running==null && !ready.isEmpty()){
                    nextDispatch = ready.pollFirst();
                    soPhase=SOPhase.TCP;
                    soBusy=OS.getTCP();
                }

                // 1 tick de trabajo
                if(soPhase!=SOPhase.NONE){
                    soBusy--;
                    M.cpuSO++;
                    if(soBusy==0){
                        if(soPhase==SOPhase.TIP)
                            tipJustFinished=true;
                        else if(soPhase==SOPhase.TFP)
                            tfpJustFinished=true;
                        else if(soPhase==SOPhase.TCP)
                            tcpJustFinished=true;
                        soPhase=SOPhase.NONE;
                    }
                }
                else if(running!=null){
                    // consumir 1 tick de CPU del proceso y del quantum
                    running.setRemCPU(running.getRemCPU()-1);
                    M.cpuProc++;
                    M.addCpuUsed(running);
                    if(qRemaining>0)
                        qRemaining--;
                    if(running.getRemCPU()==0){
                        burstJustFinished = true;
                    }
                    else if(qRemaining==0){
                        quantumExpired = true;
                    }
                }
                else {
                    M.cpuIdle++;
                }

                // Descontar I/O
                for(Iterator<BlockedItem> it=blocked.iterator(); it.hasNext();){
                    BlockedItem bi=it.next();
                    if(bi.ioRemaining>0)
                        bi.ioRemaining--;
                    if(bi.ioRemaining==0){
                        toReadyNextTick.add(bi.p);
                        it.remove();
                    }
                }

                M.addReadyWait(ready);

                // Fin TFP
                if(tfpJustFinished && finishingProcess!=null){
                    M.onFinish(finishingProcess, t+1);
                    finishingProcess=null;
                    tfpJustFinished=false;
                }

                t++;
            }

            M.printReport(LOG);
        }
}
