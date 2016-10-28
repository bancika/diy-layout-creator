<?php
	
$name=$_REQUEST["username"];
$token=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];
$projectId=$_REQUEST["projectId"];
$comment=$_REQUEST["comment"];


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
	echo "{\"string\":ProjectId not provided.}";
	exit;
}
if (!$comment) {
	echo "{\"string\":Comment not provided.}";
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
	$sql = "INSERT INTO diylc_comment (project_id, user_id, comment, posted_at) VALUES (".addslashes($projectId).",".$userId.",\"".addslashes($comment)."\",NOW())";
	if (!$result = $mysqli->query($sql)) {
		echo "{\"string\":Error while uploading the project into the database.}";
		exit;
	} else {
		echo "{\"string\":Success}";		
	}
} else {
	echo "{\"string\":User is not logged in.}";		
}
	
$mysqli->close();

?>