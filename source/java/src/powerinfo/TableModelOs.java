/*
Java Power Info utility, (C)2021 IC Book Labs
Table model for OS information, abstract class
*/

package powerinfo;

import javax.swing.table.*;

public abstract class TableModelOs extends AbstractTableModel {
// Standard methods of AbstractTableModel
@Override abstract public int getRowCount();
@Override abstract public int getColumnCount();
@Override abstract public String getColumnName( int column );
@Override abstract public Class getColumnClass( int column );
@Override abstract public Object getValueAt( int row, int column );
@Override abstract public boolean isCellEditable( int row, int column );
@Override abstract public void setValueAt( Object value, int row, int column );
}
