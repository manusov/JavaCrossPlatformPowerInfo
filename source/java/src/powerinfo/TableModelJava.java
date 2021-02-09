/*
Java Power Info utility, (C)2021 IC Book Labs
Table model for Java JVM information
*/

package powerinfo;

import java.util.Properties;
import javax.swing.table.*;

public class TableModelJava extends AbstractTableModel {
private final String[] colNames = { "Parameter", "Value" };
private final String[] rowNames = { "java.version",
                                    "java.vendor",
                                    "java.vendor.url",
                                    "java.home",
                                    "java.class.version",
                                    "java.class.path",
                                    "os.name",
                                    "os.arch",
                                    "os.version",
                                    "file.separator",
                                    "path.separator",
                                    "line.separator",
                                    "user.name",
                                    "user.home",
                                    "user.dir" };
private final String[] rowValues;
private Properties p;

public TableModelJava() { int n = rowNames.length;
                          rowValues = new String[n];
                          p = System.getProperties();
                          for ( int i=0; i<n; i++ )
                            { 
                            String s = p.getProperty(rowNames[i]);
                            int m = s.length();
                            boolean b = false;
                            for ( int j=0; j<m; j++ )
                                {
                                char c = s.charAt(j);
                                if ( c < ' ' ) { b=true; }
                                }
                            if ( b )
                                {
                                String s1 = "";
                                for ( int j=0; j<m; j++ )
                                    {
                                    s1 = s1 + (int)(s.charAt(j));
                                    if ( j  < m-1 ) { s1 = s1 + ", "; }
                                    }
                                s = s1;
                                }
                            
                            rowValues[i] = s;
                            } }
@Override public int getRowCount() { return rowNames.length; }
@Override public int getColumnCount() { return colNames.length; }
@Override public String getColumnName( int column ) { return colNames[column]; }
@Override public Class getColumnClass( int column ) { return String.class; }
@Override public Object getValueAt( int row, int column )
                        { switch( column )
                            { case 0:  return " " + rowNames[row];
                              default: return " " + rowValues[row]; } }
@Override public boolean isCellEditable( int row, int column ) { return false; }
@Override public void setValueAt( Object value, int row, int column ) {}
}
