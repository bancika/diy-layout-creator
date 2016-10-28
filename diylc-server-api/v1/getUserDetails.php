<?php
	
$name=$_REQUEST["username"];
$token=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];

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

$className="org.diylc.plugins.cloud.model.UserEntity";

// Verify that the user is logged in
$sql = "
SELECT email, website, bio
FROM diylc_user 
WHERE name = \"".addslashes($name)."\" AND token= \"".addslashes($token)."\" AND machine_id = \"".addslashes($machineId)."\"";

if (!$result = $mysqli->query($sql) or !($row = $result->fetch_assoc())) {
	echo "{\"string\":Error while looking up the user.}";
} else {
	echo "{\"".$className."\": {
		\"username\":\"".$name."\",
		\"email\":\"".addslashes($row["email"])."\",
		\"website\":\"".addslashes($row["website"])."\",
		\"bio\":\"".addslashes(str_replace(array("\n","\r\n","\r"),"<br>", $row["bio"]))."\"
	}}";
}

$mysqli->close();
?>