package pw.stego.ui.controlls.panels;

import pw.Stego;
import pw.stego.ui.SuccessWindow;
import pw.stego.ui.controlls.buttons.FixedButton;
import pw.stego.ui.controlls.combos.PatternComboBox;

import javax.swing.*;
import java.io.IOException;

/**
 * Encode tab panel
 * Created by lina on 20.12.16.
 */
public class EncodePanel extends ToolPanel{

    public EncodePanel(FileControlPanel key, FileControlPanel container, FileControlPanel message, PatternComboBox pattern) {
        super();

        addWithRigid(key);
        addWithRigid(container);
        addWithRigid(message);
        addWithRigid(pattern);

        JButton encodeButton = new FixedButton("Encode");
        encodeButton.addActionListener((e) -> {
            try {
                Stego.encode(
                        key.getChosedFile(),
                        message.getChosedFile(),
                        pattern.getChosedType(),
                        container.getChosedFile()
                );

                new SuccessWindow("Successfuly inserted into " + container.getName());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        addWithRigid(Box.createVerticalGlue());
        addWithRigid(encodeButton);
    }
}
