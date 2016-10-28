<?php
	
$name=$_REQUEST["username"];
$oldPwd=$_REQUEST["oldPassword"];
$machineId=$_REQUEST["machineId"];
$newPwd=$_REQUEST["newPassword"];

if (!$name) {
	echo "{\"string\":Username not provided.}";
	exit;
}
if (!$oldPwd) {
	echo "{\"string\":Old password not provided.}";
	exit;
}
if (strlen($newPwd) < 6) {
	echo "{\"string\":Password must be at least 6 characters long.}";
	exit;
}
$oldPwd= hash("ripemd160", $oldPwd);
$newPwd= hash("ripemd160", $newPwd);
	
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
SET password = \"".$newPwd."\"
WHERE name = \"".addslashes($name)."\" AND password= \"".$oldPwd."\"";

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Database error.}";
	exit;
}

if ($mysqli->affected_rows === 0) {
	echo "{\"string\":Could not update the password.}";
} else {
	echo "{\"string\":Success}";
}

$mysqli->close();
?>