import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ProcessLoader {

    public static List<Process> load(Path path) throws IOException {
        String text;
        try {
            text = readPlainText(path);
        } catch (BadLocationException e) {
            throw new IOException("No se pudo extraer texto del RTF: " + e.getMessage(), e);
        }

        List<Process> process = new ArrayList<>();
        String[] lines = text.split("\\R"); // separar texto por lineas

        int lineNo = 0;
        for (String rawLine : lines) {
            lineNo++;

            // quitar espacios
            String line = rawLine.replaceAll("//.*$", "")
                    .replaceAll("#.*$", "")
                    .trim();
            if (line.isEmpty()) continue;

            // quitar comas finales
            line = line.replaceAll(",\\s*$", "");

            // split por coma
            String[] parts = line.split(",");
            if (parts.length != 6) {
                System.err.println("Línea " + lineNo + ": se esperaban 6 campos y hay " + parts.length + " -> '" + rawLine + "'. Se omite.");
                continue;
            }

            try {
                String name         = parts[0].trim();
                int arrival         = Integer.parseInt(parts[1].trim());
                int cantBurstCPU    = Integer.parseInt(parts[2].trim());
                int timeBurstCPU    = Integer.parseInt(parts[3].trim());
                int timerBurstIO    = Integer.parseInt(parts[4].trim());
                int priority        = Integer.parseInt(parts[5].trim());

                Process p = new Process(name, arrival, cantBurstCPU, timeBurstCPU, timerBurstIO, priority);
                process.add(p);
            } catch (NumberFormatException nfe) {
                System.err.println("Línea " + lineNo + ": número inválido -> '" + rawLine + "'. Se omite. Detalle: " + nfe.getMessage());
            } catch (Exception ex) {
                System.err.println("Línea " + lineNo + ": error creando Process -> '" + rawLine + "'. Se omite. Detalle: " + ex.getMessage());
            }
        }
        return process;
    }

    private static String readPlainText(Path path) throws IOException, BadLocationException {
        String filename = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (filename.endsWith(".rtf")) {
            try (InputStream is = Files.newInputStream(path)) {
                RTFEditorKit rtf = new RTFEditorKit();
                Document doc = rtf.createDefaultDocument();
                rtf.read(is, doc, 0);
                return doc.getText(0, doc.getLength());
            }
        } else {
            // txt / csv / lo que sea texto
            return Files.readString(path, StandardCharsets.UTF_8);
        }
    }
}
