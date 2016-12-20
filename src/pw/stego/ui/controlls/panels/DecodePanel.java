package pw.stego.ui.controlls.panels;

import pw.Stego;
import pw.stego.ui.SuccessWindow;
import pw.stego.ui.controlls.buttons.FixedButton;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Decode tab panel
 * Created by lina on 20.12.16.
 */
public class DecodePanel extends ToolPanel {
    public DecodePanel(FileControlPanel choseKeyD, FileControlPanel choseContainerD, JFrame placeholder) {
        addWithRigid(choseKeyD);
        addWithRigid(choseContainerD);

        JButton decodeButton = new FixedButton("Decode");

        decodeButton.addActionListener((e) -> {
            try {
                JFileChooser saver = new JFileChooser();
                if (saver.showSaveDialog(placeholder) == JFileChooser.APPROVE_OPTION)
                    Files.write(
                            saver.getSelectedFile().toPath(),
                            Stego.decode(choseKeyD.getChosedFile(), choseContainerD.getChosedFile())
                    );

                new SuccessWindow("Successfuly decoded to " + saver.getSelectedFile().getName());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        addWithRigid(Box.createVerticalGlue());
        addWithRigid(decodeButton);
    }
}
