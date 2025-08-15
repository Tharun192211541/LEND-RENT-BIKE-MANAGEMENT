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
    $name = trim($_POST['Full_Name']);
    $age = trim($_POST['Age']);
    $email = filter_var($_POST['email'], FILTER_SANITIZE_EMAIL);
    $mobile = trim($_POST['Mobile_number']);
    $store_name = trim($_POST['Store_name']);
    $store_address = trim($_POST['Store_Address']);
    $state = trim($_POST['State']);
    $city = trim($_POST['City']);

    // Validate inputs
    if (empty($name) || empty($age) || empty($email) || empty($mobile) || empty($store_name) || empty($store_address) || empty($state) || empty($city)) {
        $response['status'] = 'error';
        $response['message'] = 'All fields are required.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid email format.';
    } elseif (!preg_match("/^[0-9]{10}$/", $mobile)) {
        $response['status'] = 'error';
        $response['message'] = 'Invalid mobile number. Must be 10 digits.';
    } else {
        // Check if email exists in the admin table
        $check_stmt = $conn->prepare("SELECT user_id FROM admin WHERE email = ?");
        $check_stmt->bind_param("s", $email);
        $check_stmt->execute();
        $result = $check_stmt->get_result();

        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $user_id = $row['user_id'];

            // Check if email already exists in admin_edit table
            $edit_check_stmt = $conn->prepare("SELECT user_id FROM admin_edit WHERE email = ?");
            $edit_check_stmt->bind_param("s", $email);
            $edit_check_stmt->execute();
            $edit_result = $edit_check_stmt->get_result();

            if ($edit_result->num_rows > 0) {
                // Update existing record
                $update_stmt = $conn->prepare("UPDATE admin_edit SET Full_Name = ?, Age = ?, Mobile_number = ?, Store_name = ?, Store_Address = ?, State = ?, City = ? WHERE email = ?");
                $update_stmt->bind_param("sissssss", $name, $age, $mobile, $store_name, $store_address, $state, $city, $email);
                
                if ($update_stmt->execute()) {
                    $_SESSION['user'] = array('user_id' => $user_id, 'email' => $email);
                    session_regenerate_id(true);
                    
                    $response['status'] = 'success';
                    $response['message'] = 'Data updated successfully!';
                    $response['data'] = $_SESSION['user'];
                } else {
                    $response['status'] = 'error';
                    $response['message'] = 'Database error: ' . $update_stmt->error;
                }
                $update_stmt->close();
            } else {
                // Insert new record
                $insert_stmt = $conn->prepare("INSERT INTO admin_edit (user_id, Full_Name, Age, email, Mobile_number, Store_name, Store_Address, State, City) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                $insert_stmt->bind_param("ssissssss", $user_id, $name, $age, $email, $mobile, $store_name, $store_address, $state, $city);
                
                if ($insert_stmt->execute()) {
                    $_SESSION['user'] = array('user_id' => $user_id, 'email' => $email);
                    session_regenerate_id(true);
                    
                    $response['status'] = 'success';
                    $response['message'] = 'New data inserted successfully!';
                    $response['data'] = $_SESSION['user'];
                } else {
                    $response['status'] = 'error';
                    $response['message'] = 'Database error: ' . $insert_stmt->error;
                }
                $insert_stmt->close();
            }
            $edit_check_stmt->close();
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Email not found in admin table.';
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
