//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Table model for Windows OS information ============================

/*
Native function GetSystemInfo results comments, see also:

https://msdn.microsoft.com/en-us/library/windows/desktop/ms724958(v=vs.85).aspx

--- Output buffer ---
  WORD      wProcessorArchitecture;
  WORD      wReserved;
  DWORD     dwPageSize;
  LPVOID    lpMinimumApplicationAddress;
  LPVOID    lpMaximumApplicationAddress;
  DWORD_PTR dwActiveProcessorMask;
  DWORD     dwNumberOfProcessors;
  DWORD     dwProcessorType;
  DWORD     dwAllocationGranularity;
  WORD      wProcessorLevel;
  WORD      wProcessorRevision;

--- wProcessorArchitecture ---

 PROCESSOR_ARCHITECTURE_AMD64 = 9 = x64 (AMD or Intel)
 PROCESSOR_ARCHITECTURE_ARM = 5 = ARM
 PROCESSOR_ARCHITECTURE_IA64 = 6 = Intel Itanium-based
 PROCESSOR_ARCHITECTURE_INTEL = 0  = x86
 PROCESSOR_ARCHITECTURE_UNKNOWN = 0xffff = Unknown architecture

--- dwProcessorType ---

 PROCESSOR_INTEL_386 (386)
 PROCESSOR_INTEL_486 (486)
 PROCESSOR_INTEL_PENTIUM (586)
 PROCESSOR_INTEL_IA64 (2200)
 PROCESSOR_AMD_X8664 (8664)
 PROCESSOR_ARM (Reserved)

--- wProcessorRevision ---

Intel Pentium, Cyrix, or NextGen 586
The high byte is the model and the low byte is the stepping.
For example, if the value is xxyy, the model number and
stepping can be displayed as follows: 
Model xx, Stepping yy
 
Intel 80386 or 80486 A value of the form xxyz. 
If xx is equal to 0xFF,
y - 0xA is the model number, and z is the stepping identifier.
If xx is not equal to 0xFF,
xx + 'A' is the stepping letter and yz is the minor stepping.
 
ARM Reserved. 

*/


package powerinfo.supportwindows;

import powerinfo.*;

