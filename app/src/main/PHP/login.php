<?php
session_start();
header('Content-Type: application/json');

$response = array();

// Database connection
$servername = "localhost";
$username = "root";
$password_db = "";
$database = "api";

$conn = new mysqli($servername, $username, $password_db, $database);

// Check connection
if ($conn->connect_error) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Database connection failed: ' . $conn->connect_error
    ]);
    exit;
}

// Check if request method is POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Retrieve and sanitize inputs
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    $password = isset($_POST['password']) ? $_POST['password'] : '';

    if (empty($user_id) || empty($password)) {
        echo json_encode([
            'status' => 'error',
            'message' => 'User ID and password are required.'
        ]);
        exit;
    }

    // Function to check user in a specific table
    function checkUser($conn, $table, $user_id, $password) {
        $stmt = $conn->prepare("SELECT user_id, password FROM $table WHERE user_id = ?");
        $stmt->bind_param("s", $user_id);
        $stmt->execute();
        $stmt->store_result();
        
        if ($stmt->num_rows > 0) {
            $stmt->bind_result($db_user_id, $db_password);
            $stmt->fetch();
            if ($password === $db_password) { // Use password_verify() if storing hashed passwords
                $_SESSION['user'] = [
                    'user_id' => $db_user_id,
                    'role' => $table
                ];
                return [
                    'status' => 'success',
                    'role' => $table,
                    'message' => ucfirst($table) . ' login successful.'
                ];
            } else {
                return [
                    'status' => 'error',
                    'message' => 'Invalid password.'
                ];
            }
        }
        return null;
    }

    // Check in 'admin' table first
    $adminResult = checkUser($conn, 'admin', $user_id, $password);
    if ($adminResult) {
        echo json_encode($adminResult);
        exit;
    }

    // Check in 'customer' table if not found in 'admin'
    $customerResult = checkUser($conn, 'customer', $user_id, $password);
    if ($customerResult) {
        echo json_encode($customerResult);
        exit;
    }

    // If user not found in either table
    echo json_encode([
        'status' => 'error',
        'message' => 'User not found.'
    ]);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Invalid request method. Please use POST.'
    ]);
}

$conn->close();
?>
