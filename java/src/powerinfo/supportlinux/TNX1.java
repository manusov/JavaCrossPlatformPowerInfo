//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Node expand listener for linux virtual file system tree ===========

package powerinfo.supportlinux;

import java.util.ArrayList;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import powerinfo.*;

public class TNX1 extends TreeNodeExpander
{
private DefaultTreeModel dtm;
private ArrayList<DefaultMutableTreeNode> list;
private AbstractTableModel atm;
private ViewPanel vp;
private TreeMonitor tm;

public TNX1 ( DefaultTreeModel x1, ArrayList<DefaultMutableTreeNode> x2 )
    {
    dtm = x1;
    list = x2;
    vp = null;
    tm = null;
    }

@Override public void treeExpanded(TreeExpansionEvent e1)
    {
    TreePath x1 = e1.getPath();
    DefaultMutableTreeNode x2 = 
            (DefaultMutableTreeNode) x1.getLastPathComponent();
    ListEntry x3 = 
            (ListEntry) x2.getUserObject();
//---------- Handling open node ----------
    // if (x3.handled==false)
    //    {
        FileService.builtTree( list, 1, x2  , true , true );
        dtm.reload(x2);
    //    }
//---------- Handling childs of opened node ----------
    int n = x2.getChildCount();
    DefaultMutableTreeNode y1;
    for (int i=0; i<n; i++)
        {
        y1 = (DefaultMutableTreeNode) x2.getChildAt(i);
        FileService.builtTree( list, 1, y1, true, true );
        dtm.reload(y1);
        }
//---------- Update co-visualized table ----------
    AbstractTableModel m = null;
    if (tm!=null)
        {
        m = tm.getTableModelByPath(x1);
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
