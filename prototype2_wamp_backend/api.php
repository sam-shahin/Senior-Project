<?php

// Tell the browser that this endpoint returns JSON.
header("Content-Type: application/json");

// Load local database settings.
// The real config.php is ignored by Git.
$config = require __DIR__ . "/config.php";

// Connect to the local MySQL/MariaDB database.
$connection = new mysqli(
    $config["host"],
    $config["user"],
    $config["password"],
    $config["database"],
    $config["port"]
);

// Stop and return an error if the database connection fails.
if ($connection->connect_error) {
    http_response_code(500);

    echo json_encode([
        "error" => "Database connection failed"
    ]);

    exit;
}

// Read the search text sent by the user.
$question = trim($_GET["question"] ?? "");

// Add + before each word so every search word is required.
$words = preg_split('/\s+/', $question);
$boolean_question = "";

foreach ($words as $word) {
    $word = preg_replace('/[^a-zA-Z0-9]/', '', $word);

    if ($word !== "") {
        $boolean_question .= "+" . $word . " ";
    }
}

// Search the legal question and answer fields.
$sql = "SELECT id, question, answer, source_name,
               source_url, jurisdiction, last_updated
        FROM legal_information
        WHERE MATCH(question, answer)
        AGAINST (? IN BOOLEAN MODE)
        ORDER BY MATCH(question, answer)
        AGAINST (? IN BOOLEAN MODE) DESC";

$stmt = $connection->prepare($sql);
$stmt->bind_param(
    "ss",
    $boolean_question,
    $boolean_question
);

$stmt->execute();

$result = $stmt->get_result();
$legal_information = [];

// Store each database result in an array.
while ($row = $result->fetch_assoc()) {
    $legal_information[] = $row;
}

// Send the results back as JSON.
echo json_encode($legal_information, JSON_PRETTY_PRINT);

$stmt->close();
$connection->close();

?>