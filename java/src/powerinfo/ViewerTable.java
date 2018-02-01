//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== View Panel for single table =======================================

package powerinfo;

import javax.swing.*;
import javax.swing.table.*;

public class ViewerTable extends ViewPanel
{
private final JPanel p;                  // complex panel
private final JScrollPane sp1;           // scroll panels
private final JTable table;              // tables
private final AbstractTableModel atm;    // tables models
private final BoxLayout bl;              // layout manager

public ViewerTable
    ( int x, int y, AbstractTableModel z1 )
    {
    atm = z1;
//---------- Built panel components ----------    
    table = new JTable(atm);
    sp1 = new JScrollPane(table);
//---------- Built panel and set layout ----------
    p = new JPanel();
    bl = new BoxLayout(p, BoxLayout.X_AXIS);
    p.setLayout(bl);
    p.add(sp1);
    }    

//---------- Return panel ----------    
@Override public JPanel getP()
    {
    return p;
    }

//---------- Return table ----------    
@Override public JTable getTable()
    {
    return table;
    }

//---------- Return combo box ----------    
@Override public JComboBox getCombo()
    {
    return null;
    }

//---------- Return clear button ----------    
@Override public JButton getClearButton()
    {
    return null;
    }

//---------- Return run button ----------    
@Override public JButton getRunButton()
    {
    return null;
    }

}
