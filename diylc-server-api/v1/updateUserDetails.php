<?php
	
$name=$_REQUEST["username"];
$token=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];
$email=$_REQUEST["email"];
$website=$_REQUEST["website"];
$bio=$_REQUEST["bio"];

if (!$name) {
	echo "{\"string\":Username not provided.}";
	exit;
}
if (!$token) {
	echo "{\"string\":Token not provided.}";
	exit;
}
if (!$machineId) {
	echo "{\"string\":Machine ID not provided.}";
	exit;
}
if (!$email) {
	echo "{\"string\":eMail not provided.}";
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

// Verify that the user is logged in
$sql = "
UPDATE diylc_user
SET email= \"".addslashes($email)."\", website= \"".addslashes($website)."\", bio= \"".addslashes($bio)."\"
WHERE name = \"".addslashes($name)."\" AND token= \"".addslashes($token)."\" AND machine_id = \"".addslashes($machineId)."\"";

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Database error.}";
	exit;
}

if ($mysqli->affected_rows === 0) {
	echo "{\"string\":Could not update the account.}";
} else {
	echo "{\"string\":Success}";
}

$mysqli->close();
?>