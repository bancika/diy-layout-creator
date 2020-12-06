<?php
	
$name=$_REQUEST["username"];
$token=$_REQUEST["token"];
$machineId=$_REQUEST["machineId"];
$projectName=$_REQUEST["projectName"];
$category=$_REQUEST["category"];
$description=$_REQUEST["description"];
$diylcVersion=$_REQUEST["diylcVersion"];
$keywords=$_REQUEST["keywords"];
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
if (!$category) {
	echo "{\"string\":Category not provided.}";
	exit;
}
if (!$description) {
	echo "{\"string\":Description not provided.}";
	exit;
}
if (!$diylcVersion) {
	echo "{\"string\":DIYLC version not provided.}";
	exit;
}
if (!$thumbnailFile && !$projectId) {
	echo "{\"string\":Thumbnail file not uploaded.}";
	exit;
}
if (!$projectFile && !$projectId) {
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
if (!$projectId && getimagesize($thumbnailFile['tmp_name']) == false) {
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
	
	// Find the right category	
	$sql = "
	SELECT category_id 
	FROM diylc_category_view
	WHERE LOWER(display_name) = LOWER(\"".addslashes($category)."\")";

	if (!$result = $mysqli->query($sql)) {
		echo "{\"string\":Error while determining the category.}";
		exit;
	}
	
	if ($row = $result->fetch_assoc()) {
		$categoryId = $row["category_id"];
		
		if ($projectId && is_numeric($projectId)) {
			// Update the existing project in the database
			$sql= "
			UPDATE diylc_project 
			SET name=\"".$projectName."\", description=\"".$description."\", category_id=".$categoryId.", diylc_version=\"".$diylcVersion."\", keywords = \"".$keywords."\", last_update = NOW()
			WHERE project_id=".addslashes($projectId)." AND owner_user_id=".$userId;
		} else {			
			// Insert into the database
			$sql= "
			INSERT INTO diylc_project (name, description, category_id, owner_user_id, diylc_version, keywords, uploaded_on, last_update) 
			VALUES (\"".addslashes($projectName)."\",\"".addslashes($description)."\",".$categoryId.",".$userId.",\"".addslashes($diylcVersion)."\",\"".addslashes($keywords)."\",now(),now())";
		}

		if (!$result = $mysqli->query($sql) || $mysqli->affected_rows == 0) {
			echo "{\"string\":\"Error while uploading the project into the database.".$mysqli->error."\"}";
			exit;
		} else if (!$projectId) {
			if (!$projectId || !is_numeric($projectId))
				$projectId = $mysqli->insert_id;
			// Move the uploaded files
			if (move_uploaded_file($thumbnailFile['tmp_name'], '/home/diyfever/public_html/diylc/thumbnails/'.$projectId.".png") and	
			    move_uploaded_file($projectFile['tmp_name'], '/home/diyfever/public_html/diylc/uploads/'.$projectId.".diy"))
				echo "{\"string\":Success}";
			else {
				// delete the entry if we couldn't move the files
				$sql="UPDATE diylc_project SET deleted = 1 WHERE project_id=".$projectId;
				$mysqli->query($sql);
				echo "{\"string\":Error processing uploaded files.}";
			}
		} else echo "{\"string\":Success}";
	} else {
		echo "{\"string\":Invalid category.}";	
	}
} else {
	echo "{\"string\":User is not logged in.}";		
}
	
$mysqli->close();

?>