import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Metrics {
    Map<String,Integer> arrival = new LinkedHashMap<>();
    Map<String,Integer> finish  = new LinkedHashMap<>();
    Map<String,Integer> readyWait = new LinkedHashMap<>();
    Map<String,Integer> cpuUsed   = new LinkedHashMap<>();
    List<String> eventos = new ArrayList<>();
    List<String> mProcess = new ArrayList<>();

    int firstArrival = Integer.MAX_VALUE;
    int lastFinishTFP = 0;

    int cpuIdle = 0;  // ticks
    int cpuSO   = 0;  // TIP+TCP+TFP
    int cpuProc = 0;  // procesos

    String CPUProc, CPUSO, CPUIdle, TRtS, TMRtS;

    public void onCreate(Process p) {
        arrival.put(p.getProcessName(), p.getArrivalTime());
        readyWait.put(p.getProcessName(), 0);
        cpuUsed.put(p.getProcessName(), 0);
        firstArrival = Math.min(firstArrival, p.getArrivalTime());
    }

    public void addReadyWait(Collection<Process> procs) {
        for (Process p : procs)
            readyWait.merge(p.getProcessName(), 1, Integer::sum);
    }

    public void addCpuUsed(Process p) {
        cpuUsed.merge(p.getProcessName(), 1, Integer::sum);
    }

    public void onFinish(Process p, int t) {
        finish.put(p.getProcessName(), t);
        lastFinishTFP = Math.max(lastFinishTFP, t);
    }

    public void printReport(List<String> log) {
        System.out.println("==== LOG DE EVENTOS ====");
        for (String s : log) {
            System.out.println(s);
            String line = String.format(s);
            eventos.add(line);
        }

        System.out.println();

        System.out.println("==== MÉTRICAS POR PROCESO ====");
        System.out.printf("%-6s %8s %8s %10s\n", "Proc", "TRp", "TRn", "ReadyWait");
        double sumTRp = 0;
        for (String name : arrival.keySet()) {
            int TRp = finish.get(name) - arrival.get(name);
            int used = cpuUsed.get(name);
            double TRn = used > 0 ? (double)TRp / used : 0.0;
            sumTRp += TRp;

            System.out.printf("%-6s %8d %8.2f %10d\n", name, TRp, TRn, readyWait.get(name));

            String line = String.format("%-6s %8d %8.2f %10d", name, TRp, TRn, readyWait.getOrDefault(name, 0));
            mProcess.add(line);
        }
        System.out.println();

        System.out.println("==== MÉTRICAS DE TANDA ====");
        int n = arrival.size();
        double TMRt = n > 0 ? sumTRp / n : 0.0;
        int TRt = lastFinishTFP - firstArrival;
        System.out.printf("TRt = %d\n", TRt);
        TRtS = String.format("TRt = %d", TRt);

        System.out.printf("TMRt = %.2f\n", TMRt);
        TMRtS = String.format("TMRt = %.2f\n", TMRt);

        System.out.println();

        System.out.println("==== USO DE CPU (ticks y % sobre TRt) ====");
        int total = cpuIdle + cpuSO + cpuProc;
        if (total != TRt) {
            System.out.printf("(Nota: total=%d != TRt=%d; esto puede pasar si arrancás t≠firstArrival)\n", total, TRt);
        }
        double f = TRt > 0 ? 100.0 / TRt : 0.0;
        System.out.printf("CPU Proc = %d (%.2f%%)\n", cpuProc, cpuProc * f);
        CPUProc = String.format("CPU Proc = %d (%.2f%%)\n", cpuProc, cpuProc * f);
        System.out.printf("CPU SO   = %d (%.2f%%)\n", cpuSO,   cpuSO   * f);
        CPUSO = String.format("CPU SO   = %d (%.2f%%)\n", cpuSO,   cpuSO   * f);
        System.out.printf("CPU Idle = %d (%.2f%%)\n", cpuIdle, cpuIdle * f);
        CPUIdle = String.format("CPU Idle = %d (%.2f%%)\n", cpuIdle, cpuIdle * f);
    }

    public List<String> getEventos() {
        return eventos;
    }

    public List<String> getMProcess() {
        return mProcess;
    }

    public String getTRT() {
        return TRtS;
    }

    public String getTMRt() {
        return TMRtS;
    }

    public String getCPUProc() {
        return CPUProc;
    }

    public String getCPUSO() {
        return CPUSO;
    }

    public String getCPUIdle() {
        return CPUIdle;
    }

    public void exportarReportesEscritorio() {
        try {
            String report = buildReportTextDesdeCampos();
            String base = "reporte_" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            Path desktop = getDesktopPath();
            Files.createDirectories(desktop);

            // TXT (UTF-8)
            Files.writeString(desktop.resolve(base + ".txt"), report, StandardCharsets.UTF_8);

            // RTF
            StyledDocument doc = new DefaultStyledDocument();
            doc.insertString(0, report, null);
            try (OutputStream os = Files.newOutputStream(desktop.resolve(base + ".rtf"))) {
                new RTFEditorKit().write(os, doc, 0, doc.getLength());
            }

            System.out.println("Reportes generados en: " + desktop.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("No se pudieron generar los reportes en el Escritorio: " + e.getMessage());
        }
    }

    private String buildReportTextDesdeCampos() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        sb.append("==== LOG DE EVENTOS ====").append(nl);
        for (String s : getEventos()) sb.append(s).append(nl);
        sb.append(nl);

        sb.append("==== MÉTRICAS POR PROCESO ====").append(nl);
        sb.append(String.format("%-6s %8s %8s %10s%n", "Proc", "TRp", "TRn", "ReadyWait"));

        double sumTRp = 0.0;
        for (String name : arrival.keySet()) {
            int TRp  = finish.getOrDefault(name, 0) - arrival.getOrDefault(name, 0);
            int used = cpuUsed.getOrDefault(name, 0);
            double TRn = used > 0 ? (double) TRp / used : 0.0;
            sumTRp += TRp;

            sb.append(String.format("%-6s %8d %8.2f %10d%n",
                    name, TRp, TRn, readyWait.getOrDefault(name, 0)));
        }
        sb.append(nl);

        int n = arrival.size();
        double TMRt = n > 0 ? sumTRp / n : 0.0;
        int TRt = lastFinishTFP - firstArrival;

        sb.append("==== MÉTRICAS DE TANDA ====").append(nl);
        sb.append(String.format("TRt = %d%n", TRt));
        sb.append(String.format("TMRt = %.2f%n", TMRt));
        sb.append(nl);

        int total = cpuIdle + cpuSO + cpuProc;
        sb.append("==== USO DE CPU (ticks y % sobre TRt) ====").append(nl);
        double f = TRt > 0 ? 100.0 / TRt : 0.0;
        sb.append(String.format("CPU Proc = %d (%.2f%%)%n", cpuProc, cpuProc * f));
        sb.append(String.format("CPU SO   = %d (%.2f%%)%n",  cpuSO,  cpuSO  * f));
        sb.append(String.format("CPU Idle = %d (%.2f%%)%n", cpuIdle, cpuIdle * f));

        return sb.toString();
    }

    private Path getDesktopPath() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File homeOrDesktop = fsv.getHomeDirectory();
        Path p = homeOrDesktop.toPath();

        if (p.getFileName() != null && p.getFileName().toString().equalsIgnoreCase("Desktop"))
            return p;

        Path desktop = p.resolve("Desktop");
        if (Files.isDirectory(desktop)) return desktop;

        Path escritorio = p.resolve("Escritorio");
        if (Files.isDirectory(escritorio)) return escritorio;

        return p;
    }
}
