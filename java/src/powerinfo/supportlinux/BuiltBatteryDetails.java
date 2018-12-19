/*
Java Power Info utility, (C)2018 IC Book Labs
Built text table for battery details
*/

// Return 2-dimensional array or accept/modify 3 arrays.
// This example uses abstract classes, alternative is interface.

package powerinfo.supportlinux;

public class BuiltBatteryDetails 
{

private abstract class Parm
    {
    abstract boolean matchOsName(String s);
    abstract String getUserName(String s);
    abstract String parseValue(String s);
    }

private String extractNumberString(String s)
    {
    int m = s.length();
    String s1 = "";
    char c1 = ' ';
    for ( int i=0; i<m; i++ )
        {
        c1 = s.charAt(i);
        if (Character.isDigit(c1)) { s1=s1+c1; }
        }
    return s1;
    }

private String extractNumberValue
        ( String parsed, String units, double divisor, boolean type )
    {
    parsed = extractNumberString(parsed);
    int n = -1;        
    try { n = Integer.parseInt(parsed); } catch (Exception e) { }
    if (n<0) return "?";
    double x = n;
    if (type) { x /= divisor; parsed = String.format( "%.3f", x ); }
    else      { parsed = String.format( "%d", n ); }
    return parsed + units;
    }

private class P0 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("voltage_min_design"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
        { return extractNumberValue( s , " V" , 1000000.0 , true ); }
    }

private class P1 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("energy_now"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
//      { return extractNumberValue( s , " mWh" , 1000000.0 , true ); }
        { return extractNumberValue( s , " mWh" , 1000.0 , true ); }
    }

private class P2 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("power_now"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
//      { return extractNumberValue( s , " mW" , 1000000.0 , true ); }
        { return extractNumberValue( s , " mW" , 1000.0 , true ); }
    }

private class P3 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("energy_full_design"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
//      { return extractNumberValue( s , " mWh" , 1000000.0 , true ); }
        { return extractNumberValue( s , " mWh" , 1000.0 , true ); }
    }

private class P4 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("capacity"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
        { return extractNumberValue( s ," %" , 1.0 , false ); }
    }

private class P5 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("voltage_now"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
        { return extractNumberValue( s , " V" , 1000000.0 , true ); }
    }

private final Parm[] parmList = 
    { new P0(), new P1(), new P2(), new P3(), new P4(), new P5() };

public void parmTranslator( String[] x , String[] y , String[] z )
    {
    int n = x.length;           // n = number of strings in the table
    int m = parmList.length;    // m = number of entries in the translator
    for (int i=0; i<n; i++)
        {
        for (int j=0; j<m; j++)
            {
            if ( parmList[j].matchOsName( x[i] ) )
                {
                x[i] = parmList[j].getUserName( x[i] );
                z[i] = parmList[j].parseValue ( y[i] );
                }
            }
        }
    }

    
}
