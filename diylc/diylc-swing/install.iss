[Setup]
AppName=DIY Layout Creator
AppVersion={#diylcver}
DefaultDirName={pf}\DIYLC
DefaultGroupName=DIY Layout Creator
UninstallDisplayIcon={app}\diylc.exe
Compression=lzma2
SolidCompression=yes
OutputDir=build
OutputBaseFilename=diylc
ChangesAssociations=yes

[Files]
Source: "diylc.exe"; DestDir: "{app}"
Source: "diylc.l4j.ini"; DestDir: "{app}"
Source: "config.xml"; DestDir: "{app}"
Source: "update.xml"; DestDir: "{app}"
Source: "splash.png"; DestDir: "{app}"
Source: "diylc.ico"; DestDir: "{app}"
Source: "build\jar\diylc.jar"; DestDir: "{app}"
Source: "build\jar\lib\*"; DestDir: "{app}\lib"
Source: "build\jar\library\*"; DestDir: "{app}\library"

[Registry]

Root: HKCR; Subkey: ".diy"; ValueData: "DIYLC"; Flags: uninsdeletevalue; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "DIYLC"; ValueData: "Program DIYLC";  Flags: uninsdeletekey; ValueType: string; ValueName: ""
Root: HKCR; Subkey: "DIYLC\DefaultIcon"; ValueData: "{app}\diylc.ico"; ValueType: string;  ValueName: ""
Root: HKCR; Subkey: "DIYLC\shell\open\command";  ValueData: """{app}\diylc.exe"" ""%1"""; ValueType: string; ValueName: ""

[Icons]
Name: "{group}\DIY Layout Creator"; Filename: "{app}\diylc.exe"

[Run]
Filename: "{app}\diylc.exe"; Flags: postinstall skipifsilent
