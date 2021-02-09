/*
Java Power Info utility, (C)2021 IC Book Labs
Table Model for battery details, linux
*/

package powerinfo.supportlinux;

import powerinfo.*;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;

public class TableModelBatteryDetailsLinux extends TableModelBatteryDetails {
    
private final String sysPath1 = "/sys/class/power_supply/BAT0";  // BUG CONST 0
private final String drawParm1 = sysPath1 + "/" + "energy_now" + "/";
private final String drawParm2 = sysPath1 + "/" + "power_now" + "/";
private final String drawParm3 = sysPath1 + "/" + "capacity" + "/";
private final String drawParm4 = sysPath1 + "/" + "voltage_now" + "/";
private final String drawParm5 = sysPath1 + "/" + "status" + "/";

private String stringParm, backup5;
private int intParm, backup1, backup2, backup3, backup4;
private double doubleParm;
private boolean request;

private ListEntry sysEntry;
private DefaultMutableTreeNode sysNode;
private final ArrayList<DefaultMutableTreeNode> sysArray;
private String sysString;
BuiltBatteryDetails bbd;

private final String[] colNames = { "Parameter", "Value", "Comments" };
private String[] rowNames = { "NO DATA" };
private String[] rowValues = null;
private String[] rowComments = null;

private double stamp1 = Double.NaN, stamp2 = Double.NaN;

public TableModelBatteryDetailsLinux()
    {
        
    backup1 = -1;
    backup2 = -1;
    backup3 = -1;
    backup4 = -1;
    backup5 = "?";
        
    bbd = new BuiltBatteryDetails();
    sysArray = new ArrayList();
    sysEntry = new ListEntry( "", "", sysPath1, false, false );
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
        bbd.parmTranslator( rowNames, rowValues, rowComments );
        }

    }

@Override public int getRowCount() { return rowNames.length; }
@Override public int getColumnCount() { return colNames.length; }
@Override public String getColumnName( int column ) { return colNames[column]; }
@Override public Class getColumnClass( int column ) { return String.class; }
@Override public Object getValueAt( int row, int column )
            { switch(column)
                { case 0:  return " " + rowNames[row];
                  case 1: if ( rowValues!=null && rowValues[row] != null )
                                 { return " " + rowValues[row]; }       
                            else { return " ";                  }  
                  default:  if ( rowComments!=null && rowComments[row] != null )
                                 { return " " + rowComments[row]; }       
                            else { return " ";                  } } }

@Override public boolean isCellEditable( int row, int column ) { return false; }
@Override public void setValueAt( Object value, int row, int column ) {}

@Override public boolean DynamicalBatteryDetails()
    {
    stamp1 = 0.0;
    stamp2 = 0.0;
    request = false;
    
    stringParm = FileService.readParameter ( drawParm1 );
    intParm = 0;
    if ( stringParm != null )
        {
        try { intParm = Integer.parseInt( stringParm ); }
        catch ( Exception e ) { }
        }
    if ( intParm != backup1 ) { backup1 = intParm; request = true; }
    doubleParm = intParm;
    //  check errors, return NOT A NUMBER if error
    if (stringParm == null) { stamp1 = Double.NaN;          }
    else                    { stamp1 = doubleParm / 1000.0; }
    stringParm = FileService.readParameter ( drawParm2 );
    intParm = 0;
    if ( stringParm != null )
        {
        try { intParm = Integer.parseInt( stringParm ); }
        catch ( Exception e ) { }
        }
    if ( intParm != backup2 ) { backup2=intParm; request=true; }
    doubleParm = intParm;
    // check errors, return NOT A NUMBER if error
    if (stringParm==null) { stamp2 = Double.NaN;          }
    else                  { stamp2 = doubleParm / 1000.0; }

    stringParm = FileService.readParameter ( drawParm3 );
    intParm = 0;
    if ( stringParm != null )
        {
        try { intParm = Integer.parseInt( stringParm ); }
        catch ( Exception e ) { }
        }
    if ( intParm != backup3 ) { backup3 = intParm; request = true; }
 
    stringParm = FileService.readParameter ( drawParm4 );
    intParm = 0;
    if ( stringParm != null )
        {
        try { intParm = Integer.parseInt( stringParm ); }
        catch ( Exception e ) { }
        }
    if ( intParm != backup4 ) { backup4 = intParm; request = true; }
    
    stringParm = FileService.readParameter ( drawParm5 );
    if ( stringParm != null )
        {
        if (!(stringParm.equals( backup5 ) ) ) 
            { backup5=stringParm; request = true; }
        }
    if ( !request ) { return false; }
    
    sysArray.clear();

    // START of fragment can be subroutine, optimization required
    sysEntry = new ListEntry( "", "", sysPath1, false, false );
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
            sysNode = sysArray.get( i+2 );
            sysEntry = (ListEntry)sysNode.getUserObject();
            sysString = sysEntry.name1;
            rowNames[i] = "" + sysString;
            sysString = sysEntry.name2;
            rowValues[i] = "" + sysString;
            }
        bbd.parmTranslator( rowNames, rowValues, rowComments );
        }
    // END of fragment can be subroutine, optimization required

    return true;    
    }

@Override public String[] getEnumerationPath()
    {
        
    // UNDER CONSTRUCTION, YET BUG: ONLY BAT0 DEVICE    
    
    String[] a = { sysPath1 };
    return a; 
        
    }

@Override public void setSelected( int a ) { }
@Override public double getStamp1() { return stamp1; }
@Override public double getStamp2() { return stamp2; }
// This functionality used for Windows, not used yet for Linux,
// return FALSE means units = mWh/mW, this variant used
// return TRUE  means units = Relative ratio units
@Override public boolean getPowerUnits() { return false; }
@Override public double[][] readAllStamps() { return null; }

}
