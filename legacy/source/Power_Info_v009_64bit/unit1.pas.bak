unit Unit1;  { Main module }

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, FileUtil, TAGraph, TASeries, Forms, Controls,
  Graphics, Dialogs, Grids, StdCtrls, ExtCtrls, ComCtrls,
  Unit2, Unit3;

type

  { TForm1 }

  TForm1 = class(TForm)
    Button1: TButton;
    Chart1: TChart;
    SeriesLevel: TLineSeries;
    Chart2: TChart;
    SeriesRate: TLineSeries;
    ComboBox1: TComboBox;
    Label1: TLabel;
    Label2: TLabel;
    Label3: TLabel;
    Label4: TLabel;
    Label5: TLabel;
    PageControl1: TPageControl;
    StringGrid1: TStringGrid;
    StringGrid2: TStringGrid;
    TabSheet1: TTabSheet;
    TabSheet2: TTabSheet;
    TabSheet3: TTabSheet;
    Timer1: TTimer;
    procedure AppStart(Sender: TObject);
    procedure ChangeSelection(Sender: TObject);
    procedure RunMonitor(Sender: TObject);
    procedure TimerTicks(Sender: TObject);
  private
    { private declarations }
  public
    { public declarations }
  end;

var
{--- User interface objects ---}
  Form1: TForm1;
  sg1,sg2:TStringGrid;
  cb1:TComboBox;
  ReadyForTick:Boolean=false;
{--- Power status parameters, numbers ---}
  sy1,sy2,sy3,sy4:Byte; sy5,sy6:DWord;
{--- Battery parameters, count and strings ---}
  BatteryCount:       Integer;
  BatteryNames:       Array [0..9] of String;
  BatteryTags:        Array [0..9] of DWord;
  BatteryTagsStrings: Array [0..9] of String;
  BatteryModels:      Array [0..9] of String;
  BatteryVendors:     Array [0..9] of String;
  BatteryDatesNum:    Array [0..9] of DWord;
  BatteryDatesStr:    Array [0..9] of String;
  BatterySerialNum:   Array [0..9] of String;
  BatteryUniqueId:    Array [0..9] of String;
  BatteryChemistry1:  Array [0..9] of String;
  BatteryChemistry2:  Array [0..9] of String;
{--- Additions for v0.04 ---}
  BatRechargableNum:  Array [0..9] of Byte;
  BatRechargableStr:  Array [0..9] of String;
  BatDesignedCapNum:  Array [0..9] of DWord;
  BatDesignedCapStr:  Array [0..9] of String;
  BatFChargeCapNum:   Array [0..9] of DWord;
  BatFChargeCapStr:   Array [0..9] of String;
  BatDefAlrt1CapNum:  Array [0..9] of DWord;
  BatDefAlrt1CapStr:  Array [0..9] of String;
  BatDefAlrt2CapNum:  Array [0..9] of DWord;
  BatDefAlrt2CapStr:  Array [0..9] of String;
  BatCritBiasNum:     Array [0..9] of DWord;
  BatCritBiasStr:     Array [0..9] of String;
  BatCycleCountNum:   Array [0..9] of DWord;
  BatCycleCountStr:   Array [0..9] of String;
  BatTemperatureNum:  Array [0..9] of DWord;
  BatTemperatureStr:  Array [0..9] of String;
  BatPowerStateNum:   Array [0..9] of DWord;
  BatPowerStateStr:   Array [0..9] of String;
  BatCapacityNum:     Array [0..9] of DWord;
  BatCapacityStr:     Array [0..9] of String;
  BatVoltageNum:      Array [0..9] of DWord;
  BatVoltageStr:      Array [0..9] of String;
  BatRateNum:         Array [0..9] of Integer;   // Signed
  BatRateStr:         Array [0..9] of String;
{--- Additions for v0.08, Charging monitor support ---}
  gen:Boolean=false;                     // Global enable flag, also for ticks
  gi:Integer;                            // Global index, also for timer ticks
  gx,gy:Double;                          // Global x,y coord., also for ticks
  GN,GMIN,GMAX:Integer;                  // Length, Min, Max
  gyy1,gyy2: Array [0..7200] of Double;  // Array for scroll with redraw
  tc1,tc2:TChart;                        // Charts


implementation

