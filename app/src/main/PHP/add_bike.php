<?php
session_start();
header('Content-Type: application/json');

// Database connection
$servername = "localhost";
$username = "root";
$password_db = "";
$database = "api";

$conn = new mysqli($servername, $username, $password_db, $database);

if ($conn->connect_error) {
    echo json_encode(['status' => 'error', 'message' => 'Database connection failed: ' . $conn->connect_error]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Retrieve and sanitize inputs
    $bike_name = trim($_POST['Bike_Name']);
    $model = trim($_POST['Model']);
    $bike_reg_number = trim($_POST['Bike_Reg_number']);
    $mileage = trim($_POST['Milage']);
    $colour = trim($_POST['colour']);
    $rent_price = trim($_POST['rent_price']);
    $from_date = trim($_POST['From_date']);
    $to_date = trim($_POST['To_date']);

    // Delete expired bikes
    $deleteStmt = $conn->prepare("DELETE FROM add_bike WHERE To_date < CURDATE()");
    $deleteStmt->execute();
    $deleteStmt->close();

    // Validate inputs
    if (empty($bike_name) || empty($model) || empty($bike_reg_number) || empty($mileage) || empty($colour) || empty($rent_price) || empty($from_date) || empty($to_date)) {
        echo json_encode(['status' => 'error', 'message' => 'All fields are required.']);
        exit;
    }

    if (!preg_match("/^[A-Za-z0-9 ]+$/", $bike_name)) {
        echo json_encode(['status' => 'error', 'message' => 'Bike name can only contain letters and numbers.']);
        exit;
    }

    if (!is_numeric($mileage) || $mileage <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'Mileage must be a positive number.']);
        exit;
    }

    if (!is_numeric($rent_price) || $rent_price <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'Rent price must be a positive number.']);
        exit;
    }

    // Convert dates to timestamps
    $current_date = strtotime(date('Y-m-d'));
    $from_date_ts = strtotime($from_date);
    $to_date_ts = strtotime($to_date);

    // Validate date logic
    if ($from_date_ts < $current_date) {
        echo json_encode(['status' => 'error', 'message' => 'From date cannot be in the past.']);
        exit;
    }

    if ($to_date_ts < $current_date) {
        echo json_encode(['status' => 'error', 'message' => 'To date cannot be in the past.']);
        exit;
    }

    if ($from_date_ts > $to_date_ts) {
        echo json_encode(['status' => 'error', 'message' => 'From date cannot be after To date.']);
        exit;
    }

    // Check if Bike_Reg_number already exists
    $checkStmt = $conn->prepare("SELECT Bike_Reg_number FROM add_bike WHERE Bike_Reg_number = ?");
    $checkStmt->bind_param("s", $bike_reg_number);
    $checkStmt->execute();
    $checkStmt->store_result();

    if ($checkStmt->num_rows > 0) {
        echo json_encode(['status' => 'error', 'message' => 'Bike with this registration number already exists.']);
        $checkStmt->close();
        exit;
    }
    $checkStmt->close();

    // Handle Image Upload (JPG only)
    if (isset($_FILES['bike_image']) && $_FILES['bike_image']['error'] === UPLOAD_ERR_OK) {
        $image_tmp = $_FILES['bike_image']['tmp_name'];
        $image_name = basename($_FILES['bike_image']['name']);
        $image_ext = strtolower(pathinfo($image_name, PATHINFO_EXTENSION));

        // Allow only JPG format
        if ($image_ext !== 'jpg' && $image_ext !== 'jpeg') {
            echo json_encode(['status' => 'error', 'message' => 'Only JPG format is allowed.']);
            exit;
        }

        // Save Image to Uploads Directory
        $upload_dir = "uploads/";
        if (!is_dir($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        $image_path = $upload_dir . uniqid() . '.jpg'; // Unique filename

        if (move_uploaded_file($image_tmp, $image_path)) {
            // Insert Data into Database
            $stmt = $conn->prepare("INSERT INTO add_bike (Bike_Name, Model, Bike_Reg_number, Milage, colour, rent_price, From_date, To_date, bike_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            $stmt->bind_param("sssssssss", $bike_name, $model, $bike_reg_number, $mileage, $colour, $rent_price, $from_date, $to_date, $image_path);

            if ($stmt->execute()) {
                echo json_encode(['status' => 'success', 'message' => 'Bike added successfully!', 'image_url' => $image_path]);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $stmt->error]);
            }
            $stmt->close();
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Failed to upload image.']);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'JPG image file is required.']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method. Please use POST.']);
}

$conn->close();
?>
