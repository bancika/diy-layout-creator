<?php
	
$name=$_REQUEST["username"];
$pwd=$_REQUEST["password"];
$email=$_REQUEST["email"];
$website=$_REQUEST["website"];
$bio=$_REQUEST["bio"];

if (!$name) {
	echo "{\"string\":Username not provided.}";
	exit;
}
if (!$pwd) {
	echo "{\"string\":Password not provided.}";
	exit;
}
if (strlen($pwd) < 6) {
	echo "{\"string\":Password must be at least 6 characters long.}";
	exit;
}
if (!$email) {
	echo "{\"string\":eMail not provided.}";
	exit;
}
$pwd = hash("ripemd160", $pwd);
	
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

// Run the query
$sql= "SELECT name FROM diylc_user WHERE LOWER(name) = LOWER('".$name."')";

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}

function ip_details($IPaddress) 
{
	$json       = file_get_contents("http://ip-api.com/json/{$IPaddress}");
	$details    = json_decode($json);
	return $details;
}  

$ip = $_SERVER["REMOTE_ADDR"];
$location = ip_details($ip);

	
$num = $num = $result->num_rows;
if ($num === 0) {
	$sql= "INSERT INTO diylc_user (name, password, email, website, bio, ip, country) VALUES ('".addslashes($name)."', '".$pwd."', '".addslashes($email)."', '".addslashes($website)."', '".addslashes($bio)."', '".$ip."', '".$location->country."')";
	
	if ($mysqli->query($sql) === TRUE) {
		echo "{\"string\":Success}";
	} else {
	    echo "{\"string\":Error}";
	}

} else {  
  echo "{\"string\":User already exists.}";
}

$mysqli->close();
?>