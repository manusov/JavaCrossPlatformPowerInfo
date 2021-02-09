/*
Java Power Info utility, (C)2021 IC Book Labs
Table model for Power Status

Native function GetSystemPowerStatus results comments, see also:

https://msdn.microsoft.com/en-us/library/windows/desktop/aa373232(v=vs.85).aspx

typedef struct _SYSTEM_POWER_STATUS {
  BYTE  ACLineStatus;
  BYTE  BatteryFlag;
  BYTE  BatteryLifePercent;
  BYTE  SystemStatusFlag;
  DWORD BatteryLifeTime;
  DWORD BatteryFullLifeTime;
} SYSTEM_POWER_STATUS, *LPSYSTEM_POWER_STATUS;

ACLineStatus
The AC power status. This member can be one of the following values. 
Value Meaning
0     Offline
1     Online
255   Unknown status

BatteryFlag
The battery charge status. This member can contain one or more of the following flags. 
Value Meaning
1     High—the battery capacity is at more than 66 percent
2     Low—the battery capacity is at less than 33 percent
4     Critical—the battery capacity is at less than five percent
8     Charging
128   No system battery
255   Unknown status—unable to read the battery flag information

BatteryLifePercent
The percentage of full battery charge remaining.
This member can be a value in the range 0 to 100, or 255 if status is unknown

SystemStatusFlag
The status of battery saver. To participate in energy conservation, avoid resource
intensive tasks when battery saver is on. To be notified when this value changes,
call the RegisterPowerSettingNotification function with the power setting
GUID, GUID_POWER_SAVING_STATUS. 
Value Meaning
0     Battery saver is off.
1     Battery saver on. Save energy where possible.

BatteryLifeTime
The number of seconds of battery life remaining,
or –1 if remaining seconds are unknown.

BatteryFullLifeTime
The number of seconds of battery life when at full charge,
or –1 if full battery lifetime is unknown.

*/

package powerinfo.supportwindows;

import powerinfo.*;

