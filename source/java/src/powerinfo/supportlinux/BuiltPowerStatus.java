/*
Java Power Info utility, (C)2021 IC Book Labs
Built text table for AC power status
*/

// Return 2-dimensional array or accept/modify 3 arrays.
// This example uses abstract classes, alternative is interface.

package powerinfo.supportlinux;

public class BuiltPowerStatus 
{

private abstract class Parm
    {
    abstract boolean matchOsName( String s );
    abstract String getUserName( String s );
    abstract String parseValue( String s );
    }

private String extractNumberString( String s )
    {
    int m = s.length();
    String s1 = "";
    char c1;
    for ( int i=0; i<m; i++ )
        {
        c1 = s.charAt(i);
        if ( Character.isDigit( c1 ) ) { s1 = s1 + c1; }
        }
    return s1;
    }

private class P0 extends Parm
    {
    @Override boolean matchOsName(String s)   
        { return s.equals("online"); }
    @Override String getUserName(String s) 
        { return s; }
    @Override String parseValue(String s) 
        { 
        s = extractNumberString(s);
        int n = -1;        
        try { n = Integer.parseInt(s); } catch ( Exception e ) { }
        if      ( n == 0 ) return "AC power offline";
        else if ( n == 1 ) return "AC power online";
        return "?";
        }
    }

private final Parm[] parmList = { new P0() };

public void parmTranslator( String[] x , String[] y , String[] z )
    {
    int n = x.length;           // n = number of strings in the table
    int m = parmList.length;    // m = number of entries in the translator
    for ( int i=0; i<n; i++ )
        {
        for ( int j=0; j<m; j++ )
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
