//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Node expand listener for CPU information tree =====================

package powerinfo.supportcpu;

import java.util.ArrayList;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import powerinfo.*;

public class CPUTNX1 extends TreeNodeExpander
{
private DefaultTreeModel dtm;
private ArrayList<DefaultMutableTreeNode> list;
private AbstractTableModel atm;
private ViewPanel vp;
private TreeMonitor tm;

public CPUTNX1 ( DefaultTreeModel x1, ArrayList<DefaultMutableTreeNode> x2 )
    {
    dtm = x1;
    list = x2;
    vp = null;
    tm = null;
    }

@Override public void treeExpanded(TreeExpansionEvent e1)
    {
//---------- Update co-visualized table ----------
    AbstractTableModel m = null;
    if (tm!=null)
        {
        m = tm.getTableModelByPath(null);
        if (m!=null)
            {
            vp.getTable().setModel(m);
            }
        }
    }

@Override public void treeCollapsed(TreeExpansionEvent e1)
    {
    // Reserved
    }

@Override public void setViewer(ViewPanel x)
    {
    vp = x;
    }

@Override public void setMonitor(TreeMonitor x)
    {
    tm = x;
    }

}
