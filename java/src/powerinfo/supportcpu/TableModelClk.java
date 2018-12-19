/*
Java Power Info utility, (C)2018 IC Book Labs
Table Model for Processor clock
*/

package powerinfo.supportcpu;

public class TableModelClk extends TableModelCPU {
    
private final String[] colNames = { "Parameter", "Value" };
private final String[] rowNames = { "Time Stamp Counter" };
private String[] rowValues = null;
private final String[] rowComments = null;

public TableModelClk(Double x)
    {
    x/=1000000.0;
    String s = String.format( "%.2f MHz", x );
    rowValues = new String[1];
    rowValues[0] = s;
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
