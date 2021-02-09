/*
Java Power Info utility, (C)2021 IC Book Labs
Universal parametrized view panel, abstract class
*/

package powerinfo;

import javax.swing.*;

abstract public class ViewPanel 
    {
    abstract public JPanel getP();
    abstract public JTable getTable();
    abstract public JComboBox getCombo();
    abstract public JButton getClearButton();
    abstract public JButton getRunButton();
    }