{$R *.lfm}

{ TForm1 }

{---------- Helper procedure: visual parameters at Power Status table ---------}
procedure RevisualPowerStatus (sg1:TStringGrid; y1,y2,y3,y4:Byte; y5,y6:Dword);
const
  ACLineStatus: Array [0..2] of String =
  ( 'Offline', 'Online', 'Unknown status' );
  BatteryFlag: Array [0..5] of String =
  ( 'High>66%', 'Low<33%', 'Critical<5%', 'Charging',
  'No system battery', 'Unknown status' );
  SystemStatusFlag: Array [0..2] of String =
  ( 'Battery saver is OFF', 'Battery saver ON', 'Unknown status' );
var
  s1:String;
begin
  {--- Begin update Power Status table: parameters values ---}
  sg1.Cells[2,1]:= ' ' + IntToStr(ShortInt(y1));
  sg1.Cells[2,2]:= ' ' + Format('%.2Xh',[ y2 ]);

  { patch v0.07 }
  if y3<101 then sg1.Cells[2,3]:= ' ' + IntToStr(ShortInt(y3)) + '%'
  else begin
  sg1.Cells[2,3]:= ' ' + IntToStr(ShortInt(y3));
  sg1.Cells[3,3]:= ' n/a';
  end;

  sg1.Cells[2,4]:= ' ' + IntToStr(ShortInt(y4));
  sg1.Cells[2,5]:= ' ' + IntToStr(Integer(y5));
  sg1.Cells[2,6]:= ' ' + IntToStr(Integer(y6));
{--- Continue update Power Status table: parameters comments ---}
{--- AC Line Status ---}
  if (y1>2) then y1:=2;
  sg1.Cells[3,1]:= ' ' + ACLineStatus[y1];
{--- Battery Flag ---}
  s1 := ' ';
  if (y2=255) then s1 += BatteryFlag[5]  else        // Unknown status (255)
  begin
    if (y2 AND $01 <> 0) then s1 += BatteryFlag[0];  // High (bit 0)
    if (y2 AND $02 <> 0) then          // Low (bit 1)
    begin
      if (length(s1)>1) then s1+=' , ';
      s1 += BatteryFlag[1];
    end;
    if (y2 AND $04 <> 0) then          // Critical (bit 2)
    begin
      if (length(s1)>1) then s1+=' , ';
      s1 += BatteryFlag[2];
    end;
    if (y2 AND $08 <> 0) then          // Charging (bit 3)
    begin
      if (length(s1)>1) then s1+=' , ';
      s1 += BatteryFlag[3];
    end;
    if (y2 AND $80 <> 0) then          // No system battery (bit 7)
    begin
      if (length(s1)>1) then s1+=' , ';
      s1 += BatteryFlag[4];
    end;
  end;
{--- Write complex string for battery flag ---}
  sg1.Cells[3,2]:= s1;
{--- Battery saver flag, system status ---}
  if (y4>1) then y4:=2;
  sg1.Cells[3,4]:=' '+SystemStatusFlag[y4];
{--- Battery Life Time ---}
  if ((y5 AND $80000000)=0) then
  begin
    sg1.Cells[2,5]:=sg1.Cells[2,5]+' seconds';
    sg1.Cells[3,5]:=FormatFloat(' 0.0 minutes', double(y5)/60 );
  end else sg1.Cells[3,5]:=' Remaining time is unknown';
{--- Battery Full Life Time ---}
  if ((y6 AND $80000000)=0) then
  begin
    sg1.Cells[2,6]:=sg1.Cells[2,6]+' seconds';
    sg1.Cells[3,6]:=FormatFloat(' 0.0 minutes', double(y6)/60 );
  end else sg1.Cells[3,6]:=' Full-charge time is unknown';

end;

