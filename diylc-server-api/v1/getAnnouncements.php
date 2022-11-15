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

function ip_details($IPaddress)
{
    $json       = file_get_contents("http://ip-api.com/json/{$IPaddress}");
    $details    = json_decode($json);
    return $details;
}

$ip = $_SERVER["REMOTE_ADDR"];
$location = ip_details($ip);

$userAgent = addslashes($_SERVER['HTTP_USER_AGENT']);

$sql= "INSERT INTO diylc_usage_log (ip, country, os) VALUES ('".$ip."', '".$location->country."', '".$userAgent."')";
$mysqli->query($sql);

$userId=$_REQUEST["userId"];
if ($userId) {
    $sql= "INSERT INTO diylc_user_stats (id, country) VALUES ('".$userId."', '".$location->country."') ON DUPLICATE KEY UPDATE last_seen = CURRENT_TIMESTAMP, counter = counter + 1";
    $mysqli->query($sql);
}

$mysqli->close();
?>
{"list":{"org.diylc.announcements.Announcement":[

]}}