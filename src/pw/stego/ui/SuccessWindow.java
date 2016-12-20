package pw.stego.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Appears on successfull operation
 * Created by lina on 20.12.16.
 */
public class SuccessWindow extends JFrame{
    public SuccessWindow(String message) {
        super("Success!");

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setResizable(false);
        setPreferredSize(new Dimension(300, 70));

        JButton ok = new JButton("Ok");
        JLabel label = new JLabel(message);

        ok.addActionListener((e) -> this.dispose());
        ok.setPreferredSize(new Dimension(30, 30));

        ok.setAlignmentX(Box.CENTER_ALIGNMENT);
        label.setAlignmentX(Box.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(label);
        add(Box.createVerticalGlue());
        add(ok);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
