<?php
	
$className="org.diylc.plugins.cloud.model.CommentEntity";
$projectId=$_REQUEST["projectId"];

	
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
SELECT c.*, u.name username
FROM diylc_comment c
INNER JOIN diylc_user u ON u.user_id = c.user_id
WHERE project_id = ".$projectId."
ORDER BY posted_at";

//echo $sql;

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error:".$mysqli->error."}";
	exit;
}

$num = $num = $result->num_rows;
if ($num === 0) {
	echo "{\"list\":\"\"}";
} else {
	// JSON header
	echo "{\"list\":{\"".$className."\":[";

	$i=0;
	while ($row = $result->fetch_assoc()) {
	if ($i>0) {
		echo ",";
	}
	
	$time = strtotime($row["posted_at"]);
	$postedAt= date("Y-m-d H:i:s", $time);

	
	echo "{";
	echo "\"id\":".$row["comment_id"].",";
	echo "\"parentId\":".$row["parent_id"].",";
	echo "\"username\":\"".$row["username"]."\",";
	echo "\"comment\":\"".$row["comment"]."\",";
	echo "\"postedAt\":\"".$postedAt."\"";		
	echo "}";
	
	$i++;
}

	echo "]}}";
}

$result->free();
$mysqli->close();
?>