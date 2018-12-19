/*
Java Power Info utility, (C)2018 IC Book Labs
Table model for Tree Monitor (text table built), abstract class
*/

package powerinfo;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

public abstract class TreeMonitor 
    {
    public abstract AbstractTableModel getTableModelByPath(TreePath x);
    }
