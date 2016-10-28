<?php
header("Content-type: application/octet-stream");
header('Content-Disposition: attachment; filename=project.diy');

$id=$_REQUEST["id"];

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

$sql = "
UPDATE diylc_project 
SET download_count = download_count + 1
WHERE project_id=".$id;

//echo $sql;

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}

readfile("http://diy-fever.com/diylc/uploads/".$id.".diy");

$mysqli->close();
?>