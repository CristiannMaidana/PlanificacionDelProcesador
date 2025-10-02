import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.concurrent.CountDownLatch;

public class PlannerPage extends JFrame {
    private JPanel plannerPage;
    private JComboBox comboBoxPlanner;
    private JTextField textFieldTIP;
    private JButton aceptarButton;
    private JButton siguienteButton;
    private JTextField textFieldTCP;
    private JTextField textFieldTFP;
    private JTextField textFieldQuantum;
    private String plannerName;
    private int tip, tcp, tfp, quantum;
    private boolean completaCarga = false;

    public PlannerPage(CountDownLatch latch) {
        setContentPane(plannerPage);
        setSize(650,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        disableTextField();

        comboBoxPlanner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plannerName = (String) comboBoxPlanner.getSelectedItem();
                //Mostrar alerta de el planificador que eligio
                if(plannerName==null || plannerName.equals("")){
                    JOptionPane.showMessageDialog(null, "Debe seleccionar un planner para continuar");
                }
                else{
                    JOptionPane.showMessageDialog(null, "La planner especificada es: " + plannerName);
                }
            }
        });
        aceptarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(plannerName==null || plannerName.equals("")){
                    JOptionPane.showMessageDialog(null,
                            "Elegí un planificador antes de continuar",
                            "Alerta", JOptionPane.WARNING_MESSAGE);
                }
                else{
                    JOptionPane.showMessageDialog(null,"Cargue tiempos de SO.");
                    textFieldTCP.setEnabled(true);
                    textFieldTFP.setEnabled(true);
                    textFieldTIP.setEnabled(true);
                    textFieldQuantum.setEnabled(true);
                }
            }
        });
        textFieldTIP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(textFieldTIP.isEnabled() && !textFieldTIP.getText().isEmpty()){
                    tip = Integer.parseInt(textFieldTIP.getText());
                }
            }
        });
        textFieldTCP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(textFieldTCP.isEnabled() && !textFieldTCP.getText().isEmpty()){
                    tcp = Integer.parseInt(textFieldTCP.getText());
                }
            }
        });
        textFieldTFP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(textFieldTFP.isEnabled() && !textFieldTFP.getText().isEmpty()){
                    tfp = Integer.parseInt(textFieldTFP.getText());
                }
            }
        });
        textFieldQuantum.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(textFieldQuantum.isEnabled() && !textFieldQuantum.getText().isEmpty()){
                    quantum = Integer.parseInt(textFieldQuantum.getText());
                }
            }
        });
        siguienteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldTCP.getText().isEmpty() || textFieldTFP.getText().isEmpty() || textFieldQuantum.getText().isEmpty() ) {
                    JOptionPane.showMessageDialog(null,
                            "Debe agregar un tiempo a todos los datos para continuar",
                            "Alerta",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
                else{
                    System.out.println(textFieldTIP.getText()+textFieldTFP.getText()+textFieldQuantum.getText()+textFieldTCP.getText());
                    System.out.println("Fallo");
                    completaCarga = true;
                    latch.countDown();
                    dispose();
                }
            }
        });
    }

    private void disableTextField(){
        textFieldTCP.setEnabled(false);
        textFieldTFP.setEnabled(false);
        textFieldTIP.setEnabled(false);
        textFieldQuantum.setEnabled(false);
    }

    public String getPlannerName() {
        return plannerName;
    }

    public int getTip() {
        return tip;
    }

    public int getTcp() {
        return tcp;
    }

    public int getTFP() {
        return tfp;
    }

    public int getQuantum() {
        return quantum;
    }

    public boolean getIsCompletaCarga() {
        return completaCarga;
    }
}
