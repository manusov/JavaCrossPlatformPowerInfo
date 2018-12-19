/*
Java Power Info utility, (C)2018 IC Book Labs
Open tree node listener, for assign text table, linux
*/

package powerinfo.supportlinux;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import powerinfo.*;

public class TreeMonitorLinux extends TreeMonitor 
{
private final String h1 = "/sys/class/power_supply/AC";
private final String h2 = "/sys/class/power_supply/BAT0";
    
@Override public AbstractTableModel getTableModelByPath(TreePath x)
    {
    AbstractTableModel m;
    DefaultMutableTreeNode x1 = 
        (DefaultMutableTreeNode) x.getLastPathComponent();
    ListEntry x2 = (ListEntry) x1.getUserObject();
    String x3 = x2.path;

    switch (x3) {
        case h1:
            m = new TableModelPowerStatusLinux();
            break;
        case h2:
            m = new TableModelBatteryDetailsLinux();
            break;
        default:
            m = new STM1();
            break;
        }
    return m;
    }
}
