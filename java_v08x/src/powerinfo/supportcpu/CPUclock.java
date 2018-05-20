/*
Java Power Info utility, (C)2018 IC Book Labs
CPU clock measurement and control
*/

package powerinfo.supportcpu;

import powerinfo.*;

public class CPUclock 
{
private final long[] IPB, OPB;
private final int IPB_SIZE = 4096, OPB_SIZE = 4096;

public CPUclock()
    {
    IPB = new long[IPB_SIZE];
    OPB = new long[OPB_SIZE];
    for ( int i=0; i<IPB_SIZE; i++ ) { IPB[i]=0; }
    for ( int i=0; i<OPB_SIZE; i++ ) { OPB[i]=0; }
    }

public long measureTSC()
    {
    if ( (PowerInfo.pal.NativeAgent( null, OPB, 3, OPB_SIZE )) != 0 )
        {
        return OPB[0];
        }
    else
        {
        return 0;
        }
    }
    
}
