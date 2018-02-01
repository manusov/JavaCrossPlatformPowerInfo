//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== View Panel for power=f(time) drawings =============================

package powerinfo;

import java.awt.Dimension;
import java.util.Vector;
import javax.swing.*;

public class ViewerPowerDraws extends ViewPanel 
{
private final JPanel p;                         // complex  panel
private final JComboBox combo;                  // combo box                    *
private final DataModelChargingMonitor dmcm;    // data model for drawings
private final GraphIcon gric1, gric2;           // drawings images
private final JButton b21, b22;                 // buttons clear, run

public ViewerPowerDraws
    ( int x, int y,
    DataModelChargingMonitor z1, GraphIcon z2, GraphIcon z3,
    String comboName, Vector comboVector )
    {
    dmcm = z1; gric1 = z2; gric2 = z3;
    p = new JPanel();
//---------- Setup layouts ----------    
    JPanel phc21 = new JPanel();
    JPanel phc22 = new JPanel();
    JPanel phc23 = new JPanel();
    JPanel pvc21 = new JPanel();
    BoxLayout s2x1 = new BoxLayout (phc21, BoxLayout.X_AXIS);
    BoxLayout s2x2 = new BoxLayout (phc22, BoxLayout.X_AXIS);
    BoxLayout s2x3 = new BoxLayout (phc23, BoxLayout.X_AXIS);
    BoxLayout s2y1 = new BoxLayout (pvc21, BoxLayout.Y_AXIS);
    phc21.setLayout(s2x1);
    phc22.setLayout(s2x2);
    phc23.setLayout(s2x3);
    pvc21.setLayout(s2y1);
    
//---------- Setup combo components, added at v0.75 ----------
    combo = new JComboCheckBox(comboVector);
    combo.setMaximumSize( new Dimension(x,26) );
    JLabel l1 = new JLabel(comboName);
    JPanel phc1 = new JPanel();
    BoxLayout s1x1 = new BoxLayout (phc1, BoxLayout.X_AXIS);
    phc1.setLayout(s1x1);
    phc1.add(Box.createHorizontalStrut(4));
    phc1.add(l1);
    phc1.add(Box.createHorizontalStrut(6));
    phc1.add(combo);
    phc1.add(Box.createHorizontalStrut(3));
    
    
//---------- GUI components for drawings ----------
    JLabel lgric1 = new JLabel(gric1);
    JLabel lgric2 = new JLabel(gric2);
    b21 = new JButton("Clear");
    b22 = new JButton("Run");
//---------- Horizontal layout for graph1 with struts ----------
    phc21.add(Box.createHorizontalStrut(2));
    phc21.add(lgric1);
    phc21.add(Box.createHorizontalStrut(2));
//---------- Horizontal layout for graph2 with struts ----------
    phc22.add(Box.createHorizontalStrut(2));
    phc22.add(lgric2);
    phc22.add(Box.createHorizontalStrut(2));
//---------- Horizontal layout for buttons ----------
    Dimension db2 = new Dimension (80,28);
    b21.setPreferredSize(db2);
    b22.setPreferredSize(db2);
    phc23.add(Box.createHorizontalGlue());
    phc23.add(b21);
    phc23.add(Box.createHorizontalStrut(4));
    phc23.add(b22);
    phc23.add(Box.createHorizontalGlue());
//---------- Vertical layout for graph1, graph2, local buttons ----------
//--- addends at v0.75 ---
    pvc21.add(Box.createVerticalStrut(2));
    pvc21.add(phc1);
    pvc21.add(Box.createVerticalStrut(8));
//---
    pvc21.add(Box.createVerticalStrut(2));
    pvc21.add(phc21);
    pvc21.add(Box.createVerticalStrut(8));
    pvc21.add(phc22);
    pvc21.add(Box.createVerticalStrut(8));
    pvc21.add(phc23);
    pvc21.add(Box.createVerticalStrut(8));
//---------- Make panel ----------
    p.add(pvc21);
    }

//---------- Return panel ----------    
@Override public JPanel getP()
    {
    return p;
    }

//---------- Return table ----------    
@Override public JTable getTable()
    {
    return null;
    }

//---------- Return combo box ----------    
@Override public JComboBox getCombo()
    {
    return null;
    }

//---------- Return clear button ----------    
@Override public JButton getClearButton()
    {
    return b21;
    }

//---------- Return run button ----------    
@Override public JButton getRunButton()
    {
    return b22;
    }

}
