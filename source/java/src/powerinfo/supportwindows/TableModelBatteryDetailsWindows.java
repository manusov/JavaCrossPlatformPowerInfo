/*
Java Power Info utility, (C)2021 IC Book Labs
Table model for Battery Details

See detail comments at WIN32JNI.ASM, WIN64JNI.ASM
Native IOCTL functions description:
https://msdn.microsoft.com/ru-ru/library/windows/desktop/aa372659(v=vs.85).aspx
*/

package powerinfo.supportwindows;

import powerinfo.*;

public class TableModelBatteryDetailsWindows extends TableModelBatteryDetails {
private final String[] colNames = { "Parameter", "Value", "Comments" };
private final String[] rowNames = 
                            { "OS enumeration",
                              "Battery tag",
                              "Battery model",
                              "Battery vendor",
                              "Manufacture date",
                              "Serial number",
                              "Unique ID",
                              "Battery chemistry",
                              "Battery technology flag",
                              "Battery designed capacity",
                              "Battery full charged capacity",
                              "Default alert level 1",
                              "Default alert level 2",
                              "Critical bias",
                              "Cycle count",
                              "Battery temperature",
                              "Actual power state",  // "Current power state"
                              "Actual capacity",     // "Current capacity"
                              "Actual voltage",      // "Current voltage"
                              "Actual rate" };        // "Current rate"

private final String[] MONTHS = { "?",
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December" };

private final String[] BATTERY_TYPES = {
    "N/A"  , "Data is not readable",
    "PbAc" , "Lead Acid" ,
    "LION" , "Lithium Ion" ,
    "Li-I" , "Lithium Ion" ,
    "NiCd" , "Nickel Cadmium" ,
    "NiMH" , "Nickel Metal Hydride" ,
    "NiZn" , "Nickel Zinc" ,
    "RAM"  , "Rechargeable Alkaline-Manganese",
    "VBOX" , "Oracle Virtual Machine battery" };

private final String[] BATTERY_TECHNOLOGIES = {
    "Nonrechargeable", "Rechargeable", "Unknown type" };

private final String[] BATTERY_STATES = {
    "Online", "Discharging", "Charging", "Critical" };

private String[] rowValues = null;
private String[] rowComments = null;
private static final int OPB_SIZE = 2562;  // 256*10 + 2 , 10 units
private long[] OPB;
private long chg1 = -1, chg2 = -1, chg3 = -1, chg4 = -1, chg5 = -1;
private boolean fchg;
private int count = 0, selected = 0;
private boolean powerUnits = false;

private int mask8;
private long mask16;
private long mask32;

private double stamp1 = Double.NaN, stamp2 = Double.NaN;

public TableModelBatteryDetailsWindows()
    {
    mask8 = 0xFF;
    mask16 = 0xFFFF;
    mask32 = -1; mask32 = mask32 >>> 32;
    if ( PowerInfo.pal.getNativeValid() == true )
        {
        OPB = new long[OPB_SIZE];
        for (int i=0; i<OPB_SIZE; i++) { OPB[i]=0; }
        rowValues = new String[rowNames.length];
        rowComments = new String[rowNames.length];
        DynamicalBatteryDetails();
        }     // End of native library validity condition block
    }         // End of class constructor


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
                  default:  if ( rowComments != null && rowComments[row] != null )
                                 { return " " + rowComments[row]; }       
                            else { return " ";                  } } }

@Override public boolean isCellEditable( int row, int column ) { return false; }
@Override public void setValueAt( Object value, int row, int column ) {}

