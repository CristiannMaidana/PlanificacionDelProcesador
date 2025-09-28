import java.util.*;

public class PriorityExternalPlanner extends SimulatorStrategy {

    // Config de política
    final boolean useAging;       // true: disminuir prioridad del running cada tick
    final int agingStep;          // cuánto se reduce por tick (>=1)
    final boolean useQuantum;     // true: aplicar quantum (máximo de ticks continuos)

    final PriorityQueue<Process> ready = new PriorityQueue<>(
            Comparator.<Process>comparingInt(p->-p.getCurPriority()) // mayor primero
                    .thenComparingInt(p->p.getArrivalTime()));


    PriorityExternalPlanner(OperatingSystem os, List<Process> batch, boolean useAging, int agingStep, boolean useQuantum){
        super(os, batch);
        this.useAging=useAging;
        this.agingStep=Math.max(1,agingStep);
        this.useQuantum=useQuantum;
    }

    @Override
    public void run(){
        while(!done() || soPhase!=SOPhase.NONE || running!=null || !ready.isEmpty() || !blocked.isEmpty() || !field.isEmpty() || !admissionBuffer.isEmpty() || !admittingNow.isEmpty()){

            while(!field.isEmpty() && field.peekFirst().getArrivalTime() == t){
                admissionBuffer.add(field.removeFirst());
            }

            // CORRIENDO -> TERMINADO (TFP)
            if(burstJustFinished && running!=null && running.getRemainingBursts()==1){
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
                running.setRemainingBursts(running.getRemainingBursts()-1);
                running.setRemCPU(running.getTimerBurstCPU());
                running.setCurPriority(running.getPriority());
                running=null;
                burstJustFinished=false;
                qRemaining=0;
            }

            // CORRIENDO -> LISTO (EXPROPIACIÓN)
            if(soPhase==SOPhase.NONE && running!=null && (wantPreempt || (useQuantum && quantumExpired))){
                String why = wantPreempt? "expropiación por prioridad" : "quantum agotado";
                log("t=%d | CORRIENDO→LISTO (%s): %s", t, why, running.getProcessName());
                ready.add(running);
                running=null;
                wantPreempt=false;
                quantumExpired=false;
                nextDispatch = ready.poll();
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
                    p.setCurPriority(p.getPriority());
                    ready.add(p);
                }
                toReadyNextTick.clear();
            }

            // NUEVO -> LISTO (TIP)
            if(tipJustFinished){
                for(Process p: admittingNow){
                    log("t=%d | NUEVO→LISTO (tras TIP): %s", t, p.getProcessName());
                    p.setRemCPU(p.getTimerBurstCPU());
                    p.setCurPriority(p.getPriority());
                    ready.add(p);
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

            // (TCP)
            if(tcpJustFinished){
                if(nextDispatch!=null){
                    running=nextDispatch;
                    qRemaining = useQuantum? OS.getQuantum() : Integer.MAX_VALUE;
                    log("t=%d | LISTO→CORRIENDO: %s (prio=%d)", t, running.getProcessName(), running.getCurPriority());
                    nextDispatch=null;
                }
                tcpJustFinished=false;
            }
            if(soPhase==SOPhase.NONE && running==null && !ready.isEmpty()){
                nextDispatch = ready.poll();
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
                running.setRemCPU(running.getRemCPU()-1);
                M.cpuProc++;
                M.addCpuUsed(running);
                if(useQuantum && qRemaining>0)
                    qRemaining--;
                if(useAging){
                    running.setCurPriority(Math.max(Integer.MIN_VALUE/4, running.getCurPriority() - agingStep));
                }
                if(running.remCPU==0){
                    burstJustFinished=true;
                }
                else if(useQuantum && qRemaining==0){
                    quantumExpired=true;
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

            if(soPhase==SOPhase.NONE && running!=null && !ready.isEmpty()){
                Process best = ready.peek();
                if(best!=null && best.getCurPriority() > running.getCurPriority()){
                    wantPreempt = true;
                } else {
                    wantPreempt = false;
                }
            }
            else {
                wantPreempt = false;
            }

            t++;
        }

        M.printReport(LOG);
    }
}

