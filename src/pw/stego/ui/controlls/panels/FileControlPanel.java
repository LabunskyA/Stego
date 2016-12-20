package pw.stego.ui.controlls.panels;

import pw.stego.ui.controlls.buttons.FixedButton;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Panel with label, button and associated File
 * Created by lina on 20.12.16.
 */
public class FileControlPanel extends JPanel {
    private File file;

    public FileControlPanel(String label, JFrame placeholder) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Box.CENTER_ALIGNMENT);

        FixedButton labeledButton = new FixedButton(label);
        JLabel jLabel = new JLabel(label);
        jLabel.setAlignmentX(Box.CENTER_ALIGNMENT);

        add(jLabel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(labeledButton);

        labeledButton.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(placeholder) == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                jLabel.setText(file.getName());
            }
        });
    }

    File getChosedFile() {
        return file;
    }
}