@Override public boolean DynamicalBatteryDetails()
    {
    mask8 = 0xFF;
    mask16 = 0xFFFF;
    mask32 = -1; mask32 = mask32 >>>32;
    fchg = false;
    if ( (PowerInfo.pal.NativeAgent( null, OPB, 2, OPB_SIZE )) != 0 )
        {
        long x, y;
        double z;
        String s, s1="";
        boolean f;
        // Check state change for revisual requests
        // distributed to strings builders
        // Rebuilt table text data
        count = (int)OPB[0];  // THIS VARIABLE GLOBAL, ITEMS COUNT
        if (count!=0)         // Check OPB[0] = Number of devices
            {
            int i = selected * 256;
            int j = 0, k = 0;
            // OS enumeration
            s = "";
            long b1 = 0;
            char c1 = ' ';
            int m1 = 0;
            for ( int j1=0; j1<128; j1++ )
                {
                b1 = OPB[ i + m1 + 8 ];
                for ( int k1=0; k1<8; k1++ )     // cycle for bytes in the qword
                    {
                    c1 = (char)( b1 & 0xFF );
                    if ( c1 == 0 ) { break; }
                    s = s + c1;
                    b1 = b1 >> 8;
                    } if ( c1 == 0 ) { break; }
                m1++;
                }
            rowValues[j] = s;
            rowComments[j] = "BAT #" + selected;
            j++;
            // Battery tag
            k = i + 137; x = OPB[k];
            y = ( x >> 32 ) & mask32;
            rowValues[j]=String.format( "%08Xh", y ); j++;
            // Battery model
            k = i + 140; stringWrite( j, k ); j++;
            //--- Battery vendor ---
            k = i + 156; stringWrite( j, k ); j++;
            // Manufacture date
            k = i + 172;
            x=OPB[k]; y=OPB[k+1];
            if ((x==0)|(y==0))
                  { rowValues[j] = "n/a"; }
            else  { if ( ( y > 100 )|( y < 4 ) )
                  { rowValues[j] = "wrong data size"; } }
            if ( !( (( x == 0 )|( y == 0 )) | (( y > 100 )|( y < 4 )) ) )
                {
                x = OPB[k+2];
                x = x & mask32;
                rowValues[j] = String.format( "%08Xh" , x );
                if ( x != 0 )
                    {
                    y = ( x >> 16 ) & mask16; s = String.format( "%d, ", y );
                    y = ( x >> 8 ) & mask8; if ( y > 12 ) { y=0; }
                    s = s + MONTHS[(int)y] + " ";
                    y = x & mask8; s = s + String.format( "%d", y );
                    rowComments[j] = s;
                    }
                } j++;
            // Battery serial number, as string
            k = i + 176; stringWrite( j, k ); j++;
            // Battery Unique ID as string
            k = i + 192; stringWrite( j, k ); j++;
            // Battery chemistry, and initializing k=index for next
            k = i + 208;
            x=OPB[k]; y=OPB[k+1]; s="";
            if ((x==0)|(y==0))
                  { rowValues[j] = "n/a"; }
            else  { if ( ( y > 100 )|( y < 36 ))
                  { rowValues[j] = "wrong data size"; } }
            if ( !( (( x == 0 )|( y == 0 )) | (( y > 100 )|( y < 36 )) ) )
                {
                x = OPB[k+3];
                y = x & mask32;
                s="";
                for ( int r=0; r<4; r++ ) 
                    { if ( ( y &mask8 ) == 0 ) { break; }
                      s = s + (char)( y & mask8 ); y = y >> 8;  }
                rowValues[j]=s;
                int q = ( BATTERY_TYPES.length )/2;
                int p = 0; s1 = "Unknown";
                for ( int r=0; r<q; r++ )
                    { p = r*2;
                      if ( ( s.compareToIgnoreCase(BATTERY_TYPES[p]) ) == 0 )
                         { s1 = BATTERY_TYPES[p+1]; break; }  }
                rowComments[j] = s1; j++;
                // Battery technology flag
                x = OPB[k+2];
                y = ( x >> 32 ) & mask8;
                rowValues[j] = String.format( "%02Xh", y );
                if ( y > 2 ) { y = 2; }
                rowComments[j] = BATTERY_TECHNOLOGIES[(int)y]; j++;
                // Battery designed capacity
                // variable powerUnits is globally used also, getPowerUnits()
                if (( x & 0x40000000) == 0 ) { powerUnits = false; s1 = "mWh"; }
                else { powerUnits = true; s1 = "relative ratio units"; }
                //
                x = OPB[k+3];
                y = (x >> 32) & mask32;
                if ( y == 0 ) { s = "n/a"; }
                else { s = String.format( "%d ", y ) + s1; }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s; j++;
                // Battery full charged capacity
                x = OPB[k+4];
                y = x & mask32;
                if (y==0) { s = "n/a"; }
                else { s = String.format( "%d ", y ) + s1; }
                rowValues[j]   = String.format( "%08Xh" , y );
                rowComments[j] = s; j++;
                // Default alert #1 capacity
                x = OPB[k+4];
                y = ( x >> 32 ) & mask32;
                if ( y == 0 ) { s = "n/a"; }
                else { s = String.format( "%d ", y ) + s1; }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s; j++;
                // Default alert #2 capacity
                x = OPB[k+5];
                y = x & mask32;
                if (y==0) { s = "n/a"; }
                else { s = String.format( "%d ", y ) + s1; }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s; j++;
                // Critical bias
                y = (x >> 32) & mask32;
                if (y==0) { s = "n/a"; }
                else { s = String.format( "%d ", y ) + s1; }
                rowValues[j]   = String.format( "%08Xh" , y );
                rowComments[j] = s; j++;
                // Cycle count (charge-discharge)
                x = OPB[k+6];
                y = x & mask32;
                if (y==0) { s = "n/a"; }
                else { s = String.format( "%d ", y ); }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s; j++;
                } else { j+=8; }  // can add fill "n/a" at this point
            // Battery temperature, this starts new request results
            k = i + 216;
            x = OPB[k]; y = OPB[k+1]; s = "";
            if ( ( x == 0 )|( y == 0 ) )
                  { rowValues[j] = "n/a"; }
            else  { if ((y>100)|(y<4))
                  { rowValues[j] = "wrong data size"; } }
            if ( !( (( x == 0 )|( y == 0 )) | ( ( y > 100 )|( y < 4 )) ) )
                {
                x = OPB[k+2];
                y = x & mask32;
                if ( y != chg1 ) { chg1 = y; fchg = true; }  // MONITORING FOR REVISUAL
                z = (double)y;
                z = z*10 - 273.15;
                s = String.format( "%.2f C", z );
                if ( (z<-100.0)|(z>150.0) )
                   { s = "Unknown parameter encoding"; }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s;
                } j++;
            // Battery status, this starts new request results
            k = i + 220;
            x = OPB[k]; y = OPB[k+1]; s = "";
            if ( ( x == 0 )|( y == 0 ) )
                  { rowValues[j] = "n/a"; }
            else  { if ( ( y > 100 )|( y < 16 ) )
                  { rowValues[j] = "wrong data size"; } }
            if ( !( ( ( x == 0 )|( y == 0 )) | ( ( y > 100 )|( y < 16 ) ) ) )
                {
                // Status field 1 of 4, power status
                x = OPB[k+2];
                y = x & mask32;
                if ( y != chg2 ) { chg2 = y; fchg = true; }  // MONITORING FOR REVISUAL
                s = "";
                f = false;
                if ( ( y & 1 ) != 0 ) { s = s + BATTERY_STATES[0]; f = true; }
                if ( ( y & 2 ) != 0 ) { if ( f == true) { s = s + ", "; }
                                        s = s + BATTERY_STATES[1]; f = true; }
                if ( ( y & 4 ) != 0 ) { if ( f == true) { s = s + ", "; }
                                        s = s + BATTERY_STATES[2]; f = true; }
                if ( ( y & 8 ) != 0 ) { if (f == true) { s = s + ", "; }
                                        s = s + BATTERY_STATES[3]; f = true; }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s; j++;
                // Status field 2 of 4, Current level of capacity
                y = ( x >> 32 ) & mask32;
                if ( y != chg3 ) { chg3 = y; fchg = true; }  // MONITORING FOR REVISUAL
                s = "";
                if ( y == 0 ) { s = "n/a"; }
                else { s = String.format( "%d ", y ) + s1; }
                rowValues[j]   = String.format( "%08Xh", y ); 
                stamp1 = (int)y;
                rowComments[j] = s; j++;
                // Status field 3 of 4, voltage
                x = OPB[k+3];
                y = x & mask32;
                if ( y != chg4 ) { chg4 = y; fchg = true; }  // MONITORING FOR REVISUAL
                z = (double)y;
                z = z/1000.0;
                s = String.format( "%.3f volts", z );
                if ( ( z < 5.0 )|( z > 50.0 ) )
                   { s = "Unknown parameter encoding"; }
                if ( y == 0 ) { s="n/a"; }
                rowValues[j]   = String.format( "%08Xh", y );
                rowComments[j] = s; j++;
                // Status field 4 of 4, Charging or discharging rate
                y = ( x >> 32 ) & mask32;
                if ( y != chg5 ) { chg5 = y; fchg = true; }  // MONITORING FOR REVISUAL
                s = "";
                s = String.format( "%d mW",(int)y );
                boolean pValid = true;
                if ( ( (int)y < -1000000000 )|( (int)y > 1000000000 ) )
                          { s = "Unknown parameter encoding"; pValid = false; }
                if ( y == 0 ) { s = "n/a"; }
                rowValues[j]   = String.format( "%08Xh", y );
                // this for drawings
                if ( pValid ) { stamp2 = (int)y; }
                else          { stamp2 = Double.NaN; }
                rowComments[j] = s;
                j++;  // v0.75
                } else { j+=4; }  // can add fill "n/a" at this point
            }
        }
  return fchg;
  }        // End of method


