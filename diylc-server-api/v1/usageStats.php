<html>
<body>
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


echo "<h4>Registered users</h4>";

$sql = "
SELECT COUNT(*) as counter FROM `diylc_user_stats`
WHERE last_seen > DATE_ADD(NOW() ,INTERVAL -24 HOUR)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Today: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(*) as counter FROM `diylc_user_stats`
WHERE last_seen > DATE_ADD(NOW() ,INTERVAL -7 DAY)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Last week: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(*) as counter FROM `diylc_user_stats` 
WHERE last_seen > DATE_ADD(NOW() ,INTERVAL -30 DAY)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Last month: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(*) as counter FROM `diylc_user_stats`";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "All time: " . $count . "<br>";	
}

echo "<h4>Active users by IP</h4>";

$sql = "
SELECT COUNT(distinct ip) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -24 HOUR)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Today: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(distinct ip) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -7 DAY)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Last week: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(distinct ip) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -30 DAY)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Last month: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(distinct ip) as counter
FROM diylc_usage_log";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "All time: " . $count . "<br>";	
}



echo "<h4>Sessions started</h4>";


$sql = "
SELECT COUNT(ip) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -24 HOUR)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Today: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(ip) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -7 DAY)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Last week: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(ip) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -30 DAY)";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "Last month: " . $count . "<br>";	
}

$sql = "
SELECT COUNT(ip) as counter
FROM diylc_usage_log";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$count = $row["counter"];
	echo "All time: " . $count . "<br>";	
}

echo "<h4>Usage by country (last week)</h4>";

$sql = "
SELECT country, COUNT(*) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -7 DAY)
GROUP BY country
ORDER BY COUNT(*) DESC";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$country = $row["country"];
	$count = $row["counter"];
	echo $country . ": " . $count . "<br>";	
}


echo "<h4>Usage by country (last month)</h4>";

$sql = "
SELECT country, COUNT(*) as counter
FROM diylc_usage_log
WHERE timestamp > DATE_ADD(NOW() ,INTERVAL -30 DAY)
GROUP BY country
ORDER BY COUNT(*) DESC";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$country = $row["country"];
	$count = $row["counter"];
	echo $country . ": " . $count . "<br>";	
}


echo "<h4>Usage by country (all time)</h4>";

$sql = "
SELECT country, COUNT(*) as counter
FROM diylc_usage_log
GROUP BY country
ORDER BY COUNT(*) DESC";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$country = $row["country"];
	$count = $row["counter"];
	echo $country . ": " . $count . "<br>";	
}

echo "<h4>Usage by month (all time)</h4>";

$sql = "
SELECT year(timestamp) as y, month(timestamp) as m, COUNT(*) as counter
FROM diylc_usage_log
GROUP BY year(timestamp), month(timestamp)
ORDER BY year(timestamp) desc, month(timestamp) DESC";

if (!$result = $mysqli->query($sql)) {
	echo "Error:".$mysqli->error;
	exit;
}

while ($row = $result->fetch_assoc()) {
	$y = $row["y"];
	$m = $row["m"];
	$count = $row["counter"];
	echo $y ."." . $m . ": " . $count . "<br>";	
}

$result->free();

$mysqli->close();
?>
</body>
</html>