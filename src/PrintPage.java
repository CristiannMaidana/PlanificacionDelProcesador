import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

public class PrintPage extends JFrame {
    private JButton eventosButton;
    private JButton indicadoresButton;
    private JButton finalizarButton;
    private JPanel PrintPage;
    private JList listResults;
    private JButton crearArchivoButton;
    private JButton repetirButton;
    private JLabel JLabelResults;
    private Metrics metrics;
    private Boolean again = false, finalizar = false;

    public PrintPage(CountDownLatch latch, Metrics m, String nombre) {
        setContentPane(PrintPage);
        setSize(650,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabelResults.setText("Resultados de: " + nombre);
        this.metrics = m;

        eventosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadEventos();
            }
        });
        indicadoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMProcess();
            }
        });
        finalizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizar = true;
                latch.countDown();
                dispose();
            }
        });
        crearArchivoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                metrics.exportarReportesEscritorio();
                JOptionPane.showMessageDialog(null, "Reporte finalizado con exito!",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        repetirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int respuesta = JOptionPane.showConfirmDialog(null,
                        "¿Volver a elegir planificador con los mismo datos?");
                if (respuesta == JOptionPane.YES_OPTION) {
                    again = true;
                    latch.countDown();
                    dispose();
                }
                else {
                    again = false;
                    latch.countDown();
                    dispose();
                }
            }
        });
    }

    private void loadEventos(){
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("EVENTOS");
        for (String s : metrics.getEventos())
            model.addElement(s);
        listResults.setModel(model);
    }

    private void loadMProcess(){
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("MÉTRICAS POR PROCESO");
        String line = String.format("%-6s %8s %8s %10s\n", "Proc", "TRp", "TRn", "ReadyWait");
        model.addElement(line);
        for (String s : metrics.getMProcess())
            model.addElement(s);
        model.addElement("  ");
        model.addElement("MÉTRICAS DE TANDA");
        model.addElement(metrics.getTRT());
        model.addElement(metrics.getTMRt());
        model.addElement("  ");
        model.addElement("USO DE CPU (ticks y % sobre TRt)");
        model.addElement(metrics.getCPUProc());
        model.addElement(metrics.getCPUSO());
        model.addElement(metrics.getCPUIdle());
        listResults.setModel(model);
    }

    public boolean getAgain(){
        return again;
    }

    public boolean getFinalizar(){
        return finalizar;
    }
}
