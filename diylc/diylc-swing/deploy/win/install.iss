[Setup]
AppName=DIY Layout Creator
AppVersion={#diylcver}
DefaultDirName={pf}\DIYLC
DefaultGroupName=DIY Layout Creator
UninstallDisplayIcon={app}\diylc.exe
Compression=zip
SolidCompression=yes
OutputDir=../build
OutputBaseFilename=diylc
ChangesAssociations=yes
AlwaysShowDirOnReadyPage=yes
; Only allow the installer to run on x64-compatible systems,
; and enable 64-bit install mode.
ArchitecturesAllowed={#arch}
ArchitecturesInstallIn64BitMode={#arch}

[Files]
Source: "..\build\diylc.exe"; DestDir: "{app}"
Source: "..\icons\diylc_file.ico"; DestDir: "{app}"
Source: "..\build\jar\diylc.jar"; DestDir: "{app}"
Source: "..\build\jar\lib\*"; DestDir: "{app}\lib"
Source: "..\build\jar\library\*"; DestDir: "{app}\library"
Source: "..\themes\*"; DestDir: "{app}\themes"
Source: "..\lang\*"; DestDir: "{app}\lang"
Source: "..\fonts\*"; DestDir: "{app}\fonts"
Source: "..\build\jre_win\*"; DestDir: "{app}\jre17"; Flags: ignoreversion recursesubdirs
Source: "innosetup\donate.bmp"; DestDir: "{tmp}"; Flags: dontcopy nocompression

[Registry]
Root: HKCR; Subkey: ".diy"; ValueData: "DIYLC"; Flags: uninsdeletevalue; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "DIYLC"; ValueData: "Program DIYLC";  Flags: uninsdeletekey; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "DIYLC\DefaultIcon"; ValueData: "{app}\diylc_file.ico"; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "DIYLC\shell\open\command";  ValueData: """{app}\diylc.exe"" ""%1"""; ValueType: string; ValueName: ""

[Icons]
Name: "{group}\DIY Layout Creator"; Filename: "{app}\diylc.exe"

[Run]
Filename: "{app}\diylc.exe"; Description: "Launch DIYLC on exit"; Flags: postinstall skipifsilent

[Code]
  
procedure LicenseLinkClick(Sender: TObject);
var
  ErrorCode: Integer;
begin
  ShellExec('', 'http://www.diy-fever.com/donate', '', '', SW_SHOW, ewNoWait, 
    ErrorCode);
end;

procedure InitializeWizard;
var donateImage: TBitmapImage;
begin
  ExtractTemporaryFile('donate.bmp');
  
  donateImage := TBitmapImage.Create(WizardForm);
  donateImage.Parent:= WizardForm;
  donateImage.Bitmap.LoadFromFile(ExpandConstant('{tmp}\donate.bmp'));
  donateImage.Stretch := True;
  donateImage.Width := 142;
  donateImage.Height := 27;
  donateImage.Left := 8;
  donateImage.Top := WizardForm.InnerPage.Height + 10;
  donateImage.Cursor := crHand;
  donateImage.OnClick := @LicenseLinkClick;  
end;
