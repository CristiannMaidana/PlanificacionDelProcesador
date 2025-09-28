import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static CountDownLatch latch;
    private static PlannerPage paginaPlanificador;
    private static PrintPage paginaFinal;
    private static SimulatorStrategy simulador;
    private static boolean repetir = true, finalizar = false;

    private static void inicializoSemaforo(){
        latch = new CountDownLatch(1);
    }

    // ===================== Ejemplo de uso =====================
    public static void main(String[] args) throws InterruptedException {
        OperatingSystem OS;
        List<Process> processField = new ArrayList<>();
        List<Process> copia = new ArrayList<>(processField.size());

        while(!finalizar && repetir) {
            repetir = false;
            inicializoSemaforo();
            HomePage paginaPrincipal = new HomePage(latch);
            paginaPrincipal.setVisible(true);
            paginaPrincipal.setLocationRelativeTo(null);
            latch.await();

            while (!repetir) {
                if (paginaPrincipal.getaPolitica()) {
                    processField = paginaPrincipal.getField();

                    //Copiar archivo para repetidos datos
                    if (copia.isEmpty()) {
                        for (Process p : processField) {
                            Process c = new Process(p.getProcessName(), p.getArrivalTime(),
                                    p.getCantBurstCPU(), p.getTimerBurstCPU(),
                                    p.getTimerBurstIO(), p.getPriority());
                            copia.add(c);
                        }
                    }

                    inicializoSemaforo();
                    paginaPlanificador = new PlannerPage(latch);
                    paginaPlanificador.setVisible(true);
                    paginaPlanificador.setLocationRelativeTo(null);
                    latch.await();
                }
                if (paginaPlanificador.getIsCompletaCarga()) {
                    OS = new OperatingSystem(paginaPlanificador.getTip(), paginaPlanificador.getTFP(),
                            paginaPlanificador.getTcp(), paginaPlanificador.getQuantum());
                    switch (paginaPlanificador.getPlannerName()) {
                        case "FCFS (First Come First Served).": {
                            simulador = new FCFSPlanner(OS, copia);
                            break;
                        }
                        case "Round-Robin.": {
                            simulador = new RoundRobinPlanner(OS, copia);
                            break;
                        }
                        case "SPN (Shortest Process Next).": {
                            simulador = new SPNPlanner(OS, copia);
                            break;
                        }
                        case "Prioridad Externa.": {
                            simulador = new PriorityExternalPlanner(OS, copia, true, 1, false);
                            break;
                        }
                        case "SRTN (Shortest Remaining Time Next).": {
                            simulador = new SRTNPlanner(OS, copia);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    simulador.run();
                    inicializoSemaforo();
                    paginaFinal = new PrintPage(latch, simulador.getMetrics(), paginaPlanificador.getPlannerName(), processField);
                    paginaFinal.setVisible(true);
                    paginaFinal.setLocationRelativeTo(null);
                    latch.await();
                    repetir = !paginaFinal.getAgain();
                    finalizar = paginaFinal.getFinalizar();
                    copia.clear();
                }
            }
        }
    }
}