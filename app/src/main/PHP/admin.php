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
    // Retrieve and sanitize inputs
    $first_name = trim($_POST['First_name']);
    $last_name = trim($_POST['Last_name']);
    $shop_name = trim($_POST['shop_name']);
    $shop_address = trim($_POST['shop_address']);
    $zip_code = trim($_POST['zip_code']);
    $mobile = trim($_POST['Mobile_number']);
    $email = filter_var($_POST['email'], FILTER_SANITIZE_EMAIL);
    $password = $_POST['password'];
    $confirm_password = $_POST['confirm_password'];

    // Validate inputs
    if (empty($first_name) || empty($last_name) || empty($shop_name) || empty($shop_address) || empty($zip_code) || empty($mobile) || empty($email) || empty($password) || empty($confirm_password)) {
        $response['status'] = 'error';
        $response['message'] = 'All fields are required.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid email format.';
    } elseif (!preg_match("/^[0-9]{5,6}$/", $zip_code)) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid zip code format.';
    } elseif (!preg_match("/^[0-9]{10}$/", $mobile)) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid mobile number. Must be 10 digits.';
    } elseif ($password !== $confirm_password) {
        $response['status'] = 'error';
        $response['message'] = 'Passwords do not match.';
    } else {
       

        // Check if email already exists
        $check_stmt = $conn->prepare("SELECT email FROM admin WHERE email = ?");
        $check_stmt->bind_param("s", $email);
        $check_stmt->execute();
        $check_stmt->store_result();

        if ($check_stmt->num_rows > 0) {
            $response['status'] = 'error';
            $response['message'] = 'Email is already registered.';
        } else {
            // Generate user_id (A001, A002, ...)
            $result = $conn->query("SELECT COUNT(*) AS total FROM admin");
            $row = $result->fetch_assoc();
            $next_id = $row['total'] + 1;
            $user_id = "A" . str_pad($next_id, 3, "0", STR_PAD_LEFT);

            // Insert user data into the 'admin' table
            $stmt = $conn->prepare("INSERT INTO admin (user_id, First_name, Last_name, shop_name, shop_address, zip_code, Mobile_number, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            $stmt->bind_param("sssssssss", $user_id, $first_name, $last_name, $shop_name, $shop_address, $zip_code, $mobile, $email, $password);

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