{---------- Helper procedure: visual parameters at Battery Details table ------}
procedure RevisualBatteryDetails ( i:         Integer;
                                   sg1:       TStringGrid;
                                   stagnums:  Array of DWord;
                                   stagstrs:  Array of String;
                                   modelstrs: Array of String;
                                   vendstrs:  Array of String;
                                   datenums:  Array of DWord;
                                   datestrs:  Array of String;
                                   snumstrs:  Array of String;
                                   uniqstrs:  Array of String;
                                   chm1strs:  Array of String;
                                   chm2strs:  Array of String;
                                   {--- Additions for v0.04 ---}
                                   rechnums:  Array of Byte;
                                   rechstrs:  Array of String;
                                   dcapnums:  Array of DWord;
                                   dcapstrs:  Array of String;
                                   fchrnums:  Array of DWord;
                                   fchrstrs:  Array of String;
                                   alr1nums:  Array of DWord;
                                   alr1strs:  Array of String;
                                   alr2nums:  Array of DWord;
                                   alr2strs:  Array of String;
                                   cbianums:  Array of DWord;
                                   cbiastrs:  Array of String;
                                   ccntnums:  Array of DWord;
                                   ccntstrs:  Array of String;
                                   tempnums:  Array of DWord;
                                   tempstrs:  Array of String;
                                   psttnums:  Array of DWord;
                                   psttstrs:  Array of String;
                                   bcapnums:  Array of DWord;
                                   bcapstrs:  Array of String;
                                   voltnums:  Array of DWord;
                                   voltstrs:  Array of String;
                                   ratenums:  Array of Integer;
                                   ratestrs:  Array of String );

begin
  sg1.Cells[2,1] := stagstrs[i];
  sg1.Cells[3,2] := modelstrs[i];
  sg1.Cells[3,3] := vendstrs[i];
  sg1.Cells[2,4] := ' ' + Format( '%.8Xh',[ datenums[i] ] );
  sg1.Cells[3,4] := datestrs[i];
  sg1.Cells[3,5] := snumstrs[i];
  sg1.Cells[3,6] := uniqstrs[i];
  sg1.Cells[2,7] := chm1strs[i];
  sg1.Cells[3,7] := chm2strs[i];
{--- Additions for v0.04 ---}
  sg1.Cells[2,8]  := ' ' + Format( '%.2Xh',[ rechnums[i] ] );
  sg1.Cells[3,8]  := rechstrs[i];
  sg1.Cells[2,9]  := ' ' + Format( '%.8Xh',[ dcapnums[i] ] );
  sg1.Cells[3,9]  := dcapstrs[i];
  sg1.Cells[2,10] := ' ' + Format( '%.8Xh',[ fchrnums[i] ] );
  sg1.Cells[3,10] := fchrstrs[i];
  sg1.Cells[2,11] := ' ' + Format( '%.8Xh',[ alr1nums[i] ] );
  sg1.Cells[3,11] := alr1strs[i];
  sg1.Cells[2,12] := ' ' + Format( '%.8Xh',[ alr2nums[i] ] );
  sg1.Cells[3,12] := alr2strs[i];
  sg1.Cells[2,13] := ' ' + Format( '%.8Xh',[ cbianums[i] ] );
  sg1.Cells[3,13] := cbiastrs[i];
  sg1.Cells[2,14] := ' ' + Format( '%.8Xh',[ ccntnums[i] ] );
  sg1.Cells[3,14] := ccntstrs[i];
  sg1.Cells[2,15] := ' ' + Format( '%.8Xh',[ tempnums[i] ] );
  sg1.Cells[3,15] := tempstrs[i];
  sg1.Cells[2,16] := ' ' + Format( '%.8Xh',[ psttnums[i] ] );
  sg1.Cells[3,16] := psttstrs[i];
  sg1.Cells[2,17] := ' ' + Format( '%.8Xh',[ bcapnums[i] ] );
  sg1.Cells[3,17] := bcapstrs[i];
  sg1.Cells[2,18] := ' ' + Format( '%.8Xh',[ voltnums[i] ] );
  sg1.Cells[3,18] := voltstrs[i];
  sg1.Cells[2,19] := ' ' + Format( '%.8Xh',[ ratenums[i] ] );
  sg1.Cells[3,19] := ratestrs[i];


end;

{---------- Start application -------------------------------------------------}

procedure TForm1.AppStart(Sender: TObject);

