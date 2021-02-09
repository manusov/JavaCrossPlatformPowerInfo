/*
Java Power Info utility, (C)2021 IC Book Labs
Table model for Power Status, abstract class
*/

package powerinfo;

import javax.swing.table.*;

public abstract class TableModelPowerStatus extends AbstractTableModel {
// Standard methods of AbstractTableModel
@Override abstract public int getRowCount();
@Override abstract public int getColumnCount();
@Override abstract public String getColumnName( int column );
@Override abstract public Class getColumnClass( int column );
@Override abstract public Object getValueAt( int row, int column );
@Override abstract public boolean isCellEditable( int row, int column );
@Override abstract public void setValueAt( Object value, int row, int column );
// Methods added in this application
abstract public boolean DynamicalPowerStatus();
}
