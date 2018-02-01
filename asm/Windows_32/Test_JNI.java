// IPB=null means IPB size is function code
// OPB=null means OPB size is reserved for function code extension
// if both IPB=null, OPB=null.
//-------------------------------------------------------------------


//----------------------------------------
//
//  RCX = Java Internal
//  RDX = Java Internal
//  R8  = Parameter #1 , String object
//  R9  = Parameter #2 , long 
//
//----------------------------------------


/*

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
If xx is not equal to 0xFF, xx + 'A' is the stepping letter and yz is the minor stepping.
 
ARM Reserved. 

*/



public class Test_JNI
{
//static JNIGATE jt;
   public static void main(String[] args)
   {
        // try { jt = new JNIGATE(); } catch (Exception e2) { System.exit(0); }
	JNIGATE jt = new JNIGATE();

        // int c = jt.Dump(3,7);
        // System.out.println("Return=" + c);

	// int c = jt.NativeAgent( null, null, 0, 0 );
        // System.out.println("Return=" + c);

	long[] IPB = new long[512]; for (int i=0; i<512; i++) { IPB[i]=0; }
	long[] OPB = new long[512]; for (int i=0; i<512; i++) { OPB[i]=0; }
	IPB[0]=3;
	
	//int c = jt.NativeAgent( IPB, OPB, 512, 512 );
	//int c = jt.NativeAgent( null, OPB, 0, 512 );

	//int c = jt.NativeAgent( null, OPB, 2, 2562 );
	//System.out.println();
	//for (int i=0; i<10; i++) { System.out.printf(" %d", OPB[i+140]); }
	//System.out.println();


	int c = jt.NativeAgent( null, OPB, 2, 2562 );
	System.out.println();
	
	int k=140;
	for (int j=0; j<40; j++)
		{
		for (int i=0; i<4; i++) { System.out.printf("%016Xh ", OPB[k] ); k++; }
		System.out.println();
		}

	System.out.println();


        System.out.println("Return=" + c);
	System.out.println();
        //for (int i=0; i<10; i++) { System.out.printf( "%d  %d\n", IPB[i], OPB[i] ); }


/*

	//--- Decode System Info (WIN64) ---

	long a, a1, a2;
	a2 = -1;  a2=a2>>>32;

	a = OPB[0];
	a1 = a & 0xFFFF; System.out.println(a1);
	a1 = ( a >> 16 ) & 0xFFFF; System.out.println(a1);
	a1 = ( a >> 32 ); System.out.println(a1);
	
	a = OPB[1];
	a1 = a & a2;
	System.out.printf("%08Xh\n",a1);
	a1 = ( a >> 32 ) & a2; System.out.printf("%08Xh\n",a1);
	
	a = OPB[2];
	a1 = a & a2;
	System.out.printf("%08Xh\n",a1);
	a1 = ( a >> 32 ) & a2; System.out.println(a1);
		
	a = OPB[3];
	a1 = a & a2; System.out.println(a1);
	a1 = ( a >> 32 ) &a2; System.out.println(a1);

	a = OPB[4];
	a1 =  a & 0xFFFF; System.out.println(a1);
	a1 =  a >> 16  & 0xFFFF; System.out.printf("%04Xh\n",a1);
*/

   }
}


class JNIGATE
{
   {
	boolean f=true;
	try { System.loadLibrary("WIN32JNI"); }
	catch (UnsatisfiedLinkError e1) { f=false; }

	if (f==false)
	{
	f=true;
	try { System.loadLibrary("WIN64JNI"); }
	catch (UnsatisfiedLinkError e1) { f=false; }
	}
   
	if (f==false)
	{
	System.out.println ("Error loading native library.");
	System.exit(0);
	}
   }        
	native int Dump(int a, int b);
	native int NativeAgent( long[] a, long[] b, int c, int d );

}




