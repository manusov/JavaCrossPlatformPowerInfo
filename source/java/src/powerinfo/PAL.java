/*
Java Power Info utility, (C)2021 IC Book Labs
Platform Abstraction Layer
*/

package powerinfo;

import java.io.*;
import java.net.URL;

public class PAL 
{
static int nativeType = -1;
final static int BLOCK_SIZE = 16384;  // 4096;  // This limit maximum lib. size
private boolean nativeValid = false;
public native int checkPAL();
public native int entryPAL( long[] a, long[] b, long c, long d );

// Methods for get native platform detection results
// NativeType: 0=Win32, 1=Win64, 2=Linux32, 3=Linux64, ... , -1=Unknown
public int getNativeType() { return nativeType; }
public boolean getNativeValid() { return nativeValid; }
// Get Power Management information, target native method
public native int NativeAgent( long[] a, long[] b, long c, long d );
// Method for load user mode library
public int loadUserModeLibrary()
    {
    String[] libNames      = 
        { "WIN32JNI" , "WIN64JNI" , "libLINUX32JNI" , "libLINUX64JNI" };
    String[] libExtensions = 
        { ".dll"     , ".dll"     , ".so"           , ".so"           };
    int status = 0;
    int count = 0;
    int i = 0;
    
    int n = libNames.length;
    int m = OSDetector.detectNative();
    nativeType = m;
            
    for (i=0; i<n; i++)
        {
        if ( i != m ) { status=-1; continue; }
        try {        
            status = 0;
            URL resource = PowerInfo.class.getResource
                ( "/powerinfo/resources/" + libNames[i] + libExtensions[i] );
            File library;
            try ( InputStream input = resource.openStream() ) {
                library = File.createTempFile( libNames[i], libExtensions[i] );
                try ( FileOutputStream output = new FileOutputStream( library ) ) {
                    byte[] buffer = new byte[BLOCK_SIZE];
                    count = 0;
                    for 
                    ( int j=input.read( buffer ); j!=-1; j=input.read( buffer ) )
                        {
                        output.write( buffer, 0, j );
                        count++;
                        }
                    }
                }
            if ( count>0     ) { System.load( library.getAbsolutePath() ); }
            if ( status == 0 ) { break; }
            }
        catch ( Throwable e )
            {
            status = -1;
            count = 0;
            }
        }
    nativeValid = status >= 0;
    return status;
    }
}
