🏨 Hotel Management System - Backend

Backend API for Hotel Booking & Management System built with Spring Boot.

🚀 Technologies

* Java 21
* Spring Boot 3
* Spring Security
* JWT Authentication
* Spring Data JPA
* MySQL
* Gradle
* MapStruct
* Cloudinary
* SePay Payment Gateway

⸻

📋 Features

Authentication & Authorization

* JWT Authentication
* Refresh Token
* Logout
* Token Revocation
* Role-Based Access Control (RBAC)
* Permission-Based Authorization

User Management

* User Registration
* Login
* Profile Management
* User CRUD
* Role & Permission Management

Room & Room Type Management

* Room CRUD
* Room Type CRUD
* Room Type Images
* Room Amenities
* Room Availability Checking

Booking Management

* Create Booking
* Update Booking
* Cancel Booking
* Booking Status Tracking
* Booking History
* Cancellation Reason Tracking
* No-show Booking Handling

Invoice & Payment Management

* Invoice Generation
* Deposit Payment
* Full Payment
* Room Payment
* SePay Payment Integration
* Payment Webhook Handling
* Payment Status Tracking

Housekeeping Management

* Housekeeping Task Management
* Room Inspection
* Task Assignment
* Staff Task Tracking

Promotion Management

* Promotion CRUD
* Promotion Validation
* Booking Discount Application

Review Management

* Create Review
* Review Statistics
* Review Visibility Management
* Public Review Display

Dashboard & Reports

* Revenue Statistics
* Occupancy Statistics
* Booking Statistics
* Review Statistics

⸻

🏗️ Project Structure

src/main/java/com/hotelmanagement/backend
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── mapper
├── repository
├── security
├── service
│   └── impl
├── specification
└── util

⸻

⚙️ Environment Variables

Configure environment variables for production:

# Database
DB_URL=jdbc:mysql://localhost:3306/hotel_management
DB_USERNAME=root
DB_PASSWORD=password
# JWT
JWT_SECRET=your_secret_key
JWT_VALID_DURATION=604800
JWT_REFRESHABLE_DURATION=604800
# Mail
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
# CORS
CORS_ORIGIN=http://localhost:5173
# SePay
SEPAY_MERCHANT_ID=your_merchant_id
SEPAY_SECRET_KEY=your_secret_key
SEPAY_ENV=production
SEPAY_BANK_ACCOUNT=your_bank_account
SEPAY_BANK_CODE=your_bank_code

⸻

🗄️ Database Setup

Create database:

CREATE DATABASE hotel_management;

Update datasource configuration in:

application.yaml

For production, configure:

application-prod.yml

⸻

▶️ Running the Project

Clone repository:

git clone https://github.com/akhoa6204/backend-hotel-management.git
cd backend-hotel-management

Run application:

./gradlew bootRun

On Windows:

gradlew.bat bootRun

Application runs at:

http://localhost:3001/api

⸻

🚀 Running with Production Profile

java -jar app.jar --spring.profiles.active=prod

Or with Gradle:

./gradlew bootRun --args='--spring.profiles.active=prod'

⸻

🔐 Roles

ADMIN

Full system access.

RECEPTIONIST

* Booking Management
* Invoice Management
* Payment Management
* Housekeeping Management
* Room Management

HOUSEKEEPING

* Housekeeping Tasks
* Room Inspection

CUSTOMER

* Booking
* Payment
* Review
* Profile Management

⸻

📌 Booking Lifecycle

PENDING
    ↓
CONFIRMED
    ↓
CHECKED_IN
    ↓
CHECKED_OUT

Additional Status:

CANCELLED
NO_SHOW

⸻

📌 Payment Types

DEPOSIT
FULL_PAYMENT
ROOM_PAYMENT

Payment Status:

PENDING
SUCCESS
FAILED
EXPIRED

⸻

👨‍💻 Author

Anh Khoa

Backend Developer

GitHub: https://github.com/akhoa6204
