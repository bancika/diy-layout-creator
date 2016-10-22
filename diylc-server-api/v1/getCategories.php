<?php
	
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
$sql= "SELECT name FROM diylc_category ORDER BY sort_order";

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}

echo "{\"list\":{\"java.lang.String\":[";

$i=0;
	
while ($row = $result->fetch_assoc()) {
	if ($i>0)
		echo ",";
	echo "\"".$row["name"]."\"";
	$i++;
}

echo "]}}";

$mysqli->close();
?>