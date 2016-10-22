<?php
	
$name=$_REQUEST["username"];
$pwd=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];

if (!$name) {
	echo "{\"string\":Username not provided.}";
	exit;
}
if (!$pwd) {
	echo "{\"string\":Token not provided.}";
	exit;
}
if (!$machineId) {
	echo "{\"string\":Machine ID not provided.}";
	exit;
}
	
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
$sql= "SELECT name FROM diylc_user WHERE name = \"".addslashes($name)."\" AND token= \"".addslashes($pwd)."\" AND machine_id = \"".addslashes($machineId)."\"";

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}
	
if ($result->num_rows === 0) {
	echo "{\"string\":Error}";
} else {
	$sql = "UPDATE diylc_user SET last_login = NOW() WHERE name = \"".$name."\"";
	$result2 = $mysqli->query($sql);
	echo "{\"string\":Success}";
}


$result->free();
$mysqli->close();
?>