package pw.stego.ui.controlls.buttons;

import javax.swing.*;
import java.awt.*;

/**
 * Button with fixed size
 * Created by lina on 19.12.16.
 */
public class FixedButton extends JButton {
    public FixedButton(String label) {
        super(label);

        setPreferredSize(new Dimension(150, 30));
        setMaximumSize(new Dimension(150, 30));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
