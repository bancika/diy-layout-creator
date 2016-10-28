<?php
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
SET view_count = view_count + 1
WHERE project_id=".$id;

//echo $sql;

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}

$im = imagecreatefrompng("http://diy-fever.com/diylc/thumbnails/".$id.".png");
if ($im) {
	header('Content-Type: image/png');
	imagepng($im);
	imagedestroy($im);
}

$mysqli->close();
?>