package pw.stego.ui;

import pw.stego.ui.controlls.combos.PatternComboBox;
import pw.stego.ui.controlls.panels.DecodePanel;
import pw.stego.ui.controlls.panels.EncodePanel;
import pw.stego.ui.controlls.panels.FileControlPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main window
 * Created by lina on 19.12.16.
 */
public class MainWindow extends JFrame {
    private MainWindow() {
        super("StegoTool");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        DecodePanel decode = new DecodePanel(
                new FileControlPanel("Key", this),
                new FileControlPanel("Container", this),
                this
        );

        EncodePanel encode = new EncodePanel(
                new FileControlPanel("Key", this),
                new FileControlPanel("Container", this),
                new FileControlPanel("Message", this),
                new PatternComboBox()
        );

        JTabbedPane pane = new JTabbedPane();
        pane.addTab("Encode", encode);
        pane.addTab("Decode", decode);

        setContentPane(pane);

        setSize(new Dimension(700, 100));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new MainWindow();
    }
}
