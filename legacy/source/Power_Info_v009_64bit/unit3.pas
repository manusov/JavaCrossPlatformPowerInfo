unit Unit3;  { Module for support Get Battery Info by IOCTL }

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, Windows;

type

{--- IOCTL common definitions ---}

  DeviceInterfaceData = packed record
           cbSize:Dword;               // Struct. size, caller must set
           classguid:TGuid;            // Interface class GUID
           flags:DWord;                // Interface flags
           rsvd:UInt64;                // Reserved
  end;  PDevIntData = ^DeviceInterfaceData;

  DeviceInfoData = packed record
           cbSize:Dword;               // Struct. size, caller must set
           classguid:TGuid;            // Interface class GUID
           devinst:DWord;              // Handle to device instance
           rsvd:UInt64;                // Reserved
  end;  PDevInfoData = ^DeviceInfoData;

  DeviceInterfaceDetailData = packed record
           cbSize:Dword;                        // Struct. size, caller must set
           DevicePath:Array [0..1023] of Char;  // Path for use file operations
  end;  PDevIntDetData = ^DeviceInterfaceDetailData;

{--- IOCTL definitions for batteries ---}

{--- IOCTL BATTERY INFORMATION request, output buffer formats ---}
{ required fixing, yet used constant offsets }
  BATTERY_INFORMATION = packed record
           Capabilities:DWord;
           Technology:Byte;
           Reserved:Array [0..2] of Byte;
           Chemistry:Array [0..3] of Word;  // UNICODE
           DesignedCapacity:DWord;
           FullChargedCapacity:DWord;
           DefaultAlert1:DWord;
           DefaultAlert2:DWord;
           CriticalBias:DWord;
           CycleCount:DWord
  end;  P_BATTERY_INFORMATION = ^BATTERY_INFORMATION;

{--- IOCTL BATTERY INFORMATION request input buffer format ---}
  BATTERY_QUERY_INFORMATION = packed record
    BatteryTag:DWord;
    InformationLevel:DWord;  // type is BATTERY_QUERY_INFORMATION_LEVEL
    AtRate:DWord
  end;  P_BATTERY_QUERY_INFORMATION = ^BATTERY_QUERY_INFORMATION;

{--- IOCTL BATTERY STATUS request input buffer format ---}
{ input buffer for this IOCTL request }
BATTERY_WAIT_STATUS = packed record
    BatteryTag:DWord;        // tag for battery select
    Timeout:DWord;           // timeouts handling option
    PowerState:DWord;        // select state, for output data association
    LowCapacity:DWord;
    HighCapacity:DWord
  end;  P_BATTERY_WAIT_STATUS = ^BATTERY_WAIT_STATUS;

{--- IOCTL BATTERY STATUS request output buffer format ---}
{ output buffer for this IOCTL request }
  BATTERY_STATUS = packed record
    PowerState:DWord;
    Capacity:DWord;
    Voltage:DWord;
    Rate:DWord
  end;  P_BATTERY_STATUS = ^BATTERY_STATUS;

{---------- OS API used library functions declarations, as constant -----------}

function SetupDiGetClassDevsA          // Function name, "A"=ASCII, "W"=UNICODE
         ( pClassGuid: Pointer;        // Pointer to device class GUID
           Enumerator: PChar;          // Pointer to enumerator name
           hwndParent: Hwnd;           // Pointer to parent window handle
           flags:Dword                 // Control flags for return filtering
         ): Hwnd;                      // Return handle for DEVICE INFORM. SET
         stdcall; external 'setupapi.dll';

function SetupDiEnumDeviceInterfaces   // Function name
         ( hdevinfo: Hwnd;             // Handle for DEVICE INFORMATION SET
           devinfo: PChar;             // Pointer to Dev. Info enum. control
           pClassGuid: Pointer;        // Pointer to device class GUID
           idev: UInt32;               // Member index
           devint:PDevIntData          // Pointer to Device Interface Data
         ): Boolean;                   // Result flag
         stdcall; external 'setupapi.dll';

