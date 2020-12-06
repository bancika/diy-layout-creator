<?php
	
$name=$_REQUEST["username"];
$token=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];
$projectId=$_REQUEST["projectId"];
$diylcVersion=$_REQUEST["diylcVersion"];

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
    echo "{\"string\":Project ID not provided.}";
    exit;
}
if (!$diylcVersion) {
    echo "{\"string\":DIYLC version not provided.}";
    exit;
}
if (!$thumbnailFile && !projectId) {
	echo "{\"string\":Thumbnail file not uploaded.}";
	exit;
}
if (!$projectFile && !projectId) {
	echo "{\"string\":Project file not uploaded.}";
	exit;
}
if ($thumbnailFile['size'] > 100000) {
        echo "{\"string\":Thumbnail file is too big.}";
	exit;
}
if ($projectFile['size'] > 10000000) {
        echo "{\"string\":Project file is too big.}";
	exit;
}
if (getimagesize($thumbnailFile['tmp_name']) == false) {
        echo "{\"string\":Thumbnail image is not valid.}";
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
	
	$sql = "
    SELECT project_id
    FROM diylc_project
    WHERE project_id = \"".addslashes($projectId)."\" AND owner_user_id = \"".addslashes($userId)."\"";
    	
	if (!$result = $mysqli->query($sql)) {
	    echo "{\"string\":Error while looking up the project.}";
	    exit;
	}
	
    $sql= "
		UPDATE diylc_project
		SET diylc_version=\"".$diylcVersion."\", last_update = NOW()
		WHERE project_id=".addslashes($projectId)." AND owner_user_id=".$userId;
	
	if (!$result = $mysqli->query($sql) || $mysqli->affected_rows == 0) {
	    echo "{\"string\":\"Error while uploading the project into the database.".$mysqli->error."\"}";
	    exit;
	} else {
    	// Move the uploaded files
    	if (move_uploaded_file($thumbnailFile['tmp_name'], '/home/diyfever/public_html/diylc/thumbnails/'.$projectId.".png") and	
    	    move_uploaded_file($projectFile['tmp_name'], '/home/diyfever/public_html/diylc/uploads/'.$projectId.".diy"))
    		echo "{\"string\":Success}";
    	else {
    		echo "{\"string\":Error processing uploaded files.}";
    	}
	}
} else {
	echo "{\"string\":User is not logged in.}";		
}
	
$mysqli->close();

?>