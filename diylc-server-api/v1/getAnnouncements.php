<?php
// Load help class
require_once("properties.php");

// Load properties
$dbProperties = new Properties();
$propertiesFile = fopen("db.properties", "rb");
$dbProperties->load($propertiesFile);

// Connect to the DB
$username=$dbProperties->getProperty("user");
$password=$dbProperties->getProperty("pass");
$database=$dbProperties->getProperty("db");
$mysqli = new mysqli(localhost,$username,$password,$database);

function ip_details($IPaddress) 
{
	$json       = file_get_contents("http://ip-api.com/json/{$IPaddress}");
	$details    = json_decode($json);
	return $details;
}  

$ip = $_SERVER["REMOTE_ADDR"];
$location = ip_details($ip);

$sql= "INSERT INTO diylc_usage_log (ip, country) VALUES ('".$ip."', '".$location->country."')";
$mysqli->query($sql);

$userId=$_REQUEST["userId"];
if ($userId) {
    $sql= "INSERT INTO diylc_user_stats (id, country) VALUES ('".$userId."', '".$location->country."') ON DUPLICATE KEY UPDATE last_seen = CURRENT_TIMESTAMP, counter = counter + 1";
    $mysqli->query($sql);    
}

$mysqli->close();
?>
{"list":{"org.diylc.announcements.Announcement":[{"title":"Greetings, fellow DIY'ers!","text":"If you need a custom component or feature implemented in DIYLC, do not hesitate to get in touch.<br>I can implement custom components for a reasonable fee and paid component requests will be<br>automatically put on the top of the queue for the next version.<br><br>Write to <font color='blue'>bancika@gmail.com</font>.<br><br>Cheers! ","date":"2019-03-04"},
{"title":"Calling translators","text":"If you wish to contribute by translating DIYLC interface to your language, please get in touch through <font color='blue'>bancika@gmail.com</font>. Thanks!","date":"2020-04-22"},
{"title":"Configuration issues!","text":"In case you recently experienced any issues with losing configuration (variants, recent files, etc), you may be affected by a bug that was discovered and fixed today.<br>DIYLC was using .diylc and diylc user directories simultaneously which caused issues with persistence of the configuration.<br>If you are affected, please upgrade to DIYLC v4.21.3 and then try copying config.xml file from .diylc to diylc directory in your user directory.<br><b>Note:</b> make sure to backup existing files prior to overwriting!<br><br>Sorry for inconvenience and stay safe!","date":"2022-02-22"},
{"title":"Your DIY files are needed :)","text":"In order to increase reliability and decrease the number of regression bugs in DIYLC, I have started building a regression test suite.<br>The idea is to build a framework that would automatically run any future release candidate version against a base of known DIY files,<br>trying to open each file, export it to PNG, save it back to another file and then validate that the outputs are as expected.<br>In order for this to be effective, we need a decent number of DIY files - old and new.<br>Ideally, we should cover all the use cases - guitar pedals, schematics, guitar wirings, amplifiers, tube stuff, etc. More is more here.<br>If you wish to contribute some of your cool projects, please send them over to <font color='blue'>bancika@gmail.com</font>.<br>Thanks in advance!","date":"2022-03-02"},
{"title":"Good news, everyone!","text":"As we are approaching DIYLC's sweet sixteen birthday, I'm happy to say that DIYLC is still growing - both in terms of features and the the user base.<br>By examining traffic statistics of DIYLC back-end, I can roughly estimate that we have more than doubled DIYLC user base in the last 5 years!<br>And growth trends are still pointing up. Thank you all for your ever growing support. DIYLC is still very much alive and I'm adding cool new features.<br><br>All the best!","date":"2022-03-24"}
]}}