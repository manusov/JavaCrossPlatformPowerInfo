//========== Java Power Info utility, (C)2018 IC Book Labs =====================
//========== Data model for Charging Monitor, drawings y=f(x) ==================

package powerinfo;

public class DataModelChargingMonitor {

private final String nameX1 = "Time, seconds";

private final String nameY1mWh = "Actual level, mWh" ,  // "Current level, mWh"
                     nameY1RU  = "Actual level, relative ratio units";

private final String nameX2 = nameX1;

private final String nameY2mWh = "Actual rate, mW" ,    // "Current rate, mW"
                     nameY2RU  = "Actual rate, relative ratio units";

private double dataY1[], dataY2[];
private int nX, mX;
private boolean powerUnits = false;

//--- Constructor initialize 2 arrays (current level, current rate) --
// accept size
public DataModelChargingMonitor(int a)
    {
    nX=a;                        // Graph X-size, scroll if overflowed
    mX=0;                        // Number of stored measurements
    dataY1 = new double[nX];     // Array of stored measurements, graph #1
    dataY2 = new double[nX];     // Array of stored measurements, graph #2
    for (int i=0; i<nX; i++)
        { dataY1[i]=0.0; dataY2[i]=0.0; }
    }

//--- Get X,Y names for 2 graphs: current level, current rate ---
public String getNameX1() 
    {
    return nameX1; 
    }

public String getNameY1() 
    {
    if (powerUnits) { return nameY1RU;  }
    else            { return nameY1mWh; }
    }

public String getNameX2() 
    {
    return nameX2; 
    }

public String getNameY2()
    {
    if (powerUnits) { return nameY2RU;  }
    else            { return nameY2mWh; }
    }

//--- Setup power units: mWh/mW (0) or Relative ratio units (1) ---
public void setPowerUnits(boolean b)
    {
    powerUnits = b;
    }

//--- Reserved for write array for first graph (current level, mWh) ---
public void setDataY1(double a)  
    {
    // reserved
    }

//--- Reserved for write array for second graph (current rate, mW) ---
public void setDataY2(double a)  
    {
    // reserved
    }

//--- Push one value to graph array, normally called by timer tick ---
// for first graph: current level, mWh
public void pushDataY1(double a)
    {
    if ( mX<nX )
        {
        dataY1[mX]=a;
        }
    else
        {
        int n=dataY1.length;
        for (int i=0; i<(n-1); i++) { dataY1[i]=dataY1[i+1]; }
        dataY1[n-1]=a;
        }
    }

//--- Push one value to graph array, normally called by timer tick ---
// for second graph: current rate, mW
public void pushDataY2(double a)
    {
    if ( mX<nX )
        {
        dataY2[mX]=a;
        mX++;          // For data2 only, otherwise increment 2 times per tick
        }
    else
        {
        int n=dataY2.length;
        for (int i=0; i<(n-1); i++) { dataY2[i]=dataY2[i+1]; }
        dataY2[n-1]=a;
        }
    }

//--- Gen number of already stored measurements, X-length ---
// for first graph: current level, mWh
public int getSizeX1()
    {
    return mX;    // Bug because for both models: Y1, Y2 
    }

//--- Gen number of already stored measurements, X-length ---
// for second graph: current rate, mW
public int getSizeX2()
    {
    return mX;    // Bug because for both models: Y1, Y2 
    }

//--- Get array of already stored measurements, Y-values ---
// for first graph: current level, mWh
public double[] getArrayY1()
    {
    return dataY1;
    }

//--- Get array of already stored measurements, Y-values ---
// for second graph: current rate, mW
public double[] getArrayY2()
    {
    return dataY2;
    }

//--- Clear array of already stored measurements, Y-values ---
// for first graph: current level, mWh
public void clearArrayY1()
    {
    mX=0;  // Bug because for both models: Y1,Y2
    for (int i=0; i<nX; i++) { dataY1[i]=0.0; }
    }

//--- Clear array of already stored measurements, Y-values ---
// for second graph: current rate, mW
public void clearArrayY2()
    {
    mX=0;  // Bug because for both models: Y1,Y2
    for (int i=0; i<nX; i++) { dataY2[i]=0.0; }
    }

}
