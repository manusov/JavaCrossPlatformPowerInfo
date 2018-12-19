/*
Java Power Info utility, (C)2018 IC Book Labs
Table model for Battery Details
*/

package powerinfo;

import javax.swing.table.*;

public abstract class TableModelBatteryDetails extends AbstractTableModel {
// Standard methods of AbstractTableModel
@Override abstract public int getRowCount();
@Override abstract public int getColumnCount();
@Override abstract public String getColumnName(int column);
@Override abstract public Class getColumnClass(int column);
@Override abstract public Object getValueAt(int row, int column);
@Override abstract public boolean isCellEditable(int row, int column);
@Override abstract public void setValueAt(Object value, int row, int column);
// Methods added in this application
abstract public boolean DynamicalBatteryDetails();
abstract public String[] getEnumerationPath();
abstract public void setSelected(int a);
abstract public double getStamp1();
abstract public double getStamp2();
abstract public boolean getPowerUnits();
abstract public double[][] readAllStamps();
}
