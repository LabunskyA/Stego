package pw.stego.ui.controlls.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Panel with automated rigids
 * Created by lina on 20.12.16.
 */
class ToolPanel extends JPanel {
    ToolPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    Component addWithRigid(Component comp) {
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(comp);

        return comp;
    }
}