function SetupDiGetDeviceInterfaceDetailA   // Fnc.name, "A"=ASCII, "W"=UNICODE
         ( hdevinfo: Hwnd;             // Handle for DEVICE INFORMATION SET
           devint:PDevIntData;         // Pointer to Device Interface Data
           devintdet:PDevIntDetData;   // Pointer to Dev. Int. Detail Data
           devintdetsize:DWord;        // Size of Dev. Int. Detail Data
           reqsize:Pointer;            // Pointer to output dword: req. size
           devinfo:Pointer             // Pointer to output devinfo data
         ): Boolean;                   // Result flag
         stdcall; external 'setupapi.dll';


{---------- Module procedures entry points ------------------------------------}
{ arrays (not scalars) because some batteries (maximum=10) supported }

function InitIoctl
         ( var sa1: Array of String;   // Enumeration paths for combo box
           var sn2: Array of DWord;    // Battery tags as numbers
           var sa2: Array of String;   // Battery tags as strings
           var sa3: Array of String;   // Battery models as strings
           var sa4: Array of String;   // Battery manufacture name as strings
           var sn5: Array of DWord;    // Battery manufacture date as numbers
           var sa5: Array of String;   // Battery manufacture date as strings
           var sa6: Array of String;   // Battery serial number as strings
           var sa7: Array of String;   // Battery unique id as strings
           var ss8: Array of String;   // Battery chemistry, short string
           var sl8: Array of String;   // Battery chemistry, long string
           {--- Additions for v0.04 ---}
           var sn09: Array of Byte;    // Battery rechargable flag as number
           var sa09: Array of String;  // ... as string
           var sn10: Array of DWord;   // Battery designed capacity as number
           var sa10: Array of String;  // ... as string
           var sn11: Array of DWord;   // Battery full charge capacity as number
           var sa11: Array of String;  // ... as string
           var sn12: Array of DWord;   // Battery default alert #1 cap. as num.
           var sa12: Array of String;  // ... as string
           var sn13: Array of DWord;   // Battery default alert #2 cap. as num.
           var sa13: Array of String;  // ... as string
           var sn14: Array of DWord;   // Battery critical bias capac. as num.
           var sa14: Array of String;  // ... as string
           var sn15: Array of DWord;   // Battery cycle count as number
           var sa15: Array of String;  // ... as string
           var sn16: Array of DWord;   // Battery temperature as number
           var sa16: Array of String;  // ... as string
           var sn17: Array of DWord;   // Battery power state as number
           var sa17: Array of String;  // ... as string
           var sn18: Array of DWord;   // Battery current capacity as number
           var sa18: Array of String;  // ... as string
           var sn19: Array of DWord;   // Battery current voltage as number
           var sa19: Array of String;  // ... as string
           var sn20: Array of Integer; // Battery current rate (+/-) as number
           var sa20: Array of String   // ... as string
         ): Integer;

implementation

function InitIoctl ( var sa1:Array of String;
                     var sn2: Array of DWord;
                     var sa2: Array of String;
                     var sa3: Array of String;
                     var sa4: Array of String;
                     var sn5: Array of DWord;
                     var sa5: Array of String;
                     var sa6: Array of String;
                     var sa7: Array of String;
                     var ss8: Array of String;
                     var sl8: Array of String;
                     {--- Additions for v0.04 ---}
                     var sn09: Array of Byte;
                     var sa09: Array of String;
                     var sn10: Array of DWord;
                     var sa10: Array of String;
                     var sn11: Array of DWord;
                     var sa11: Array of String;
                     var sn12: Array of DWord;
                     var sa12: Array of String;
                     var sn13: Array of DWord;
                     var sa13: Array of String;
                     var sn14: Array of DWord;
                     var sa14: Array of String;
                     var sn15: Array of DWord;
                     var sa15: Array of String;
                     var sn16: Array of DWord;
                     var sa16: Array of String;
                     var sn17: Array of DWord;
                     var sa17: Array of String;
                     var sn18: Array of DWord;
                     var sa18: Array of String;
                     var sn19: Array of DWord;
                     var sa19: Array of String;
                     var sn20: Array of Integer;
                     var sa20: Array of String
                   ): Integer;

