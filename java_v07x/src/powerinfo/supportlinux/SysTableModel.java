//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Table Model for linux "/sys" file system, abstract template =======

package powerinfo.supportlinux;

import javax.swing.table.*;

public abstract class SysTableModel extends AbstractTableModel
{
@Override abstract public int getRowCount();
@Override abstract public int getColumnCount();
@Override abstract public String getColumnName(int column);
@Override abstract public Class getColumnClass(int column);
@Override abstract public Object getValueAt(int row, int column);
@Override abstract public boolean isCellEditable(int row, int column);
@Override abstract public void setValueAt(Object value, int row, int column);
}
