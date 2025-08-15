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
    echo json_encode(['status' => 'error', 'message' => 'Database connection failed: ' . $conn->connect_error]);
    exit;
}

// Check if request method is POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Retrieve and sanitize inputs
    $name = trim($_POST['Full_Name']);
    $age = trim($_POST['Age']);
    $gender = trim($_POST['Gender']);
    $email = filter_var($_POST['email'], FILTER_SANITIZE_EMAIL);
    $mobile = trim($_POST['Mobile_number']);
    $address = trim($_POST['Address']);
    $profession = trim($_POST['Profession']);
    $Work_Address = trim($_POST['Work_Address']);

    // Validate inputs
    if (empty($name) || empty($age) || empty($gender) || empty($email) || empty($mobile) || empty($address) || empty($profession) || empty($Work_Address)) {
        echo json_encode(['status' => 'error', 'message' => 'All fields are required.']);
        exit;
    }
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        echo json_encode(['status' => 'error', 'message' => 'Invalid email format.']);
        exit;
    }
    if (!preg_match("/^[0-9]{10}$/", $mobile)) {
        echo json_encode(['status' => 'error', 'message' => 'Invalid mobile number. Must be 10 digits.']);
        exit;
    }

    // Check if email exists in the customer table
    $check_stmt = $conn->prepare("SELECT user_id FROM customer WHERE email = ?");
    $check_stmt->bind_param("s", $email);
    $check_stmt->execute();
    $result = $check_stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $user_id = $row['user_id'];

        // Check if email already exists in customer_edit table
        $edit_check_stmt = $conn->prepare("SELECT user_id FROM customer_edit WHERE email = ?");
        $edit_check_stmt->bind_param("s", $email);
        $edit_check_stmt->execute();
        $edit_result = $edit_check_stmt->get_result();

        if ($edit_result->num_rows > 0) {
            // Update existing record
            $update_stmt = $conn->prepare("UPDATE customer_edit SET Full_Name = ?, Age = ?, gender = ?, Mobile_number = ?, Address = ?, Profession = ?, Work_Address = ? WHERE email = ?");
            $update_stmt->bind_param("sissssss", $name, $age, $gender, $mobile, $address, $profession, $Work_Address, $email);
            
            if ($update_stmt->execute()) {
                $_SESSION['user'] = ['user_id' => $user_id, 'email' => $email];
                session_regenerate_id(true);
                
                echo json_encode(['status' => 'success', 'message' => 'Data updated successfully!', 'data' => $_SESSION['user']]);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $update_stmt->error]);
            }
            $update_stmt->close();
        } else {
            // Insert new record
            $insert_stmt = $conn->prepare("INSERT INTO customer_edit (user_id, Full_Name, Age, gender, email, Mobile_number, Address, Profession, Work_Address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            $insert_stmt->bind_param("ssissssss", $user_id, $name, $age, $gender, $email, $mobile, $address, $profession, $Work_Address);
            
            if ($insert_stmt->execute()) {
                $_SESSION['user'] = ['user_id' => $user_id, 'email' => $email];
                session_regenerate_id(true);
                
                echo json_encode(['status' => 'success', 'message' => 'New data inserted successfully!', 'data' => $_SESSION['user']]);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $insert_stmt->error]);
            }
            $insert_stmt->close();
        }
        $edit_check_stmt->close();
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Email not found in customer table.']);
    }
    $check_stmt->close();
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method. Please use POST.']);
}

$conn->close();
?>
