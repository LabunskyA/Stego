package pw.stego.ui.controlls.combos;

import pw.stego.util.Patterns;

/**
 * Combobox with patterns types
 * Created by lina on 20.12.16.
 */
public class PatternComboBox extends FixedComboBox {
    private Patterns.Type pattern;

    public PatternComboBox() {
        super();
        
        addItem("Simple");
        addItem("ILCD");
        addItem("ILED");

        setSelectedItem("Simple");

        addActionListener((e) -> {
            if (getSelectedItem().equals("Simple"))
                pattern = Patterns.Type.SIMPLE;

            if (getSelectedItem().equals("ILCD"))
                pattern = Patterns.Type.ILCD;

            if (getSelectedItem().equals("ILED"))
                pattern = Patterns.Type.ILED;
        });
    }

    public Patterns.Type getChosedType() {
        return pattern;
    }
}