const colnames: Array [0..3] of String =
      ( '#','Parameter', 'Value', 'Comments' );
      rownames1: Array [1..6] of String =
      ( 'ACLineStatus', 'BatteryFlag', 'BatteryLifePercent',
        'SystemStatusFlag', 'BatteryLifeTime', 'BatteryFullLifeTime' );
      rownames2: Array [1..19] of String =
      ( 'Battery tag', 'Battery model', 'Battery vendor',
        'Manufacture date', 'Serial number', 'Unique ID', 'Battery chemistry',
        'Battery technology flag',
        'Battery designed capacity', 'Battery full charged capacity',
        'Default alert level 1' , 'Default alert level 2',
        'Critical bias', 'Cycle count', 'Battery temperature',
        'Current power state', 'Current capacity',
        'Current voltage', 'Current rate' );

var i:Integer;
    x1:Double;
    y1,y2,y3,y4:Byte; y5,y6:Dword;

begin

{---------- Global settings ---------------------------------------------------}

  DecimalSeparator:='.';
  sy1:=0; sy2:=0; sy3:=0; sy4:=0; sy5:=0; sy6:=0;
  gen:=false;

{---------- (1) Power Status table string grid --------------------------------}

{--- Initializing variables ---}
  sg1:=StringGrid1;
  sg1.Clear;
  sg1.RowCount:=7+20;
  sg1.ColCount:=4;
  sg1.ColWidths[0]:=40;
{--- Set columns widths ---}
  for i:=1 to sg1.ColCount-1 do
  begin
    sg1.ColWidths[i]:=
    (sg1.ClientWidth-sg1.ColWidths[0]-20) DIV (sg1.ColCount-1);
  end;
  sg1.ColWidths[2] := sg1.ColWidths[2] - 70;
  sg1.ColWidths[3] := sg1.ColWidths[3] + 70;
{--- Write rows numbers ---}
  for i:=1 to sg1.RowCount-1 do
  begin
    sg1.Cells[0,i]:='  '+IntToStr(i);
  end;
{--- Write columns names ---}
  for i:=0 to sg1.ColCount-1 do
  begin
    sg1.Cells[i,0]:=' '+colnames[i];
  end;
{--- Write rows (parameters) names ---}
  for i:=1 to sg1.RowCount-1-20 do
  begin
    sg1.Cells[1,i]:=' '+rownames1[i];
  end;
{--- Call API, output error message and exit if API return error ---}
  if (GetPower(y1,y2,y3,y4,y5,y6)=false) then
  begin
    MessageDlg('API GetSystemPowerStatus failed', mtError, [mbOk], 0);
    halt;
  end;
{--- Visual parameters at Power Status table ---}
  RevisualPowerStatus(sg1,y1,y2,y3,y4,y5,y6);

{---------- (2) Battery select combo box --------------------------------------}

{--- Initializing variables ---}
  cb1:=ComboBox1;
  cb1.Clear;
{--- Call API ---}
  BatteryCount := InitIoctl ( BatteryNames,
                    BatteryTags, BatteryTagsStrings,
                    BatteryModels, BatteryVendors,
                    BatteryDatesNum, BatteryDatesStr,
                    BatterySerialNum, BatteryUniqueId,
                    BatteryChemistry1, BatteryChemistry2,
    {--- Additions for v0.04 ---}
                    BatRechargableNum, BatRechargableStr,
                    BatDesignedCapNum, BatDesignedCapStr,
                    BatFChargeCapNum,  BatFChargeCapStr,
                    BatDefAlrt1CapNum, BatDefAlrt1CapStr,
                    BatDefAlrt2CapNum, BatDefAlrt2CapStr,
                    BatCritBiasNum,    BatCritBiasStr,
    {--- This 6 pairs of parameters is argument for dynamical visual ---}
                    BatCycleCountNum,  BatCycleCountStr,
                    BatTemperatureNum, BatTemperatureStr,
                    BatPowerStateNum,  BatPowerStateStr,
                    BatCapacityNum,    BatCapacityStr,
                    BatVoltageNum,     BatVoltageStr,
                    BatRateNum,        BatRateStr );

  if (BatteryCount>0) then
  begin
{--- Enable combo box and built list, if open IOCTL OK ---}
    cb1.Enabled:=true;
    cb1.Clear;
    for i:=0 to BatteryCount-1 do
    begin
      cb1.Items.Add(' ' + BatteryNames[i]);
      cb1.ItemIndex:=0;
    end;

  end;

{---------- (3) Battery details table string grid -----------------------------}

