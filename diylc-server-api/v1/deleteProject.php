<?php
	
$name=$_REQUEST["username"];
$token=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];
$projectId=$_REQUEST["projectId"];

//echo var_dump($_FILES);

$thumbnailFile=$_FILES["thumbnail"];
$projectFile=$_FILES["project"];

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
if (!$projectId) {
	echo "{\"string\":Project Id not provided.}";
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
SELECT user_id 
FROM diylc_user 
WHERE name = \"".addslashes($name)."\" AND token= \"".addslashes($token)."\" AND machine_id = \"".addslashes($machineId)."\"";

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error while looking up the user.}";
	exit;
}

if ($row = $result->fetch_assoc()) {
	$userId = $row["user_id"];
		
	// Update the existing project in the database
	$sql= "
	UPDATE diylc_project 
	SET deleted = 1
	WHERE project_id=".addslashes($projectId)." AND owner_user_id=".$userId;
	
	//echo "{\"string\":\"".$sql."\"}";
	//exit;
	
	if (!$result = $mysqli->query($sql) || $mysqli->affected_rows == 0) {
		echo "{\"string\":\"Error while deleting the project from the database. ".$mysqli->error."\"}";
		exit;
	} else {
		echo "{\"string\":Success}";

	}	
} else {
	echo "{\"string\":User is not logged in.}";		
}
	
$mysqli->close();

?>