/*
Java Power Info utility, (C)2021 IC Book Labs
Table Model for power status, linux
*/

package powerinfo.supportlinux;

import powerinfo.*;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;

public class TableModelPowerStatusLinux extends TableModelPowerStatus {

private final String sysPath1 = "/sys/class/power_supply/AC";
private final String sysPath2 = "/sys/class/power_supply/AC/device";
private final String drawParm1 = sysPath1 + "/" + "online" + "/";

private String stringParm;
private int intParm, intParmBackup;

private ListEntry sysEntry;
private DefaultMutableTreeNode sysNode;
private final ArrayList<DefaultMutableTreeNode> sysArray;
private String sysString;
BuiltPowerStatus bps;

private final String[] colNames = { "Parameter", "Value", "Comments" };
private String[] rowNames = { "NO DATA" };
private String[] rowValues=null;
private String[] rowComments=null;

public TableModelPowerStatusLinux()
    {
    intParmBackup = -1;
    bps = new BuiltPowerStatus();
    sysArray = new ArrayList();
    sysEntry = new ListEntry( "", "", sysPath1, false, false );
    sysNode = new DefaultMutableTreeNode( sysEntry, true );
    sysArray.add( sysNode );
    sysEntry = new ListEntry( "", "", sysPath2, false, false );
    sysNode = new DefaultMutableTreeNode( sysEntry, true );
    sysArray.add( sysNode );
    
    FileService.builtTree( sysArray, 2, null, false, true );
    
    int n = sysArray.size();
    int m = n-2;
    if (m>0)
        {
        rowNames = new String[m];
        rowValues = new String[rowNames.length];
        rowComments = new String[rowNames.length];

        for ( int i=0; i<m; i++ )
            {
            sysNode = sysArray.get(i+2);
            sysEntry = (ListEntry)sysNode.getUserObject();
            sysString = sysEntry.name1;
            rowNames[i] = "" + sysString;
            sysString = sysEntry.name2;
            rowValues[i] = "" + sysString;
            }
        
        bps.parmTranslator( rowNames, rowValues, rowComments );
        }
    }

@Override public int getRowCount() { return rowNames.length; }
@Override public int getColumnCount() { return colNames.length; }
@Override public String getColumnName( int column ) { return colNames[column]; }
@Override public Class getColumnClass( int column ) { return String.class; }
@Override public Object getValueAt( int row, int column )
            { switch(column)
                { case 0:  return " " + rowNames[row];
                  case 1: if ( rowValues != null && rowValues[row] != null )
                                 { return " " + rowValues[row]; }       
                            else { return " ";                  }  
                  default:  if ( rowComments!=null && rowComments[row] != null )
                                 { return " " + rowComments[row]; }       
                            else { return " ";                  } } }

@Override public boolean isCellEditable( int row, int column ) { return false; }
@Override public void setValueAt( Object value, int row, int column ) {}

@Override public boolean DynamicalPowerStatus()
    {
    stringParm = FileService.readParameter ( drawParm1 );
    intParm = -1;
    if ( stringParm != null )
        {
        try { intParm = Integer.parseInt( stringParm ); }
        catch ( Exception e ) { }
        }
    
    if ( intParm == intParmBackup ) { return false; }
    intParmBackup = intParm;
    
    sysArray.clear();

    // START of fragment can be subroutine, optimization required
    sysEntry = new ListEntry( "", "", sysPath1, false, false );
    sysNode = new DefaultMutableTreeNode( sysEntry, true );
    sysArray.add( sysNode );
    sysEntry = new ListEntry( "", "", sysPath2, false, false );
    sysNode = new DefaultMutableTreeNode( sysEntry, true );
    sysArray.add( sysNode );
    
    FileService.builtTree( sysArray, 2, null, false, true );
    
    int n = sysArray.size();
    int m = n-2;
    if (m>0)
        {
        rowNames = new String[m];
        rowValues = new String[rowNames.length];
        rowComments = new String[rowNames.length];

        for ( int i=0; i<m; i++ )
            {
            sysNode = sysArray.get(i+2);
            sysEntry = (ListEntry)sysNode.getUserObject();
            sysString = sysEntry.name1;
            rowNames[i] = "" + sysString;
            sysString = sysEntry.name2;
            rowValues[i] = "" + sysString;
            }
        
        bps.parmTranslator( rowNames, rowValues, rowComments );
        }
    // END of fragment can be subroutine, optimization required
    
    return true;    
    }
}