{--- Initializing variables ---}
  sg2:=StringGrid2;
  sg2.Clear;
  sg2.RowCount:=20+5;
  sg2.ColCount:=4;
  sg2.ColWidths[0]:=40;
{--- Set columns widths ---}
  for i:=1 to sg2.ColCount-1 do
  begin
    sg2.ColWidths[i]:=
    (sg2.ClientWidth-sg2.ColWidths[0]-20) DIV (sg2.ColCount-1);
  end;
  sg2.ColWidths[2] := sg2.ColWidths[2] - 70;
  sg2.ColWidths[3] := sg2.ColWidths[3] + 70;
{--- Write rows numbers ---}
  for i:=1 to sg2.RowCount-1 do
  begin
    sg2.Cells[0,i]:='  '+IntToStr(i);
  end;
{--- Write columns names ---}
  for i:=0 to sg2.ColCount-1 do
  begin
    sg2.Cells[i,0]:=' '+colnames[i];
  end;
  {--- Write rows (parameters) names ---}
  for i:=1 to 19 do
  begin
    sg2.Cells[1,i]:=' '+rownames2[i];
  end;
{--- Revisual details if data valid ---}
  if BatteryCount > 0 then
  begin

    RevisualBatteryDetails ( cb1.ItemIndex, sg2,
                             BatteryTags,       BatteryTagsStrings,
                             BatteryModels,     BatteryVendors,
                             BatteryDatesNum,   BatteryDatesStr,
                             BatterySerialNum,  BatteryUniqueId,
                             BatteryChemistry1, BatteryChemistry2,
                             {--- Additions for v0.04 ---}
                             BatRechargableNum, BatRechargableStr,
                             BatDesignedCapNum, BatDesignedCapStr,
                             BatFChargeCapNum,  BatFChargeCapStr,
                             BatDefAlrt1CapNum, BatDefAlrt1CapStr,
                             BatDefAlrt2CapNum, BatDefAlrt2CapStr,
                             BatCritBiasNum,    BatCritBiasStr,
                             BatCycleCountNum,  BatCycleCountStr,
                             BatTemperatureNum, BatTemperatureStr,
                             BatPowerStateNum,  BatPowerStateStr,
                             BatCapacityNum,    BatCapacityStr,
                             BatVoltageNum,     BatVoltageStr,
                             BatRateNum,        BatRateStr );

{--- Enable charging monitor ---}
    Chart1.Enabled:=true;
    Chart2.Enabled:=true;
    Button1.Enabled:=true;

  end else
  begin
    sg2.Clean;
    sg2.Enabled:=false;
  end;

{---------- End of built GUI components ---------------------------------------}

  ReadyForTick:=true;

end;

{---------- Selection change handler for combo box, revisual details table ----}

procedure TForm1.ChangeSelection(Sender: TObject);
begin

  RevisualBatteryDetails ( cb1.ItemIndex, sg2,
                           BatteryTags,       BatteryTagsStrings,
                           BatteryModels,     BatteryVendors,
                           BatteryDatesNum,   BatteryDatesStr,
                           BatterySerialNum,  BatteryUniqueId,
                           BatteryChemistry1, BatteryChemistry2,
                           {--- Additions for v0.04 ---}
                           BatRechargableNum, BatRechargableStr,
                           BatDesignedCapNum, BatDesignedCapStr,
                           BatFChargeCapNum,  BatFChargeCapStr,
                           BatDefAlrt1CapNum, BatDefAlrt1CapStr,
                           BatDefAlrt2CapNum, BatDefAlrt2CapStr,
                           BatCritBiasNum,    BatCritBiasStr,
                           BatCycleCountNum,  BatCycleCountStr,
                           BatTemperatureNum, BatTemperatureStr,
                           BatPowerStateNum,  BatPowerStateStr,
                           BatCapacityNum,    BatCapacityStr,
                           BatVoltageNum,     BatVoltageStr,
                           BatRateNum,        BatRateStr );

end;

{---------- Start/Stop button for Charging monitor ----------------------------}
{ must not ticks handing for charger monitor without this fragment executed }

procedure TForm1.RunMonitor(Sender: TObject);
begin
  GMIN:=0;
  GMAX:=1800;    // Maximim by array size = 3600 seconds = 60 minutes = 1 hour
  GN:=3600;      // Maximum by array size = 7200 intervals 0.5 seconds
  gi:=0;
  gen := NOT gen;
  if gen=true then
  begin
  Button1.Caption:='Stop';
  SeriesLevel.Clear;
  SeriesRate.Clear;
  end else Button1.Caption:='Start';
