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
    $response['status'] = 'error';
    $response['message'] = 'Database connection failed: ' . $conn->connect_error;
    echo json_encode($response);
    exit;
}

// Check if request method is POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Retrieve form data and sanitize input
    $First_name = $_POST['First name'];
    $Last_name = $_POST['Last name'];
    $shop_name = $_POST['shop name'];
    $shop_address = $_POST['shop address'];
    $zip_code = $_POST['zip code'];
    $mobile = $_POST['Mobile number'];
    $email = filter_var($_POST['mail'], FILTER_SANITIZE_EMAIL);
    $password = $_POST['password'];
    $confirm_password = $_POST['confirm_password'];

    // Validate inputs
    if (empty($email) || empty($password) || empty($confirm_password)) {
        $response['status'] = 'error';
        $response['message'] = 'All fields are required.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid email format.';
    } elseif ($password !== $confirm_password) {
        $response['status'] = 'error';
        $response['message'] = 'Passwords do not match.';
    } else {
        // Hash the password
        //$hashed_password = password_hash($password, PASSWORD_DEFAULT);

        // Check if email already exists
        $check_stmt = $conn->prepare("SELECT email FROM customer WHERE email = ?");
        $check_stmt->bind_param("s", $email);
        $check_stmt->execute();
        $check_stmt->store_result();

        if ($check_stmt->num_rows > 0) {
            $response['status'] = 'error';
            $response['message'] = 'Email is already registered.';
        } else {
            // Generate user_id (C001, C002, ...)
            $result = $conn->query("SELECT COUNT(*) AS total FROM customer");
            $row = $result->fetch_assoc();
            $next_id = $row['total'] + 1;
            $user_id = "C" . str_pad($next_id, 3, "0", STR_PAD_LEFT);

            // Insert user data into the 'customer' table
            $stmt = $conn->prepare("INSERT INTO customer (user_id, email, password) VALUES (?, ?, ?)");
            $stmt->bind_param("sss", $user_id, $email, $password);

            if ($stmt->execute()) {
                // Start a session after successful registration
                $_SESSION['user'] = array(
                    'user_id' => $user_id,
                    'email' => $email
                );
                session_regenerate_id(true); // Secure session handling

                $response['status'] = 'success';
                $response['message'] = 'Registration successful!';
                $response['data'] = $_SESSION['user'];
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Database error: ' . $stmt->error;
            }
            $stmt->close();
        }
        $check_stmt->close();
    }
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid request method. Please use POST.';
}

$conn->close();
echo json_encode($response);
?>
