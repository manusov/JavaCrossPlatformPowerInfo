/*
Java Power Info utility, (C)2018 IC Book Labs
View Panel for table with combo box
*/

package powerinfo;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class ViewerTableCombo extends ViewPanel 
{
private final JPanel p;                  // complex  panel
private final JComboBox combo;           // combo box
private final JTable table;              // tables
private final AbstractTableModel atm;    // tables models
private final ComboChanger cch;          // trees exps.

public ViewerTableCombo
    ( int x, int y, 
      String z1, ComboChanger z2, AbstractTableModel z3 )
    {
    cch = z2; atm = z3;
    p = new JPanel();
    // Combo with listener
    combo = new JComboBox();
    combo.setMaximumSize( new Dimension(x,28) );  // REPLACE TO SPRING LAYOUT
    combo.addActionListener(cch);
    JLabel l1 = new JLabel(z1);
    // Table with scroll panel
    table = new JTable(atm);
    JScrollPane sp = new JScrollPane(table);
    // Built layout with subpanels
    JPanel phc1 = new JPanel();
    JPanel pvc1 = new JPanel();
    JPanel pt2 = new JPanel();
    BoxLayout s1x1 = new BoxLayout (phc1, BoxLayout.X_AXIS);
    BoxLayout s1y1 = new BoxLayout (pvc1, BoxLayout.Y_AXIS);
    BoxLayout s1y2 = new BoxLayout (p, BoxLayout.Y_AXIS);
    GridLayout s1grid = new GridLayout();
    phc1.setLayout(s1x1);
    pvc1.setLayout(s1y1);
    pt2.setLayout(s1grid);
    p.setLayout(s1y2);
    // Built panel with prepared components and layout
    phc1.add(Box.createHorizontalStrut(4));
    phc1.add(l1);
    phc1.add(Box.createHorizontalStrut(6));
    phc1.add(combo);
    phc1.add(Box.createHorizontalStrut(3));
    pvc1.add(Box.createVerticalStrut(7));
    pvc1.add(phc1);
    pvc1.add(Box.createVerticalStrut(7));
    pt2.add(sp);
    p.add(pvc1);
    p.add(pt2);
    }

// Return panel
@Override public JPanel getP()
    {
    return p;
    }

// Return table
@Override public JTable getTable()
    {
    return table;
    }

// Return combo box
@Override public JComboBox getCombo()
    {
    return combo;
    }

// Return clear button
@Override public JButton getClearButton()
    {
    return null;
    }

// Return run button
@Override public JButton getRunButton()
    {
    return null;
    }

}
