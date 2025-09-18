import java.util.LinkedList;
import java.util.Queue;

public class planificadorCola {
    static int tiempoCPUProceso, tiempoCPUDesocupada, tiempoCPUSO;
    static OperatingSystem OS = new OperatingSystem(4, 6, 3, 5);


    public static void main(String[] args) {
        Queue<Process> processField = new LinkedList<>();
        Queue<Process> processReady = new LinkedList<>();

        CPU CPU = new CPU(0,0,0);
        cargoCola(processField);
        FirstComeFirstServed planificador = new FirstComeFirstServed();

        OS.setPlanner(planificador);

        ejecutar(processField, processReady);

        imprimir();
    }

    public static void ejecutar(Queue<Process> processField, Queue<Process> processReady) {
        boolean fin = false;
        int tiempo = 0;
        Process process, pBlocked = null;

        while (!fin) {
            if (tiempo == (processField.peek() != null ? processField.peek().arrivalTime : Integer.MIN_VALUE)){
                if (processReady.isEmpty()) {
                    tiempoCPUDesocupada += tiempo;
                }
                process = processField.poll();
                TIP(process, processReady);
                tiempo = actualizoTiempo(tiempo, OS.getTimeNewProcess());
            }

            if (!processReady.isEmpty()) {
                pBlocked = ejecutarPlanificador(processReady);

                //Actualizar el reloj con el tiempo de rafagaCPU
                tiempo = actualizoTiempo(tiempo, pBlocked.getTimerBurstCPU());
            }

            //Revisar tiempo de arrivo, con tiempo actualizado
            tiempo = procesosDespierto(tiempo, processField, processReady);

            //Si no hay procesos esperando y hace rafaga de IO:
            if (processReady.isEmpty()) {
                tiempoCPUDesocupada += pBlocked.getTimerBurstIO();
            }

            if (pBlocked != null) {
                if (pBlocked.getCantBurstCPU() > 0){
                    TCP(pBlocked, processReady);
                    tiempo = actualizoTiempo(tiempo, OS.getTimeSwitchProcess());
                }
                else {
                    TFP(pBlocked);
                    tiempo = actualizoTiempo(tiempo, OS.getTimeEndProcess());
                }
            }

            if (processReady.isEmpty() && processField.isEmpty()) {
                fin = true;
            }
            tiempo++;
        }
    }

    public static int procesosDespierto(int tiempo, Queue<Process> processField, Queue<Process> processReady) {
        //Verifica si necesita despertar procesos, actualizando los tiempos
        Process next = processField.peek();
        while (next != null && tiempo >= next.arrivalTime) {
            Process p = processField.poll();
            TIP(p, processReady);
            tiempo = actualizoTiempo(tiempo, OS.getTimeNewProcess());
            next = processField.peek();
        }
        return tiempo;
    }

    public static void imprimir(){
        System.out.println("El tiempo de uso del CPU en el Procesedor es: "+ tiempoCPUProceso);
        System.out.println("El tiempo de uso del CPU en el SO es: "+tiempoCPUSO);
        System.out.println("El tiempo de CPU desocupada es: "+tiempoCPUDesocupada);
    }

    public static int actualizoTiempo(int tiempo, int incremento) {
        return tiempo + incremento;
    }

    public static Process ejecutarPlanificador(Queue<Process> colaPlanificador) {
        Process pReady;
        pReady = colaPlanificador.poll();
        System.out.println("Evento: LISTO a CORRIENDO, proceso: "+ pReady.processName+"(PLANIFICADOR)");
        int burstCPU = pReady.getCantBurstCPU();
        pReady.setCantBurstCPU(burstCPU-1);

        if (pReady.getCantBurstCPU() > 0)
            System.out.println("Evento: CORRIENDO a BLOQUEADO, proceso: "+ pReady.processName+"(PLANIFICADOR)");
        else
            System.out.println("Evento: CORRIENDO a TERMINADO, proceso: "+pReady.processName+"(TFP)");

        tiempoCPUProceso += pReady.getTimerBurstCPU();
        return pReady;
    }

    public static void TFP(Process process){
        tiempoCPUSO += OS.getTimeEndProcess();
//        System.out.println("Evento: CORRIENDO a TERMINADO, proceso: "+process.processName+"(TFP)");
    }

    public static void TCP(Process process, Queue<Process> processReady) {
        tiempoCPUSO += OS.getTimeSwitchProcess();
        processReady.add(process);

        if (process.getCantBurstCPU() > 0)
            System.out.println("Evento: BLOQUEADO a LISTO, proceso: "+ process.processName+"(TCP)");

    }

    public static void TIP(Process process, Queue<Process> processReady) {
        tiempoCPUSO += OS.getTimeNewProcess();
        System.out.println("Evento: NUEVO a LISTO, proceso: "+process.processName+"(TIP)");
        processReady.add(process);
    }

    public static Queue<Process> cargoCola(Queue<Process> processQueue) {
        Process p1 = new Process("P1", 0, 3, 5, 2, 10);
        Process p2 = new Process("P2", 19, 5, 2, 4, 40);
        Process p3 = new Process("P3", 23, 2, 6, 9, 60);
        Process p4 = new Process("P4", 38, 7, 1, 7, 20);
        Process p5 = new Process("P5", 44, 1, 6, 3, 70);

        processQueue.add(p1);
        processQueue.add(p2);
        processQueue.add(p3);
        processQueue.add(p4);
        processQueue.add(p5);
        return processQueue;
    }
}
