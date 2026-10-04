<?php

// Load the local database settings.
$config = require __DIR__ . "/config.php";

// Test the connection to the database.
$connection = new mysqli(
    $config["host"],
    $config["user"],
    $config["password"],
    $config["database"],
    $config["port"]
);

// Display an error if the connection fails.
if ($connection->connect_error) {
    die("Database connection failed: " . $connection->connect_error);
}

// Display this message when the connection works.
echo "Database connection successful";

$connection->close();

?>