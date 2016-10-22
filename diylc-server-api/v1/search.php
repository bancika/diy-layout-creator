<?php
	
$className="com.diyfever.diylc.plugins.cloud.model.ProjectEntity";
$criteria=$_REQUEST["criteria"];
$category=$_REQUEST["category"];
$format=$_REQUEST["format"];
$page=$_REQUEST["page"];
$itemsPerPage=$_REQUEST["itemsPerPage"];
$isJson=$format==="json";
$sort=$_REQUEST["sort"];
if(!$page)
	$page=1;
if(!$itemsPerPage)
	$itemsPerPage=10;
if(!$sort)
	$sort="Project Name";

$condition="";

if ($category)
	$condition = $condition." AND LOWER(c.name) = LOWER('".$category."')";
if ($criteria)
	$condition = $condition." AND (LOWER(p.description) LIKE LOWER('%".$criteria."%') OR LOWER(p.name) LIKE LOWER('%".$criteria."%') OR LOWER(p.keywords) LIKE LOWER('%".$criteria."%'))";
	
$limit = " LIMIT ".$itemsPerPage." OFFSET ".(($page-1)*$itemsPerPage);

$orderBy = " ORDER BY ";
if ($sort==="Author")
	$orderBy = $orderBy." u.name, p.name";	
else if ($sort==="Category")
	$orderBy = $orderBy." c.name, p.name";
else if ($sort==="Age")
	$orderBy = $orderBy." p.last_update";
else
	$orderBy = $orderBy." p.name";

$params="?criteria=".$criteria."&category=".$category."&format=".$format."&sort=".$sort;
	
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
SELECT p.project_id, p.name, p.description, c.name AS 'category', u.name AS 'owner', p.last_update 
FROM diylc_project p, diylc_category c, diylc_user u 
WHERE p.category_id = c.category_id AND p.owner_user_id = u.user_id ".$condition.$orderBy.$limit;

//echo $sql;

if (!$result = $mysqli->query($sql)) {
	echo "{\"string\":Error}";
	exit;
}

$num = $num = $result->num_rows;
if ($num === 0) {
	if ($isJson)
		echo "{\"list\":\"\"}";
	else {
		echo "<table width='600px' height='100px'><tr><td align='center' valign='top'><b>No matching projects found.</b></tr></td></table>";
		echo "<table width='600px'><tr><td align='left'>";
		
		if ($page > 1)
			echo "<a href='".$params."&page=".($page-1)."'><img src='http://diy-fever.com/diylc/api/v1/left-arrow.png'></a>";
		echo "</td><td align='right'></td></tr></table>";
	}
} else {
	// JSON header
	if ($isJson)
		echo "{\"list\":{\"".$className."\":[";
	else 
		echo "<table width='600px' border='0' cellspacing='0' cellpadding='1'>";

	$i=0;
	while ($row = $result->fetch_assoc()) {
	if ($i>0) {
		if ($isJson)
			echo ",";
		else
			echo "<tr height='8px'></tr>";
	}
	
	$time = strtotime($row["last_update"]);
	$updated = date("Y-m-d", $time);

	if ($isJson)
	{
		echo "{";
		echo "\"id\":".$row["project_id"].",";
		echo "\"name\":\"".$row["name"]."\",";
		echo "\"description\":\"".$row["description"]."\",";
		echo "\"owner\":\"".$row["owner"]."\",";
		echo "\"category\":\"".$row["category"]."\",";
		echo "\"updated\":\"".$updated ."\",";		
		echo "\"thumbnailUrl\":\"http://diy-fever.com/diylc/thumbnails/".$row["project_id"].".png\",";
		echo "\"downloadUrl\":\"http://diy-fever.com/diylc/uploads/".$row["project_id"].".diy\"";        
		echo "}";
	} else {		
		echo "
		<tr style='background-color:#999999;color:#FFFFFF;border-spacing:0;padding:0;'>
			<td rowspan='3' width='1'><img src='http://diy-fever.com/diylc/thumbnails/".$row["project_id"].".png'/></td>
			<td width='2px'></td>
			<td height='1px' colspan='2'><b>".$row["name"]."</b></td>
			<td height='1px' colspan='2' nowrap='1' align='right'>by ".$row["owner"]."</td>
			<td width='2px'></td>
		</tr>
		<tr>
			<td width='2px'></td>
			<td valign='top' halign='left' colspan='4'><p align='justify'>".$row["description"]."</p></td>
			<td style='border-right: 1px solid #999999;' width='2px'></td>
		</tr>
		<tr>
			<td style='border-bottom: 1px solid #999999;' width='2px'></td>
			<td style='border-bottom: 1px solid #999999;'colspan='3' valign='bottom'>Category: ".$row["category"]."<br>Uploaded on ".$updated."</td>
			<td style='border-right: 1px solid #999999;border-bottom: 1px solid #999999;' width='1px' colspan='2' valign='bottom' align='right'><a href='http://diy-fever.com/diylc/uploads/".$row["project_id"].".diy'><img src='http://diy-fever.com/diylc/api/v1/download-icon-32.png'/></a></td>
		</tr>";
	}
    $i++;
  }
  
	// JSON footer
 	if ($isJson)
		echo "]}}";
	else {
		echo "</table>";
		echo "<br>";
		
		echo "<table width='600px'><tr><td align='left'>";
		if ($page > 1)
			echo "<a href='".$params."&page=".($page-1)."'><img src='http://diy-fever.com/diylc/api/v1/left-arrow.png'></a>";
		echo "</td><td align='right'>";
		if ($num == $itemsPerPage)
			echo "<a href='".$params."&page=".($page+1)."'><img src='http://diy-fever.com/diylc/api/v1/right-arrow.png'></a>";
		echo "</td></tr></table>";
	}
}

$result->free();
$mysqli->close();
?>