const
{--- Months for date ---}
  Months: Array [0..12] of String = ( '?',
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December' );

{--- IOCTL base constants ---}
  GUID_DEVCLASS_BATTERY:TGUID = '{72631E54-78A4-11D0-BCF7-00AA00B7B32A}';
  DIGCF_FLAGS:UInt32=$00000012;     // PRESENT=02H, DEVICEINTERFACE=10H
  BUFFER_TOO_SMALL=122;             // Error code for buffer re-allocation
{--- File IO constants ---}
  facc:UInt64 = GENERIC_READ OR GENERIC_WRITE;
  fshr:UInt64 = FILE_SHARE_READ OR FILE_SHARE_WRITE;
  fsec:UInt64 = 0;
  fcdp:UInt64 = OPEN_EXISTING;
  fatr:UInt64 = FILE_ATTRIBUTE_NORMAL;
  ftpl:UInt64 = 0;
{--- Device IO control constants ---}
  IOCTL_BATTERY_QUERY_TAG         : UInt32 = $00294040;
  IOCTL_BATTERY_QUERY_INFORMATION : UInt32 = $00294044;
  IOCTL_BATTERY_QUERY_STATUS      : UInt32 = $0029404C;
{--- Battery types decoder ---}
  BatteryTypes: Array [0..15] of String = (
   'N/A'  , 'Data is not readable',
   'PbAc' , 'Lead Acid' ,
   'LION' , 'Lithium Ion' ,
   'Li-I' , 'Lithium Ion' ,
   'NiCd' , 'Nickel Cadmium' ,
   'NiMH' , 'Nickel Metal Hydride' ,
   'NiZn' , 'Nickel Zinc' ,
   'RAM'  , 'Rechargeable Alkaline-Manganese' );
{--- Battery technologies decoder ---}
  BatteryTechnologies: Array [0..2] of String = (
  'Nonrechargeable', 'Rechargeable', 'Unknown type' );
{--- Battery states decoder ---}
  BatteryStates: Array [0..3] of String = (
  'Online', 'Discharging', 'Charging', 'Critical' );

{--- IOCTL requests codes for battery ---}
  BatteryInformation = 0;
  BatteryDeviceName = 4;
  BatteryManufactureName = 6;
  BatteryManufactureDate = 5;
  BatterySerialNumber = 8;
  BatteryUniqueId = 7;
  BatteryTemperature =2;

var
{--- Common variables ---}
  count:Integer;
  status:Boolean;
  p1,p2:Pointer;
  h1,h2:Hwnd;
  f1:Dword;
  cbRequired:UInt32;
  e1:UInt64;
{--- Device interface variables ---}
  intf1: DeviceInterfaceData =
         ( cbSize:sizeof(DeviceInterfaceData);
           classguid:'{00000000-0000-0000-0000-000000000000}';
           flags:0;
           rsvd:0 );
  p3:PDevIntData;
{--- Device info variables ---}
  info1: DeviceInfoData =
         ( cbSize:sizeof(DeviceInfoData);
           classguid:'{00000000-0000-0000-0000-000000000000}';
           devinst:0;
           rsvd:0 );
  p4:PDevInfoData;
{--- Device interface detail data ---}
  path1: DeviceInterfaceDetailData =
         // ( cbSize:sizeof(DeviceInterfaceDetailData) );
         ( cbSize:8 );  // Size of constant part only
  p5:PDevIntDetData;
  p6:Pointer;
{--- File IO variables ---}
  fpnt:Pointer;
  hBattery:THandle;
{--- Device IO control variables ---}
  InBuf:BATTERY_QUERY_INFORMATION;
  OutBuf:Array [0..1023] of Char;
  OutRet:UInt32;
  ShortInBuf:DWord;
  cbuf:Char;
  pbuf:PChar;
  pbufd:PDWord;
  ibuf:UInt32;
  ubuf:Integer;
  valid:Integer;
  ts:String;
  fbuf:Boolean;
  dbuf:double;
{--- Additions for get status request, special records used ---}
{ need optimization for use same memory and pointers for different requests }
  InWaitStatus:BATTERY_WAIT_STATUS = ( BatteryTag:0; Timeout:0; PowerState:1;
                                       LowCapacity:50; HighCapacity:100 );
  OutStatus:BATTERY_STATUS         = ( PowerState:0; Capacity:0;
                                       Voltage:0; Rate:0 );
  pstin:P_BATTERY_WAIT_STATUS;
  pstout:P_BATTERY_STATUS;

begin

{--- Initializing output data ---}
  count:=0;
{--- Get handle for access DEVICE INFORMATION SET with list of batteries ---}
  p1:=@GUID_DEVCLASS_BATTERY;
  UInt64(p2):=0;
  UInt64(h1):=0;
  UInt64(h2):=0;
  f1:=DIGCF_FLAGS;
  h1 := SetupDiGetClassDevsA ( p1, p2, h2, f1 );                         // (1)
{--- Enumerate device interfaces ---}
  if h1<>0 then
  begin
    p3:=@intf1;
    //p4:=@info1;
    p5:=@path1;
    p6:=@cbRequired;
    cbRequired:=path1.cbSize;

{---------- Start cycle for maximum 10 batteries ------------------------------}

    repeat

{--- Get path for output string and IOCTL file I/O ---}
      status := SetupDiEnumDeviceInterfaces                              // (2)
                ( h1, p2, p1, count, p3 );
      if status=false then break;  // break if error
      status := SetupDiGetDeviceInterfaceDetailA                         // (3)
                ( h1, p3, p2, 0, p6, p2 );
      if status=true then break;  // break if error: no errors with size=0
      e1:=GetLastError();
      if (e1<>BUFFER_TOO_SMALL) then break;  // break if other type of errors
      if cbRequired > 1024-8 then break;     // break if required size too big
      status := SetupDiGetDeviceInterfaceDetailA                         // (4)
                ( h1, p3, p5, cbRequired, p2, p2 );
      if status=false then break;            // break if error
      sa1[count]:=path1.DevicePath;
{--- Open device as file for IOCTL operations ---}
      fpnt:=@path1.DevicePath;
      hBattery:=0;
      hBattery := CreateFile                                             // (5)
      ( fpnt, facc, fshr, LPSECURITY_ATTRIBUTES(fsec), fcdp, fatr, ftpl );
      if hBattery <> 0 then

      begin

{--- IOCTL DeviceIoControl usage notes for Battery Info.
  Parm#1 = HANDLE: device handle returned by CreateFile function
  Parm#2 = REQUEST CODE: IOCTL_BATTERY_QUERY_INFORMATION
  Parm#3 = POINTER: pointer to input buffer
  Parm#4 = DWORD: size of input buffer
  Parm#5 = POINTER: pointer to output buffer
  Parm#6 = DWORD: size of output buffer
  Parm#7 = POINTER: pointer to dword: variable output return size
  Parm#8 = POINTER: pointer to OVERLAPPED structure for asynchronous ---}

{--- Get BATTERY TAG and store for return to caller ---}
      ShortInBuf:=0;
      OutRet:=0;
      status := DeviceIoControl                                          // (6)
      ( hBattery, IOCTL_BATTERY_QUERY_TAG,
        @ShortInBuf, sizeof(ShortInBuf),
        @InBuf.BatteryTag, sizeof(InBuf.BatteryTag),
        @OutRet, POVERLAPPED(0) );
      if ( (status=true) AND ( InBuf.BatteryTag<>0 ) ) then

{--- Begin big conditional block ---}
      begin

        sn2[count] := InBuf.BatteryTag;
        sa2[count] := ' ' + Format( '%.8Xh',[ InBuf.BatteryTag ] );
        OutRet:=0;
{--- Battery model string ---}
        InBuf.InformationLevel:=BatteryDeviceName;
        sa3[count]:=' n/a';
        status := DeviceIoControl                                        // (7)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0))  then
        begin
          pbuf:=@OutBuf;
          sa3[count]:=' ';
          valid:=0;
          for ibuf:=1 to (OutRet DIV 2) do             // DIV 2 because UNICODE
          begin
            cbuf:=pbuf^;  // transit char is redundant, for debug
            sa3[count] := sa3[count] + cbuf;
            pbuf+=2;      // +2 (not +1) because UNICODE
            if ((Byte(cbuf)<>0) AND (Byte(cbuf)<>32)) then valid:=1;
            if (Byte(cbuf)=0) then break;
          end;
          if (valid=0) then sa3[count]:=' n/a';
        end;
{--- Battery vendor string ---}
        InBuf.InformationLevel:=BatteryManufactureName;
        sa4[count]:=' n/a';
        status := DeviceIoControl                                        // (8)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0) ) then
        begin
          pbuf:=@OutBuf;
          sa4[count]:=' ';
          valid:=0;
          for ibuf:=1 to (OutRet DIV 2) do             // DIV 2 because UNICODE
          begin
            cbuf:=pbuf^;  // transit char is redundant, for debug
            sa4[count] := sa4[count] + cbuf;
            pbuf+=2;      // +2 (not +1) because UNICODE
            if ((Byte(cbuf)<>0) AND (Byte(cbuf)<>32)) then valid:=1;
            if (Byte(cbuf)=0) then break;
          end;
          if (valid=0) then sa4[count]:=' n/a';
        end;
{--- Battery manufacture date as number and as string ---}
        InBuf.InformationLevel:=BatteryManufactureDate;
        sn5[count]:=0;
        sa5[count]:=' n/a';
        status := DeviceIoControl                                        // (9)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0)) then
        begin
          pbuf  := @OutBuf;
          sa5[count]:=' ';
          pbufd := @OutBuf;
          sn5[count] := pbufd^;
          sa5[count] := ' ' + IntToStr (( pbufd^ SHR 16 ) AND $FFFF ) + ', ';
          cbuf := (pbuf+1)^;
          if UInt32(cbuf) > 12 then cbuf:=Char(0);
          sa5[count] := sa5[count] + Months[UInt32(cbuf)] + ' ';
          sa5[count] := sa5[count] + IntToStr (UInt32(pbuf^));
        end else sn5[count]:=0;
{--- Battery serial number string ---}
        InBuf.InformationLevel:=BatterySerialNumber;
        sa6[count]:=' n/a';
        status := DeviceIoControl                                        // (10)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0)) then
        begin
          pbuf:=@OutBuf;
          sa6[count]:=' ';
          valid:=0;
          for ibuf:=1 to (OutRet DIV 2) do             // DIV 2 because UNICODE
          begin
            cbuf:=pbuf^;  // transit char is redundant, for debug
            sa6[count] := sa6[count] + cbuf;
            pbuf+=2;      // +2 (not +1) because UNICODE
            if ((Byte(cbuf)<>0) AND (Byte(cbuf)<>32)) then valid:=1;
            if (Byte(cbuf)=0) then break;
          end;
          if (valid=0) then sa6[count]:=' n/a';
        end;
{--- Battery unique id string ---}
        InBuf.InformationLevel:=BatterySerialNumber;
        sa7[count]:=' n/a';
        status := DeviceIoControl                                        // (11)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0)) then
        begin
          pbuf:=@OutBuf;
          sa7[count]:=' ';
          valid:=0;
          for ibuf:=1 to (OutRet DIV 2) do             // DIV 2 because UNICODE
          begin
            cbuf:=pbuf^;  // transit char is redundant, for debug
            sa7[count] := sa7[count] + cbuf;
            pbuf+=2;      // +2 (not +1) because UNICODE
            if ((Byte(cbuf)<>0) AND (Byte(cbuf)<>32)) then valid:=1;
            if (Byte(cbuf)=0) then break;
          end;
          if (valid=0) then sa7[count]:=' n/a';
        end;
{--- Battery chemistry, as part of battery info ---}
        InBuf.InformationLevel:=BatteryInformation;
        ss8[count]:=' ';
        sl8[count]:=' n/a';
        ts:='';
        status := DeviceIoControl                                        // (12)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0)) then
        begin
          pbuf:=@OutBuf;
          pbuf+=8;
          for ibuf:=0 to 3 do
          begin
            cbuf:=pbuf^;
            if Byte(cbuf)=0 then break;
            ss8[count]:=ss8[count]+cbuf;
            ts:=ts+cbuf;
            pbuf+=1;
          end;
          for ibuf:=0 to (length(BatteryTypes) DIV 2)-1 do
          begin
            if ( AnsiCompareText(ts, BatteryTypes[ibuf*2]) ) = 0 then
            begin
              sl8[count] := ' ' + BatteryTypes [ibuf*2+1];
            end;
          end;
        end;
{--- Battery technology, rechargable flag, use previous read IOCTL data ---}
        pbuf:=@OutBuf;
        ibuf:=(PByte(pbuf+4))^;
        sn09[count] := ibuf;
        if (ibuf>2) then ibuf:=2;
        sa09[count] := ' ' + BatteryTechnologies[ibuf];
{--- Designed capacity ---}
        if ( ( ((PDWord(pbuf))^) AND $40000000 )<>0 )
        then fbuf:=true else fbuf:=false;
        ibuf:=(PDWord(pbuf+12))^;
        sn10[count] := ibuf;
        sa10[count] := ' n/a';
        if (ibuf<>0) then
        begin
          if (fbuf=false) then sa10[count] := ' ' + IntToStr(ibuf) + ' mWh'
          else sa10[count] := ' ' + IntToStr(ibuf) + ' relative ratio units';
        end;
        if ( (ibuf<=100) AND (ibuf<>0) ) then
        sa10[count] := ' Unknown parameter encoding';
{--- Full-charged capacity ---}
        ibuf:=(PDWord(pbuf+16))^;
        sn11[count] := ibuf;
        sa11[count] := ' n/a';
        if (ibuf<>0) then
        begin
          if (fbuf=false) then sa11[count] := ' ' + IntToStr(ibuf) + ' mWh'
          else sa11[count] := ' ' + IntToStr(ibuf) + ' relative ratio units';
        end;
        if ( (ibuf<=100) AND (ibuf<>0) ) then
        sa11[count] := ' Unknown parameter encoding';
{--- Default alert #1 capacity ---}
        ibuf:=(PDWord(pbuf+20))^;
        sn12[count] := ibuf;
        sa12[count] := ' n/a';
        if (ibuf<>0) then
        begin
          if (fbuf=false) then sa12[count] := ' ' + IntToStr(ibuf) + ' mWh'
          else sa12[count] := ' ' + IntToStr(ibuf) + ' relative ratio units';
        end;
        // if ( (ibuf<=100) AND (ibuf<>0) ) then
        // sa12[count] := ' Unknown parameter encoding';
{--- Default alert #2 capacity ---}
        ibuf:=(PDWord(pbuf+24))^;
        sn13[count] := ibuf;
        sa13[count] := ' n/a';
        if (ibuf<>0) then
        begin
          if (fbuf=false) then sa13[count] := ' ' + IntToStr(ibuf) + ' mWh'
          else sa13[count] := ' ' + IntToStr(ibuf) + ' relative ratio units';
        end;
        // if ( (ibuf<=100) AND (ibuf<>0) ) then
        // sa13[count] := ' Unknown parameter encoding';
{--- Critical bias ---}
        ibuf:=(PDWord(pbuf+28))^;
        sn14[count] := ibuf;
        sa14[count] := ' n/a';
        if (ibuf<>0) then
        begin
          if (fbuf=false) then sa14[count] := ' ' + IntToStr(ibuf) + ' mWh'
          else sa14[count] := ' ' + IntToStr(ibuf) + ' relative ratio units';
        end;
        // if ( (ibuf<=100) AND (ibuf<>0) ) then
        // sa14[count] := ' Unknown parameter encoding';
{--- Cycle count ---}
        ibuf:=(PDWord(pbuf+32))^;
        sn15[count] := ibuf;
        sa15[count] := ' n/a';
        if (ibuf<>0) then sa15[count] := ' ' + IntToStr(ibuf) + ' cycles';
{--- Battery temperture string ---}
        InBuf.InformationLevel:=BatteryTemperature;
        sn16[count]:=0;
        sa16[count]:=' n/a';
        status := DeviceIoControl                                        // (13)
        ( hBattery, IOCTL_BATTERY_QUERY_INFORMATION,
          @InBuf, sizeof(InBuf), @OutBuf, sizeof(OutBuf),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0)) then
        begin
          pbuf := @OutBuf;
          ibuf := (PDWord(pbuf))^;
          dbuf := ibuf;
          dbuf := dbuf*10 - 273.15;
          sn16[count] := ibuf;
          if (ibuf<>0) then
          sa16[count] := ' ' + Format( '%.1F',[ dbuf ] ) + ' C';
          if ( (dbuf<-100.0) OR (dbuf>150.0) ) then
          sa16[count] := ' Unknown parameter encoding';
        end;
{--- Battery status read, for next 4 parameters ---}
        sn17[count]:=0; sn18[count]:=0; sn19[count]:=0; sn20[count]:=0;
        ts:=' n/a';
        sa17[count]:=ts; sa18[count]:=ts; sa19[count]:=ts; sa20[count]:=ts;
        pstin  := @InWaitStatus;
        pstout := @OutStatus;
        pstin^.BatteryTag := InBuf.BatteryTag;
        status := DeviceIoControl                                        // (14)
        ( hBattery, IOCTL_BATTERY_QUERY_STATUS,
          pstin, sizeof(InWaitStatus), pstout, sizeof(OutStatus),
          @OutRet, POVERLAPPED(0) );
        if ((status=true) AND (OutRet>0)) then
        begin
{--- Status field 1 of 4, power status ---}
        ibuf := pstout^.PowerState;
        sn17[count] := ibuf;
        ts := ' ';
        if (ibuf AND $00000001) <> 0 then
        begin
          ts := ts + BatteryStates[0]
        end;
        if (ibuf AND $00000002) <> 0 then
        begin
          if (length(ts)>1) then ts+=' , ';
          ts := ts + BatteryStates[1]
        end;
        if (ibuf AND $00000004) <> 0 then
        begin
          if (length(ts)>1) then ts+=' , ';
          ts := ts + BatteryStates[2]
        end;
        if (ibuf AND $00000008) <> 0 then
        begin
          if (length(ts)>1) then ts+=' , ';
          ts := ts + BatteryStates[3]
        end;
        sa17[count] := ts;
{--- Status field 2 of 4, capacity ---}
        ibuf := pstout^.Capacity;
        sn18[count] := ibuf;
        sa18[count] := ' n/a';
        if (ibuf<>0) then
        begin
          if (fbuf=false) then sa18[count] := ' ' + IntToStr(ibuf) + ' mWh'
          else sa18[count] := ' ' + IntToStr(ibuf) + ' relative ratio units';
        end;
        if ( (ibuf<=100) AND (ibuf<>0) ) then
        sa18[count] := ' Unknown parameter encoding';
{--- Status field 3 of 4, voltage ---}
       ibuf := pstout^.Voltage;
       sn19[count] := ibuf;
       sa19[count] := ' n/a';
       dbuf := ibuf;
       dbuf := dbuf/1000.0;
       if (ibuf<>0) then
       begin
         sa19[count] := Format( ' %.3F',[ dbuf ] ) + ' volts';
         if (dbuf<5.0) OR (dbuf>50.0) then
         sa19[count] := ' Unknown parameter encoding';
       end;
{--- Status field 4 of 4, rate ---}
       ubuf := pstout^.Rate;
       sn20[count] := ubuf;
       sa20[count] := ' n/a';
       if (ubuf<>0) then
       begin
         sa20[count] := ' ' + IntToStr(ubuf) + ' mW';
         if ( ubuf > 1000000000 ) OR ( ubuf < -1000000000 ) then
         sa20[count] := ' Unknown parameter encoding';
       end;

{--- End of status request conditional section ---}
       end;

{--- End of tag-conditional section ---}
      end;

{--- Close current battery handle ---}
      CloseHandle(hBattery);
      end;

{--- Make cycle for maximum 10 batteries ---}
    count+=1;
    until (count>9);

{---------- End cycle for maximum 10 batteries --------------------------------}

  end;

{--- Return ---}
  InitIoctl:=count;
end;


end.