public class TableModelPowerStatusWindows extends TableModelPowerStatus {
private final String[] colNames = { "Parameter", "Value", "Comments" };
private final String[] rowNames = { "ACLineStatus",
                                    "BatteryFlag",
                                    "BatteryLifePercent",
                                    "SystemStatusFlag",
                                    "BatteryLifeTime",
                                    "BatteryFullLifeTime" };
private String[] rowValues = null;
private String[] rowComments = null;
private static final int OPB_SIZE = 512;
private long[] OPB;
private long chg0 = -1, chg1 = -1, chg2 = -1;
private boolean fchg;


public TableModelPowerStatusWindows()
    {
    if ( PowerInfo.pal.getNativeValid()==true )
        {
        OPB = new long[OPB_SIZE];
        for (int i=0; i<OPB_SIZE; i++) { OPB[i]=0; }
        rowValues = new String[rowNames.length];
        rowComments = new String[rowNames.length];
        DynamicalPowerStatus();
        }     // End of native library validity condition block
    }         // End of class constructor

@Override public int getRowCount() { return rowNames.length; }
@Override public int getColumnCount() { return colNames.length; }
@Override public String getColumnName( int column ) { return colNames[column]; }
@Override public Class getColumnClass( int column ) { return String.class; }
@Override public Object getValueAt( int row, int column )
            { switch( column )
                { case 0:  return " " + rowNames[row];
                  case 1: if ( rowValues != null && rowValues[row] != null )
                                 { return " " + rowValues[row]; }       
                            else { return " ";                  }  
                  default:  if ( rowComments != null && rowComments[row] != null )
                                 { return " " + rowComments[row]; }       
                            else { return " ";                  } } }

@Override public boolean isCellEditable( int row, int column ) { return false; }
@Override public void setValueAt( Object value, int row, int column ) {}

@Override public boolean DynamicalPowerStatus()
    {
    fchg = false;
    if ( (PowerInfo.pal.NativeAgent( null, OPB, 1, OPB_SIZE )) != 0 )
        {
        int mask8 = 0xFF;
        long mask32 = -1;  mask32 = mask32 >>>32;
        long x, y;
        double z;
        String s;
        boolean f;
        // Check state change for revisual requests
        long a = OPB[0], b = OPB[1], c = (OPB[2]) & mask32;
        if ( ( a != chg0) | ( b != chg1 ) | ( c != chg2 ) )
            { chg0 = a; chg1 = b; chg2 = c; fchg = true; } 
        // Rebuilt table text data
        if ( OPB[0] != 0 )  // Check OPB[0] = status after GetSystemPowerStatus
            {
            // ACLineStatus
            x = OPB[1];
            y = x & mask8; rowValues[0]=String.format( "%02Xh", y );
            switch( (int)y ) { case 0:   s = "Offline"; break;
                               case 1:   s = "Online"; break;
                               case 255: s = "Unknown status"; break;
                               default:  s = "Invalid"; break;
                             } rowComments[0] = s;
            // BatteryFlag
            y = ( x >> 8 ) & mask8; rowValues[1]=String.format( "%02Xh", y );
            if ( y == 255 ) { s = "Unknown status"; }
            else { f = false; s = "";
                   if ( ( y & 1 ) != 0 )   { s = s + ">66%(High)"; f = true; }
                   if ( ( y & 2 ) != 0 )   { if( f == true ) { s = s + ", "; }
                                              s = s + "<33%(Low)"; f = true; }
                   if ( ( y & 4 ) != 0 )   { if( f == true ) { s = s + ", "; }
                                          s = s + "<5%(Critical)"; f = true; }
                   if ( ( y & 8 ) != 0 )   { if( f == true ) { s = s + ", "; }
                                                 s = s + "Charging"; f=true; }
                   if ( ( y & 128 ) != 0 ) { if( f == true ) { s = s + ", "; }
                                      s = s + "No system battery"; f = true; }
                 } rowComments[1] = s;
            // Battery life percent
            y = ( x >> 16 ) & mask8; rowValues[2]=String.format( "%02Xh", y );
            if ( y == 255 ) { s = "Unknown"; }
            if ( ( y < 255 ) && ( y > 100 ) ) { s = "Invalid"; }
            if ( y <= 100 ) { s = String.format( "%d", y ); s = s + "%"; }
            rowComments[2] = s;
            // System status flag
            y = ( x >> 24 ) & mask8; rowValues[3]=String.format( "%02Xh", y );
            switch((int)y) { case 0: s = "Battery saver is off"; break;
                             case 1: s = "Battery saver on" ; break;
                             default: s = "Invalid"; break; }
            rowComments[3]=s;
            // BatteryLifeTime
            y = (x >> 32) & mask32; rowValues[4]=String.format("%d",(int)y);
            if ((int)y == -1) { s = "Unknown"; }
            if (((int)y !=-1) && ((int)y<0)) { s = "Invalid"; }
            if ((int)y>=0) { s = String.format( "%d seconds = ", y );
                             z = y; z /= 60.0;
                             s = s + String.format( "%.2f minutes", z ); }
            rowComments[4]=s;                
            // BatteryFullLifeTime
            x = OPB[2];
            y = x & mask32; rowValues[5] = String.format( "%d",(int)y );
            if ( (int)y == -1 ) { s = "Unknown"; }
            if ( ((int)y !=-1 ) && ((int)y<0)) { s = "Invalid"; }
            if ( (int)y>=0 )    { s = String.format( "%d seconds = ", y );
                                  z = y; z /= 60.0;
                                  s = s + String.format( "%.2f minutes", z ); }
            rowComments[5] = s;                
            // End of strings
       }    // End of WinAPI status condition block
    }       // End of NativeAgent status condition block
  return fchg;
  }        // End of method
}          // End of class TableModelPowerStatus