public class TableModelWindows extends TableModelOs {
private String[] colNames = { "Parameter", "Value", "Comments" };
private String[] rowNames = { "wProcessorArchitecture",
                              "wReserved",
                              "dwPageSize",
                              "lpMinimumApplicationAddress",
                              "lpMaximumApplicationAddress",
                              "dwActiveProcessorMask",
                              "dwNumberOfProcessors",
                              "dwProcessorType",
                              "dwAllocationGranularity",
                              "wProcessorLevel",
                              "wProcessorRevision" };
private String[] rowValues=null;
private String[] rowComments=null;
private static final int OPB_SIZE = 512;
private long[] OPB1, OPB2;


public TableModelWindows()
    {
    if ( PowerInfo.pal.getNativeValid()==true )
        {
        OPB1 = new long[OPB_SIZE];
        OPB2 = new long[OPB_SIZE];
        for ( int i=0; i<OPB_SIZE; i++ )
            {
            OPB1[i]=0; 
            OPB2[i]=0;
            }
        int fn0=0, fn4=0;

        // Function 0 = GetSystemInfo, for Windows ia32 and Windows x64
        fn0 = PowerInfo.pal.NativeAgent( null, OPB1, 0, OPB_SIZE );
        
        // Function 4 = GetNativeSystemInfo, for WoW64 at ia32 mode
        if ( PowerInfo.pal.getNativeType()==0 )
            {
            fn4 = PowerInfo.pal.NativeAgent( null, OPB2, 4, OPB_SIZE );
            if ( (OPB2[0] & (long)0xFFFF) == 0xFFFF )
                {
                fn4 = 0;
                }
            }
        
        if ( ( fn0 != 0 ) & ( fn4 == 0 ) )
            {
            rowValues = new String[rowNames.length];
            rowComments = new String[rowNames.length];
            builtWinTable( 0, OPB1, false );
            }
        
        if ( ( fn0 != 0 ) & ( fn4 != 0 ) )
            {
            rowValues = new String[rowNames.length * 2 + 2];
            rowComments = new String[rowNames.length * 2 + 2];
            String[] x = new String[rowNames.length * 2 + 2];
            int i=0, j=0;
            for ( i=0; i<rowNames.length; i++ )
                {
                x[i] = rowNames[i];
                }
            
            x[i]=""; 
            rowValues[i]=""; 
            rowComments[i++]="";
            
            x[i]="JRE32 under Windows x64";
            rowValues[i]="detected";
            rowComments[i++]="";

            // x[i]=""; 
            // rowValues[i]=""; 
            // rowComments[i++]="";
            
            for ( j=i; j<i+rowNames.length; j++ )
                {
                x[j] = rowNames[j-i];
                }
            
            rowNames = x;
            builtWinTable( 0, OPB2, true  );
            builtWinTable( i, OPB1, false );

            }
        }
    }


//--- Helper method for twice call: Win32 and WoW64 ---

private void builtWinTable( int i, long[] OPB, boolean f32 )
    {    
    int mask8 = 0xFF;
    long mask16 = 0xFFFF;
    long mask32 = -1;  mask32 = mask32 >>>32;
    long x=0, y=0;
    int z1=0, z2=0, z3=0;
    String s;
    x = OPB[0];
    y = x & mask16; rowValues[i+0]=String.format("%d",y);
    z1=(int)y;
          
    switch (z1)
        {
        case 0:      s="x86 (32bit)"; break;
        case 5:      s="ARM";         break;
        case 6:      s="Itanium";     break;
        case 9:      s="x64";         break;
        case 0xFFFF: s="Unknown";     break;
        default:     s="?";           break;
        }
    rowComments[i+0]=s;
    if ((z1==0)|(f32))
        {
        y = (x >> 16) & mask16;   rowValues[i+1]=String.format("%d",y);
        y = (x >> 32) & mask32;   rowValues[i+2]=String.format("%d bytes",y);
        x = OPB[1];
        y = x & mask32;           rowValues[i+3]=String.format("%08Xh",y);
        y = (x >> 32) & mask32;   rowValues[i+4]=String.format("%08Xh",y);
        x = OPB[2];
        y = x & mask32;           rowValues[i+5]=String.format("%08Xh",y);
        y = (x >> 32) & mask32;   rowValues[i+6]=String.format("%d",y);
        x = OPB[3];
        y = x & mask32;           rowValues[i+7]=String.format("%d",y);
        z2 = (int)y;
        y = (x >> 32) & mask32;   rowValues[i+8]=String.format("%d bytes",y);
        x = OPB[4];
        y = x & mask16;           rowValues[i+9]=String.format("%d",y);
        y = (x >> 16) & mask16;   rowValues[i+10]=String.format("%04Xh",y);
        z3 = (int)y;
        }
    if ((z1==9)&(!f32))
        {
        y = (x >> 16) & mask16;   rowValues[i+1]=String.format("%d",y);
        y = (x >> 32) & mask32;   rowValues[i+2]=String.format("%d bytes",y);
        x = OPB[1];               rowValues[i+3]=String.format("%016Xh",x);
        x = OPB[2];               rowValues[i+4]=String.format("%016Xh",x);
        x = OPB[3];               rowValues[i+5]=String.format("%016Xh",x);
        x = OPB[4];
        y = x & mask32;           rowValues[i+6]=String.format("%d",y);
        y = (x >> 32) & mask32;   rowValues[i+7]=String.format("%d",y);
        z2 = (int)y;
        x = OPB[5];
        y = x & mask32;           rowValues[i+8]=String.format("%d bytes",y);
        y = (x >> 32) & mask16;   rowValues[i+9]=String.format("%d",y);
        y = (x >> 48) & mask16;   rowValues[i+10]=String.format("%04Xh",y);
        z3=(int)y;
        }
    if ( (z1==0) | (z1==9) )
        {
        switch (z2)
            {
            case 386:  s="386";       break;
            case 486:  s="486";       break;
            case 586:  s="PENTIUM+";  break;
            case 2200: s="IA64";      break;
            case 8664: s="x8664";     break;
            }
        rowComments[i+7]=s;
        z1 = z3 & mask8;
        z2 = (z3 >> 8) & mask8;
        rowComments[i+10]=String.format( "Model %d, Stepping %d", z2, z1 );
        }
    }

//--- End of helper method ---

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
