package pw.stego.ui.controlls.combos;

import javax.swing.*;
import java.awt.*;

/**
 * Combobox with fixed size
 * Created by lina on 19.12.16.
 */
class FixedComboBox extends JComboBox<String> {
    FixedComboBox() {
        super();

        setPreferredSize(new Dimension(150, 30));
        setMaximumSize(new Dimension(150, 30));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
