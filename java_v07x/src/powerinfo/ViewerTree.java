//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== View Panel for tree and table =====================================

package powerinfo;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;

public class ViewerTree extends ViewPanel 
{
private final JPanel p;                  // complex panel
private final JSplitPane pp;             // split panel
private final JScrollPane sp1, sp2;      // scroll panels
private final JTree tree;                // trees
private final JTable table;              // tables
private final DefaultTreeModel dtm;      // trees models
private final AbstractTableModel atm;    // tables models
private final TreeNodeExpander tnx;      // tree node expander
private final BoxLayout bl;              // layout manager

public ViewerTree
    ( int x, int y, 
      DefaultTreeModel z1, AbstractTableModel z2, TreeNodeExpander z3 )
    {
    dtm=z1; atm=z2; tnx=z3;
//---------- Built panel components ----------
    tree = new JTree(dtm);
    tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeExpansionListener(tnx);
    table = new JTable(atm);
    sp1 = new JScrollPane(tree);
    sp2 = new JScrollPane(table);
//---------- Built split panel ----------    
    pp = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true );
    pp.setOneTouchExpandable(true);
    pp.setDividerSize(8);
    pp.setDividerLocation(x/2-27);
    pp.setLeftComponent(sp1);
    pp.setRightComponent(sp2);
//---------- Built panel and set layout ----------
    p = new JPanel();
    bl = new BoxLayout(p, BoxLayout.X_AXIS);
    p.setLayout(bl);
    p.add(pp);
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
