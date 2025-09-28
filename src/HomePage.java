import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HomePage extends JFrame{
    private JButton siguienteButton;
    private JPanel paginaPrincipal;
    private JButton aceptarButton;
    private JList listProcess;
    private List<Process> fields = null;
    private boolean aPolitica = false, cargado = false;

    // Guarda solo la ruta del archivo elegido (txt o rtf)
    private Path archivoSeleccionado;

    public HomePage(CountDownLatch latch) {
        setContentPane(paginaPrincipal);
        setSize(650,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Drag & Drop sobre el panel principal ---
        paginaPrincipal.setTransferHandler(new TransferHandler() {
            @Override public boolean canImport(TransferSupport s) {
                return s.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            @Override @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport s) {
                try {
                    Transferable t = s.getTransferable();
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (files == null || files.isEmpty()) return false;

                    File f = files.get(0);
                    if (!esTextoSoportado(f)) {
                        JOptionPane.showMessageDialog(paginaPrincipal,
                                "Elegí un .txt o .rtf",
                                "Formato no soportado", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    archivoSeleccionado = f.toPath();
                    setTitle("Archivo: " + archivoSeleccionado.toAbsolutePath());
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(paginaPrincipal, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });

        siguienteButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if(archivoSeleccionado == null){
                    JOptionPane.showMessageDialog(null,
                            "Ingrese un archivo antes de avanzar",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    if (!cargado) {
                        JOptionPane.showMessageDialog(null,
                                "Apriete aceptar antes de avanzar",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        aPolitica = true;
                        latch.countDown();
                        dispose();
                    }
                }
            }
        });

        aceptarButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (archivoSeleccionado == null) {
                    JOptionPane.showMessageDialog(paginaPrincipal,
                            "Arrastrá un archivo .txt o .rtf al panel primero.",
                            "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                try {
                    fields = ProcessLoader.load(archivoSeleccionado);

                    JOptionPane.showMessageDialog(paginaPrincipal,
                            "Cargados " + fields.size() + " procesos desde:\n" + archivoSeleccionado,
                            "OK", JOptionPane.INFORMATION_MESSAGE);
                    loadProcesos(); //Mostrar en pantalla los procesos
                    cargado = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(paginaPrincipal, ex.getMessage(), "Error al cargar", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadProcesos(){
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Process p : fields) {
            model.addElement(p.toString());
        }
        listProcess.setModel(model);
    }

    private boolean esTextoSoportado(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".rtf");
    }

    public List<Process> getField() {
        return fields;
    }

    public boolean getaPolitica() {
        return aPolitica;
    }
}