@Override public String[] getEnumerationPath()
    {
    if ( count == 0 ) { return null; }
    String[] a = new String[count];
    for( int i=0; i<count; i++ )
        {
        a[i] = "BAT #" + i;
        }
    return a;
    }
        
        
@Override public void setSelected( int a ) 
    {
    selected = a;
    chg1 = -1; chg2 = -1; chg3 = -1; chg4 = -1; chg5 = -1;  // this for revisual request
    DynamicalBatteryDetails();
    }

@Override public double getStamp1() { return stamp1; }

@Override public double getStamp2() { return stamp2; }

@Override public boolean getPowerUnits() 
    { 
    // return true; // DEBUG UNITS = RELATIVE RATIO UNITS
    return powerUnits;
    }

@Override public double[][] readAllStamps()
    {
    double[][] a = null;
    if ( (PowerInfo.pal.NativeAgent( null, OPB, 2, OPB_SIZE )) != 0 )
        {
        int b = (int)OPB[0];
        if (b!=0)                         // Check OPB[0] = Number of devices
            {
            a = new double[b][2];
            for ( int i=0; i<b; i++ )
                {
                int c = (int)(( OPB[256*i+222] >> 32 ) & mask32);
                int d = (int)(( OPB[256*i+223] >> 32 ) & mask32);
                a[i][0] = c;
                a[i][1] = d;
                if ( ( d < -1000000000 )|( d > 1000000000 ) )
                    {
                    a[i][1] = Double.NaN;
                    }
                }
            }
        }
    return a;
    }


// Helper methods

private void stringWrite( int j, int k )
    {
    long a = OPB[k], b = OPB[k+1]; char c = 0; String d = ""; k += 2;
    if ( ( a == 0 )|( b == 0 ) )  { rowValues[j] = "n/a"; return; }
    if (( b < 0 )|( b > 100 ) ) { rowValues[j] = "n/a (wrong string size)"; return; }
    int n = 0; boolean m = false;
    while ( b > 0 )
        {
        if ( ( n % 4 ) == 0 ) { a = OPB[k]; k++; } n++;   // Reload qword each 4 steps
        c = (char) ( a & mask16 );                  // Get current char or NULL
        if ( c == 0 ) break;                        // Stop copy if NULL
        d = d + c; a = a >> 16; b--;                // Add char to string, shift
        if ( c != ' ' ) { m = true; }               // Mark not empty if char
        }
    if ( m == false ) { d = "n/a (empty string)"; }
    rowValues[j] = d;
    }
}          // End of class TableModelPowerStatus