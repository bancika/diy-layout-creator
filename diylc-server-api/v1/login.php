<?php
	
$name=$_REQUEST["username"];
$pwd=$_REQUEST["password"];
$machineId=$_REQUEST["machineId"];

if (!$name) {
	echo "{\"string\":Username not provided.}";
	exit;
}
if (!$pwd) {
	echo "{\"string\":Password not provided.}";
	exit;
}
if (!$machineId) {
	echo "{\"string\":Machine ID not provided.}";
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
$sql= "SELECT name FROM diylc_user WHERE name = \"".addslashes($name)."\" AND password = \"".addslashes($pwd)."\"";
if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}
	
if ($result->num_rows === 0) {
	echo "{\"string\":Error}";
} else {
	$token = uniqid(addslashes($name), false);
	$sql = "UPDATE diylc_user SET last_login = NOW(), machine_id = '".$machineId."', token='".$token."' WHERE name = \"".$name."\"";
	$result2 = $mysqli->query($sql);
	echo "{\"string\":".$token."}";
}
$result->free();
$mysqli->close();
?>