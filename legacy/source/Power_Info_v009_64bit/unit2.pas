unit Unit2;  { Module for support GetPowerStatus WinAPI }

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, Windows;

function GetPower( var y1,y2,y3,y4:Byte; var y5,y6:Dword ):Boolean;

implementation

function GetPower( var y1,y2,y3,y4:Byte; var y5,y6:Dword ):Boolean;
var PowerStatus:TSystemPowerStatus;
    x1:Boolean;
begin
  x1:=GetSystemPowerStatus(PowerStatus);
  y1:=PowerStatus.ACLineStatus;
  y2:=PowerStatus.BatteryFlag;
  y3:=PowerStatus.BatteryLifePercent;
  y4:=PowerStatus.Reserved1;
  y5:=PowerStatus.BatteryLifeTime;
  y6:=PowerStatus.BatteryFullLifeTime;
  GetPower:=x1;
end;


end.

