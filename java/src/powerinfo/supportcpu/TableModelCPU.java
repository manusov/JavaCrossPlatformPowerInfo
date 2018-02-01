//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Table Model for Processor information, common =====================

package powerinfo.supportcpu;

import javax.swing.table.*;

public class TableModelCPU extends AbstractTableModel {
    
private String[] colNames = { "Parameter", "Value", "Comments" };
private String[] rowNames = { "No text table for this screen" };
private String[] rowValues = null;
private String[] rowComments = null;

public TableModelCPU()
    {
    // UNDER CONSTRUCTION
    }

@Override public int getRowCount() { return rowNames.length; }
@Override public int getColumnCount() { return colNames.length; }
@Override public String getColumnName(int column) { return colNames[column]; }
@Override public Class getColumnClass(int column) { return String.class; }
@Override public Object getValueAt(int row, int column)
            { switch(column)
                { case 0:  return " " + rowNames[row];
                  case 1: if ( rowValues!=null && rowValues[row]!=null )
                                 { return " " + rowValues[row]; }       
                            else { return " ";                  }  
                  default:  if ( rowComments!=null && rowComments[row]!=null )
                                 { return " " + rowComments[row]; }       
                            else { return " ";                  } } }

@Override public boolean isCellEditable(int row, int column) { return false; }
@Override public void setValueAt(Object value, int row, int column) {}

}
