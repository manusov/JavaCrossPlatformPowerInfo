//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Open tree node listener, for assign text table, linux =============

package powerinfo.supportlinux;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import powerinfo.*;

public class TreeMonitorLinux extends TreeMonitor 
{

private String h1 = "/sys/class/power_supply/AC";
private String h2 = "/sys/class/power_supply/BAT0";
    
public AbstractTableModel getTableModelByPath(TreePath x)
    {
    AbstractTableModel m = null;
    DefaultMutableTreeNode x1 = 
        (DefaultMutableTreeNode) x.getLastPathComponent();
    ListEntry x2 = (ListEntry) x1.getUserObject();
    String x3 = x2.path;
    
    // REPLACE THIS BY COMPARE IN THE CYCLE
    
    if      ( x3.equals(h1) ) { m = new TableModelPowerStatusLinux();    }
    else if ( x3.equals(h2) ) { m = new TableModelBatteryDetailsLinux(); }
    else                      { m = new STM1(); }
    
    return m;
    }
}
