/*
Java Power Info utility, (C)2021 IC Book Labs
Open tree node listener, for assign text table, CPU
*/

package powerinfo.supportcpu;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.*;
import powerinfo.*;

public class TreeMonitorCPU extends TreeMonitor 
{
private AbstractTableModel m;
    
@Override public AbstractTableModel getTableModelByPath( TreePath x )
    {
    return m;
    }

public void setModel( AbstractTableModel x ) 
    {
    m = x;
    }
}