end;


{---------- Timer ticks handler for revisual ----------------------------------}
{ Yet bug #1 : hot swap not supported }
{ Yet bug #2 : first timer tick must be after all objects initialized }
{ #2 fixed by ReadyForTick }

procedure TForm1.TimerTicks(Sender: TObject);
var
{--- Variables for power status revisual required check---}
  y1,y2,y3,y4:Byte; y5,y6:Dword;
{--- Variables for battery details revisual required check---}
  i,x1:Integer;
  schg, bchg:Boolean;
  Shadow01: Array [0..9] of DWord;
  Shadow02: Array [0..9] of DWord;
  Shadow03: Array [0..9] of DWord;
  Shadow04: Array [0..9] of DWord;
  Shadow05: Array [0..9] of DWord;
  Shadow06: Array [0..9] of DWord;
const
  bnum:Integer=10;

begin

{--- Check for referenced objects initialized by main thread ---}

  if ReadyForTick=true then
  begin

{---------- Support power status, revisual if valid and changed ---------------}

  schg := GetPower(y1,y2,y3,y4,y5,y6);
  if ( schg=true ) AND
     ( (y1<>sy1)OR(y2<>sy2)OR(y3<>sy3)OR(y4<>sy4)OR(y5<>sy5)OR(y6<>sy6) )
  then
  begin
    RevisualPowerStatus(sg1,y1,y2,y3,y4,y5,y6);
    sy1:=y1; sy2:=y2; sy3:=y3; sy4:=y4; sy5:=y5; sy6:=y6;
  end;

{--- Begin of conditionally by get power status result valid ---}

  if (schg=true) then
  begin

{---------- Support battery details, revisual if valid and changed ------------}

{--- Check for changes main/shadow from previous tick ---}

  bchg:=false;
  for i:=0 to bnum-1 do
  begin
    if ( Shadow01[i] <> BatCycleCountNum[i]  ) then bchg:=bchg OR true;
    if ( Shadow02[i] <> BatTemperatureNum[i] ) then bchg:=bchg OR true;
    if ( Shadow03[i] <> BatPowerStateNum[i]  ) then bchg:=bchg OR true;
    if ( Shadow04[i] <> BatCapacityNum[i]    ) then bchg:=bchg OR true;
    if ( Shadow05[i] <> BatVoltageNum[i]     ) then bchg:=bchg OR true;
    if ( Shadow06[i] <> BatRateNum[i]        ) then bchg:=bchg OR true;
  end;

{--- This run if changes detected at monitored arrays ---}

  if (bchg=true) then
  begin

{--- Call API ---}
  x1 := InitIoctl ( BatteryNames,
          BatteryTags, BatteryTagsStrings,
          BatteryModels, BatteryVendors,
          BatteryDatesNum, BatteryDatesStr,
          BatterySerialNum, BatteryUniqueId,
          BatteryChemistry1, BatteryChemistry2,
    {--- Additions for v0.04 ---}
          BatRechargableNum, BatRechargableStr,
          BatDesignedCapNum, BatDesignedCapStr,
          BatFChargeCapNum,  BatFChargeCapStr,
          BatDefAlrt1CapNum, BatDefAlrt1CapStr,
          BatDefAlrt2CapNum, BatDefAlrt2CapStr,
          BatCritBiasNum,    BatCritBiasStr,
    {--- This 6 pairs of parameters is argument for dynamical visual ---}
          BatCycleCountNum,  BatCycleCountStr,
          BatTemperatureNum, BatTemperatureStr,
          BatPowerStateNum,  BatPowerStateStr,
          BatCapacityNum,    BatCapacityStr,
          BatVoltageNum,     BatVoltageStr,
          BatRateNum,        BatRateStr );

    { Hot connect and disconnect not supported yet, disable this scenario }
    if (x1 <> BatteryCount) then cb1.Enabled:=false

    else
    begin

{--- Revisual details if data valid ---}
  if BatteryCount > 0 then
  begin
  RevisualBatteryDetails ( cb1.ItemIndex, sg2,
                           BatteryTags,       BatteryTagsStrings,
                           BatteryModels,     BatteryVendors,
                           BatteryDatesNum,   BatteryDatesStr,
                           BatterySerialNum,  BatteryUniqueId,
                           BatteryChemistry1, BatteryChemistry2,
                           {--- Additions for v0.04 ---}
                           BatRechargableNum, BatRechargableStr,
                           BatDesignedCapNum, BatDesignedCapStr,
                           BatFChargeCapNum,  BatFChargeCapStr,
                           BatDefAlrt1CapNum, BatDefAlrt1CapStr,
                           BatDefAlrt2CapNum, BatDefAlrt2CapStr,
                           BatCritBiasNum,    BatCritBiasStr,
                           BatCycleCountNum,  BatCycleCountStr,
                           BatTemperatureNum, BatTemperatureStr,
                           BatPowerStateNum,  BatPowerStateStr,
                           BatCapacityNum,    BatCapacityStr,
                           BatVoltageNum,     BatVoltageStr,
                           BatRateNum,        BatRateStr );
  end else
  begin
    sg2.Clean;
    sg2.Enabled:=false;
  end;

{--- Update monitored arrays shadow, by current result ---}

      for i:=0 to bnum-1 do
      begin
        Shadow01[i] := BatCycleCountNum[i];
        Shadow02[i] := BatTemperatureNum[i];
        Shadow03[i] := BatPowerStateNum[i];
        Shadow04[i] := BatCapacityNum[i];
        Shadow05[i] := BatVoltageNum[i];
        Shadow06[i] := BatRateNum[i];
      end;
    end;

{--- End of conditionally by monitored areas changes detected ---}
  end;

{---------- Support Charging monitor ------------------------------------------}
{ required check for overflow }
{ required analyse, find auto scroll properity }

{--- Handling with no scroll ---}
  if (gen=true) AND (gi<GN) then
  begin
    gx := GMIN + (GMAX-GMIN) / (GN-1) * gi;
    gi += 1;
{--- Level ---}
    gy := BatCapacityNum[cb1.ItemIndex];        // Get target: Level, no scroll
    if ( gy > 1000000000 ) OR ( gy < -1000000000 ) then gy:=0.0;
    gyy1[gi] := gy;
    SeriesLevel.AddXY( gx, gy );
{--- Rate ---}
    gy := BatRateNum[cb1.ItemIndex];            // Get target: Rate, no scroll
    if ( gy > 1000000000 ) OR ( gy < -1000000000 ) then gy:=0.0;
    gyy2[gi] := gy;
    SeriesRate.AddXY( gx, gy );
  end;
{--- Handling with scroll ---}
  if (gen=true) AND (gi>=GN) then
  begin
    gx := GMIN + (GMAX-GMIN) / (GN-1) * gi;
    gi += 1;
{--- Level ---}
    gy := BatCapacityNum[cb1.ItemIndex];        // Get target: Level, Scroll
    if ( gy > 1000000000 ) OR ( gy < -1000000000 ) then gy:=0.0;
    for i:=0 to GN-2 do gyy1[i]:=gyy1[i+1];
    gyy1[GN-1]:=gy;
    SeriesLevel.Clear;
    for i:=0 to GN-1 do
    begin
      gx := GMIN + (GMAX-GMIN) / (GN-1) * i;
      SeriesLevel.AddXY( gx, gyy1[i] );
    end;
{--- Rate ---}
    gy := BatRateNum[cb1.ItemIndex];            // Get target: Rate, scroll
    if ( gy > 1000000000 ) OR ( gy < -1000000000 ) then gy:=0.0;
    for i:=0 to GN-1 do gyy2[i]:=gyy2[i+1];
    gyy2[GN-1]:=gy;
    SeriesRate.Clear;
    for i:=0 to GN-1 do
    begin
      gx := GMIN + (GMAX-GMIN) / (GN-1) * i;
      SeriesRate.AddXY( gx, gyy2[i] );
    end;
{--- End of handling with scroll ---}
  end;
{--- End of 2 handlings ---}
end;

{---------- End of Support Charging monitor -----------------------------------}

{--- End of conditionally by get power status ---}
  end;
{--- End of conditionally by ready for tick ---}
  end;

end.

