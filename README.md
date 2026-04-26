# GoTrip Backend

A microservices-based travel platform backend built with **Java 25**, **Spring Boot 4**, and **Spring Cloud 2025**.

---

## Team Members

| Student ID | Student Name |
|---|---|
| 27292 | GUI Perera |
| 29015 | DJI Senarathna |
| 27601 | WKR Pinsiri |
| 27578 | MJM Shaahid |
| 27654 | GHM Bandara |
| 27958 | MCA Jayasingha |
| 29287 | MMM Shakeef |
| 28930 | AJM Naveeth |

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [1. Setting Up the Development Environment](#1-setting-up-the-development-environment)
- [2. Authentication](#2-authentication)
- [3. API Endpoints](#3-api-endpoints)
  - [Auth Service](#auth-service)
  - [User Service](#user-service)
  - [Hotel Service](#hotel-service)
  - [Hotel Booking](#hotel-booking)
  - [Restaurant Service](#restaurant-service)
  - [Restaurant Booking](#restaurant-booking)
  - [Transport Service](#transport-service)
  - [Transport Booking](#transport-booking)
  - [Experience Service](#experience-service)
  - [Experience Booking](#experience-booking)
  - [Experience Reviews](#experience-reviews)
  - [Review Service](#review-service)
- [Quick Reference](#quick-reference)

---

## Architecture Overview

All client requests go through the **API Gateway** (port `8080`), which routes them to the appropriate microservice via the Eureka **Discovery Server**.

```
Client
  │
  ▼
API Gateway (my-gateway) :8080
  │
  ├── /auth/**                    → user-service
  ├── /user/**                    → user-service
  ├── /hotel-service/**           → hotel-service
  ├── /hotel-booking/**           → hotel-service
  ├── /restaurant-service/**      → restaurant-service
  ├── /restaurant-booking/**      → restaurant-service
  ├── /transport-service/**       → transport-service
  ├── /transport-service/bookings/** → transport-service
  ├── /experience/**              → experience-service
  └── /api/reviews/**             → review-service
```

| Module | Description |
|---|---|
| `discovery-server` | Eureka service registry — all services register here |
| `my-gateway` | Spring Cloud Gateway — single entry point for all API calls |
| `user-service` | Authentication, registration, and user management |
| `hotel-service` | Hotel listings and booking management |
| `restaurant-service` | Restaurant listings and booking management |
| `transport-service` | Transport listings and booking management |
| `experience-service` | Experience listings, bookings, and reviews |
| `review-service` | Global transport reviews |
| `common-library` | Shared DTOs, JWT utilities, filters, and exception handlers |

---

## 1. Setting Up the Development Environment

### Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 25 |
| Maven | 3.9+ |
| IntelliJ IDEA | 2024.1+ recommended |
| PostgreSQL | Any (Neon cloud recommended) |

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/upekshaip/gotrip-backend.git
cd gotrip-backend
```

---

### Step 2 — Configure the Environment File

Create a `.env` file at the **root of the project** (same level as the parent `pom.xml`):

```bash
cp env .env
```

Open `.env` and fill in your PostgreSQL connection URLs for each service:

```env
USER_SERVICE_DB_URL=jdbc:postgresql://<host>/<db>?user=<user>&password=<password>&sslmode=require&channelBinding=require
HOTEL_SERVICE_DB_URL=jdbc:postgresql://<host>/<db>?user=<user>&password=<password>&sslmode=require&channelBinding=require
EXPERIENCE_SERVICE_DB_URL=jdbc:postgresql://<host>/<db>?user=<user>&password=<password>&sslmode=require&channelBinding=require
RESTAURANT_SERVICE_DB_URL=jdbc:postgresql://<host>/<db>?user=<user>&password=<password>&sslmode=require&channelBinding=require
TRANSPORT_SERVICE_DB_URL=jdbc:postgresql://<host>/<db>?user=<user>&password=<password>&sslmode=require&channelBinding=require

# Do not change this unless you want to rotate the JWT signing secret
JWT_SECRET=d72bd332afea8b3b5c95d0335b1b071bc25683b7aa6111762c750a8ae1afec9b
```

> **Tip:** [Neon](https://neon.tech) offers a free serverless PostgreSQL tier. Create one project and provision a separate database for each service.

---

### Step 3 — Open in IntelliJ IDEA

1. Go to **File → Open** and select the root `gotrip-backend` folder.
2. IntelliJ detects the parent `pom.xml` and auto-imports all Maven modules.
3. Wait for the Maven sync to finish (watch the status bar at the bottom).

---

### Step 4 — Enable Java 25 Preview Features

The project requires Java 25 preview features (configured in the `maven-compiler-plugin`). Enable them in IntelliJ too:

**Project SDK:**
- **File → Project Structure → Project**
- Set **SDK** → Java 25
- Set **Language level** → `25 (Preview)`

**Compiler flags:**
- **File → Settings → Build, Execution, Deployment → Compiler → Java Compiler**
- Add `--enable-preview` to **Additional command line parameters**

---

### Step 5 — Create Run Configurations

Create a **Spring Boot** run configuration for each service via **Run → Edit Configurations → + → Spring Boot**:

| Service | Main Class |
|---|---|
| `discovery-server` | `com.gotrip.discovery_server.DiscoveryServerApplication` |
| `my-gateway` | `com.gotrip.my_gateway.MyGatewayApplication` |
| `user-service` | `com.gotrip.user_service.UserServiceApplication` |
| `hotel-service` | `com.gotrip.hotel_service.HotelServiceApplication` |
| `restaurant-service` | `com.gotrip.restaurant_service.RestaurantServiceApplication` |
| `transport-service` | `com.gotrip.transport_service.TransportServiceApplication` |
| `experience-service` | `com.gotrip.experience_service.ExperienceServiceApplication` |
| `review-service` | `com.gotrip.review_service.ReviewServiceApplication` |

---

### Step 6 — Start Services in Order

```
1. discovery-server   →  Eureka dashboard: http://localhost:8761
2. my-gateway         →  API Gateway:      http://localhost:8080
3. All other services (any order)
```

Wait for each service to show **UP** on the Eureka dashboard before sending requests.

---

## 2. Authentication

GoTrip uses **JWT (JSON Web Token)** stateless authentication implemented in `user-service` and enforced at the gateway via `common-library`.

### Token Flow

```
1. POST /auth/signup      →  Register with email + password
2. PATCH /auth/signup     →  Complete profile (name, gender, phone, dob)  [optional]
3. POST /auth/login       →  Receive accessToken + refreshToken
4. All protected requests →  Authorization: Bearer <accessToken>
5. GET /auth/refresh      →  Get a new accessToken using the refresh cookie
```

On signup and login, the server sets two cookies automatically:
- `accessToken` — short-lived (5 min), readable by frontend
- `jwt` — long-lived refresh token, `HttpOnly`, `Secure`

### Sending the Token

```http
Authorization: Bearer <your_access_token>
```

### User Roles

| Role | Description |
|---|---|
| `TRAVELLER` | Can browse listings and make bookings |
| `SERVICE_PROVIDER` | Can create and manage their own listings |
| `ADMIN` | Full access to all admin endpoints |

---

## 3. API Endpoints

**Base URL:** `http://localhost:8080`

> A Postman collection is included in the repo: [`goTrip.postman_collection.json`](./goTrip.postman_collection.json).
> Set `url` = `http://localhost:8080` and `accessToken` = JWT from `/auth/login`.

---

### Auth Service

> `user-service` · Prefix: `/auth`

---

#### `POST /auth/signup`

Register a new user account.

**Auth required:** No

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**Response:** `201 Created`
```json
{
  "user": { ... },
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>"
}
```

---

#### `PATCH /auth/signup`

Complete the user's profile after initial registration.

**Auth required:** Yes

**Request body:**
```json
{
  "name": "John Doe",
  "gender": "m",
  "dob": "2001-01-01",
  "phone": "0112345678"
}
```

**Response:** `201 Created`
```json
{
  "user": { ... },
  "accessToken": "<new_jwt>"
}
```

---

#### `POST /auth/login`

Login and receive access and refresh tokens.

**Auth required:** No

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**Response:** `200 OK`
```json
{
  "user": { ... },
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>"
}
```

---

#### `GET /auth/refresh`

Get a new access token. Requires the `jwt` refresh token cookie (set automatically on login).

**Auth required:** Yes (via `jwt` cookie)

**Response:** `200 OK`
```json
{
  "accessToken": "<new_jwt>"
}
```

---

### User Service

> `user-service` · Prefix: `/user`

---

#### `GET /user/me`

Returns the decoded JWT principal (the current authenticated user's claims).

**Auth required:** Yes

---

#### `GET /user/profile`

Returns the full user record from the database.

**Auth required:** Yes

---

#### `PATCH /user/update-profile`

Update the current user's profile details.

**Auth required:** Yes

**Request body:**
```json
{
  "name": "John Doe",
  "phone": "0112345678",
  "dob": "2001-01-01",
  "gender": "m"
}
```

**Response:** `200 OK`
```json
{
  "user": { ... },
  "accessToken": "<updated_jwt>"
}
```

---

#### `GET /user/admin/all-travellers`

Get a paginated list of all travellers.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /user/admin/all-providers`

Get a paginated list of all service providers.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `PATCH /user/admin/edit-user`

Edit any user's profile details.

**Auth required:** Yes (Admin)

**Request body:**
```json
{
  "userId": 1,
  "name": "Updated Name",
  "phone": "0112345678",
  "dob": "2001-01-01",
  "gender": "m"
}
```

---

#### `PATCH /user/admin/edit-user-role`

Change a user's role.

**Auth required:** Yes (Admin)

**Request body:**
```json
{
  "userId": 1,
  "role": "SERVICE_PROVIDER"
}
```

> `role` accepts: `ADMIN`, `SERVICE_PROVIDER`, `TRAVELLER`

---

#### `GET /user/admin/traveller/{travellerId}`

Get contact info for a specific traveller.

**Auth required:** Yes (Admin)

---

#### `GET /user/admin/provider/{providerId}`

Get contact info for a specific service provider.

**Auth required:** Yes (Admin)

---

### Hotel Service

> `hotel-service` · Prefix: `/hotel-service`

---

#### `POST /hotel-service`

Create a new hotel listing.

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "name": "Hotel Sunrise",
  "description": "A beautiful beachfront hotel.",
  "address": "123 Beach Road",
  "city": "Gampaha",
  "price": 10000,
  "discount": 500,
  "priceUnit": "PER_DAY",
  "latitude": 7.08298,
  "longitude": 79.99893,
  "imageUrl": "https://example.com/image.jpg",
  "featured": false
}
```

> `priceUnit` accepts: `PER_PERSON`, `PER_HOUR`, `PER_DAY`

**Response:** `201 Created`

---

#### `GET /hotel-service`

Get a paginated list of all active hotels.

**Auth required:** Yes

**Query params:** `page` (default: `1`), `limit` (default: `5`)

---

#### `GET /hotel-service/my`

Get the provider's own hotel listings, optionally filtered by status.

**Auth required:** Yes (Service Provider)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

> `status` accepts: `PENDING`, `ACTIVE`, `REMOVED`

---

#### `GET /hotel-service/{id}`

Get a hotel by ID.

**Auth required:** Yes

---

#### `GET /hotel-service/traveller/{id}`

Get hotel details in traveller-friendly format.

**Auth required:** Yes

---

#### `GET /hotel-service/provider/{id}`

Get hotel details in provider format.

**Auth required:** Yes (Service Provider)

---

#### `PUT /hotel-service/{id}`

Update a hotel listing.

**Auth required:** Yes (Service Provider, must own the hotel)

**Request body:** Same as create.

---

#### `DELETE /hotel-service/{id}`

Soft-delete a hotel listing.

**Auth required:** Yes (Service Provider, must own the hotel)

---

#### `GET /hotel-service/me`

Returns the authenticated user's JWT claims as seen by the hotel service. Used for debugging token propagation.

**Auth required:** Yes

---

#### `GET /hotel-service/admin/all`

Get all hotels (any status), paginated.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /hotel-service/admin/pending`

Get all hotels with `PENDING` status.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `PUT /hotel-service/admin/{id}`

Admin update of any hotel listing.

**Auth required:** Yes (Admin)

**Request body:** Same as create.

---

#### `PUT /hotel-service/admin/status/{id}`

Admin update of a hotel's status.

**Auth required:** Yes (Admin)

**Request body:**
```json
{
  "status": "ACTIVE"
}
```

> `status` accepts: `PENDING`, `ACTIVE`, `REMOVED`

---

### Hotel Booking

> `hotel-service` · Prefix: `/hotel-booking`

---

#### `POST /hotel-booking/request`

Submit a hotel booking request.

**Auth required:** Yes (Traveller)

**Request body:**
```json
{
  "hotelId": 1,
  "personCount": 2,
  "requestMessage": "We will arrive in the evening.",
  "startingDate": "2027-03-05",
  "startingTime": "08:00",
  "endingDate": "2027-03-06",
  "endingTime": "10:00",
  "roomCount": 2
}
```

> Dates: `YYYY-MM-DD` · Times: `HH:mm` (24-hour)

---

#### `PATCH /hotel-booking/{id}/respond`

Provider responds to a booking request (accept or decline).

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "message": "Welcome! Your room is ready.",
  "status": "ACCEPTED"
}
```

> `status` accepts: `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

---

#### `DELETE /hotel-booking/{id}/cancel`

Cancel a booking.

**Auth required:** Yes (Traveller or Provider)

---

#### `GET /hotel-booking/my-bookings`

Get all bookings made by the current traveller.

**Auth required:** Yes (Traveller)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /hotel-booking/incoming-requests`

Get all booking requests received by the current provider.

**Auth required:** Yes (Service Provider)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

### Restaurant Service

> `restaurant-service` · Prefix: `/restaurant-service`

Mirrors the Hotel Service structure exactly.

---

#### `POST /restaurant-service`

Create a new restaurant listing.

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "name": "The Sunrise Cafe",
  "description": "Rooftop dining with city views.",
  "address": "45 Galle Road",
  "city": "Colombo",
  "price": 3000,
  "discount": 200,
  "priceUnit": "PER_PERSON",
  "latitude": 6.9271,
  "longitude": 79.8612,
  "imageUrl": "https://example.com/image.jpg",
  "featured": false
}
```

> `priceUnit` accepts: `PER_PERSON`, `PER_HOUR`, `PER_DAY`

**Response:** `201 Created`

---

#### `GET /restaurant-service`

Get a paginated list of all active restaurants.

**Auth required:** Yes

**Query params:** `page` (default: `1`), `limit` (default: `5`)

---

#### `GET /restaurant-service/my`

Get the provider's own restaurant listings.

**Auth required:** Yes (Service Provider)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /restaurant-service/{id}`

Get a restaurant by ID.

**Auth required:** Yes

---

#### `GET /restaurant-service/traveller/{id}`

Get restaurant details in traveller-friendly format.

**Auth required:** Yes

---

#### `GET /restaurant-service/provider/{id}`

Get restaurant details in provider format.

**Auth required:** Yes (Service Provider)

---

#### `PUT /restaurant-service/{id}`

Update a restaurant listing.

**Auth required:** Yes (Service Provider, must own it)

**Request body:** Same as create.

---

#### `DELETE /restaurant-service/{id}`

Soft-delete a restaurant listing.

**Auth required:** Yes (Service Provider, must own it)

---

#### `GET /restaurant-service/me`

Debug: returns the authenticated user's JWT claims.

**Auth required:** Yes

---

#### `GET /restaurant-service/admin/all`

Get all restaurants (any status), paginated.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /restaurant-service/admin/pending`

Get all pending restaurants.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `PUT /restaurant-service/admin/{id}`

Admin update of any restaurant.

**Auth required:** Yes (Admin)

**Request body:** Same as create.

---

#### `PUT /restaurant-service/admin/status/{id}`

Admin update of a restaurant's status.

**Auth required:** Yes (Admin)

**Request body:**
```json
{
  "status": "ACTIVE"
}
```

---

### Restaurant Booking

> `restaurant-service` · Prefix: `/restaurant-booking`

---

#### `POST /restaurant-booking/request`

Submit a restaurant booking request.

**Auth required:** Yes (Traveller)

**Request body:**
```json
{
  "restaurantId": 1,
  "personCount": 4,
  "requestMessage": "Please reserve a window table.",
  "startingDate": "2027-04-10",
  "startingTime": "19:00",
  "endingDate": "2027-04-10",
  "endingTime": "21:00",
  "roomCount": 1
}
```

> Dates: `YYYY-MM-DD` · Times: `HH:mm` (24-hour)

---

#### `PATCH /restaurant-booking/{id}/respond`

Provider responds to a booking request.

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "message": "Your table is reserved!",
  "status": "ACCEPTED"
}
```

> `status` accepts: `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

---

#### `DELETE /restaurant-booking/{id}/cancel`

Cancel a restaurant booking.

**Auth required:** Yes

---

#### `GET /restaurant-booking/my-bookings`

Get all restaurant bookings made by the current traveller.

**Auth required:** Yes (Traveller)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /restaurant-booking/incoming-requests`

Get all booking requests received by the current provider.

**Auth required:** Yes (Service Provider)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

### Transport Service

> `transport-service` · Prefix: `/transport-service`

---

#### `POST /transport-service`

Create a new transport listing.

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "vehicleMake": "Toyota",
  "vehicleModel": "HiAce",
  "vehicleType": "Van",
  "description": "Comfortable 12-seater van.",
  "city": "Kandy",
  "priceUnit": "PER_DAY",
  "price": 15000,
  "capacity": 12,
  "latitude": 7.2906,
  "longitude": 80.6337,
  "imageUrl": "https://example.com/van.jpg",
  "isFeatured": false
}
```

> `priceUnit` accepts: `PER_DAY`, `PER_KM`, `FLAT_RATE`

**Response:** `201 Created`

---

#### `GET /transport-service`

Get a paginated list of all active transports.

**Auth required:** Yes

**Query params:** `page` (default: `1`), `limit` (default: `5`)

---

#### `GET /transport-service/search`

Search transport listings by city.

**Auth required:** Yes

**Query params:** `city` (required), `page` (default: `1`), `limit` (default: `5`)

---

#### `GET /transport-service/my`

Get the provider's own transport listings.

**Auth required:** Yes (Service Provider)

**Query params:** `status` (optional: `PENDING`, `ACTIVE`, `REMOVED`), `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /transport-service/{id}`

Get a transport by ID.

**Auth required:** Yes

---

#### `PUT /transport-service/{id}`

Update a transport listing.

**Auth required:** Yes (Service Provider, must own it)

**Request body:** Same as create.

---

#### `DELETE /transport-service/{id}`

Delete a transport listing.

**Auth required:** Yes (Service Provider, must own it)

**Response:** `204 No Content`

---

#### `GET /transport-service/me`

Debug: returns the authenticated user's JWT claims.

**Auth required:** Yes

---

#### `GET /transport-service/admin/all`

Get all transport listings (any status), paginated.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /transport-service/admin/pending`

Get all pending transport listings.

**Auth required:** Yes (Admin)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `PATCH /transport-service/admin/{id}/approve`

Approve a pending transport listing.

**Auth required:** Yes (Admin)

---

#### `GET /transport-service/admin/stats`

Get transport and booking statistics.

**Auth required:** Yes (Admin)

**Response:**
```json
{
  "totalTransports": 50,
  "activeTransports": 42,
  "totalBookings": 120,
  "pendingBookings": 8
}
```

---

### Transport Booking

> `transport-service` · Prefix: `/transport-service/bookings`

---

#### `POST /transport-service/bookings/request`

Submit a transport booking request.

**Auth required:** Yes (Traveller)

**Request body:**
```json
{
  "transportId": 3,
  "pickupLocation": "Colombo Fort",
  "dropoffLocation": "Kandy City Centre",
  "startingDate": "2027-05-01",
  "startingTime": "07:00",
  "endingDate": "2027-05-01",
  "endingTime": "12:00",
  "requestMessage": "Please be on time."
}
```

> Dates: `YYYY-MM-DD` · Times: `HH:mm` (24-hour)

---

#### `PATCH /transport-service/bookings/{id}/respond`

Provider responds to a booking request.

**Auth required:** Yes (Service Provider)

**Query params:** `status`, `message`

> `status` accepts: `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

---

#### `PATCH /transport-service/bookings/{id}/cancel`

Cancel a transport booking.

**Auth required:** Yes

---

#### `PATCH /transport-service/bookings/{id}/complete`

Mark a booking as completed (so the traveller can leave a review).

**Auth required:** Yes (Service Provider)

---

#### `GET /transport-service/bookings/my-bookings`

Get all bookings made by the current traveller.

**Auth required:** Yes (Traveller)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /transport-service/bookings/provider-requests`

Get all incoming booking requests for the current provider.

**Auth required:** Yes (Service Provider)

**Query params:** `status` (optional), `page` (default: `1`), `limit` (default: `10`)

---

### Experience Service

> `experience-service` · Prefix: `/experience`

---

#### `POST /experience/create`

Create a new experience listing.

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "title": "Sunrise Hike - Ella Rock",
  "description": "A guided 4-hour hike to the summit.",
  "category": "Hiking",
  "type": "Outdoor",
  "location": "Ella",
  "pricePerUnit": 2500.00,
  "priceUnit": "PER_PERSON",
  "maxCapacity": 10,
  "imageUrl": "https://example.com/hike.jpg"
}
```

**Response:** `201 Created`

---

#### `GET /experience/all`

Get all experience listings.

**Auth required:** Yes

---

#### `GET /experience/available`

Get all available (active) experiences, paginated.

**Auth required:** Yes

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `GET /experience/{id}`

Get an experience by ID.

**Auth required:** Yes

---

#### `GET /experience/category/{category}`

Get experiences filtered by category.

**Auth required:** Yes

---

#### `GET /experience/location/{location}`

Get experiences filtered by location.

**Auth required:** Yes

---

#### `GET /experience/my-listings`

Get all experiences created by the current provider.

**Auth required:** Yes (Service Provider)

**Query params:** `page` (default: `1`), `limit` (default: `10`)

---

#### `PATCH /experience/update/{id}`

Update an experience listing.

**Auth required:** Yes (Service Provider, must own it)

**Request body:**
```json
{
  "title": "Updated Title",
  "description": "Updated description.",
  "category": "Adventure",
  "type": "Outdoor",
  "location": "Nuwara Eliya",
  "pricePerUnit": 3000.00,
  "priceUnit": "PER_PERSON",
  "maxCapacity": 8,
  "imageUrl": "https://example.com/new.jpg",
  "available": true
}
```

---

#### `DELETE /experience/delete/{id}`

Delete an experience listing.

**Auth required:** Yes (Service Provider, must own it)

---

#### `GET /experience/admin/all`

Get all experiences (any status), paginated and filterable.

**Auth required:** Yes (Admin)

**Query params:** `filter` (default: `all`), `page` (default: `1`), `limit` (default: `10`)

---

#### `PUT /experience/admin/{id}`

Admin update of any experience.

**Auth required:** Yes (Admin)

**Request body:** Same as `PATCH /experience/update/{id}`

---

#### `PUT /experience/admin/avaible/{id}`

Admin toggle of an experience's availability.

**Auth required:** Yes (Admin)

**Request body:** Same as update.

---

#### `GET /experience/admin/stats`

Get experience and booking statistics.

**Auth required:** Yes (Admin)

**Response:**
```json
{
  "totalExperiences": 30,
  "availableExperiences": 25,
  "totalBookings": 200,
  "pendingBookings": 12
}
```

---

### Experience Booking

> `experience-service` · Prefix: `/experience/booking`

---

#### `POST /experience/booking/request`

Submit an experience booking request.

**Auth required:** Yes (Traveller)

**Request body:**
```json
{
  "experienceId": 5,
  "bookingDate": "2027-06-15",
  "startTime": "06:00",
  "requestMessage": "Please arrange transport from Ella station.",
  "quantity": 2,
  "durationHours": 4
}
```

> Date: `YYYY-MM-DD` · Time: `HH:mm`

**Response:** `201 Created`

---

#### `PATCH /experience/booking/{bookingId}/respond`

Provider responds to a booking request.

**Auth required:** Yes (Service Provider)

**Request body:**
```json
{
  "status": "ACCEPTED",
  "message": "Confirmed! Meet at the trailhead at 6 AM."
}
```

> `status` accepts: `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `COMPLETED`

---

#### `DELETE /experience/booking/{bookingId}/cancel`

Cancel an experience booking.

**Auth required:** Yes

---

#### `GET /experience/booking/{bookingId}`

Get a specific booking by ID.

**Auth required:** Yes

---

#### `GET /experience/booking/my-bookings`

Get all bookings made by the current traveller (paginated via Pageable query params).

**Auth required:** Yes (Traveller)

**Query params:** `page`, `size`, `sort` (Spring Pageable)

---

#### `GET /experience/booking/provider/all`

Get all bookings for the current provider (paginated).

**Auth required:** Yes (Service Provider)

**Query params:** `page`, `size`, `sort` (Spring Pageable)

---

#### `GET /experience/booking/provider/pending`

Get all pending bookings for the current provider.

**Auth required:** Yes (Service Provider)

---

### Experience Reviews

> `experience-service` · Prefix: `/experience/review`

---

#### `POST /experience/review`

Submit a review for an experience.

**Auth required:** Yes (Traveller)

**Request body:**
```json
{
  "experienceId": 5,
  "rating": 5,
  "comment": "Absolutely incredible. Highly recommend!"
}
```

> `rating` must be between `1` and `5`.

**Response:** `201 Created`

---

#### `PUT /experience/review/{reviewId}`

Update an existing review.

**Auth required:** Yes (Traveller, must own the review)

**Request body:** Same as create.

---

#### `DELETE /experience/review/{reviewId}`

Delete a review.

**Auth required:** Yes (Traveller, must own the review)

---

#### `GET /experience/review/experience/{experienceId}`

Get all reviews for a specific experience.

**Auth required:** Yes

---

#### `GET /experience/review/my-reviews`

Get all reviews submitted by the current traveller.

**Auth required:** Yes (Traveller)

---

#### `GET /experience/review/summary/{experienceId}`

Get a rating summary (average + count) for an experience.

**Auth required:** Yes

**Response:**
```json
{
  "experienceId": 5,
  "averageRating": 4.7,
  "totalReviews": 23
}
```

---

### Review Service

> `review-service` · Prefix: `/api/reviews`

This is a standalone service for transport reviews.

---

#### `POST /api/reviews`

Save a new transport review.

**Auth required:** No

**Request body:**
```json
{
  "transportId": 3,
  "rating": 4,
  "comment": "Very comfortable ride."
}
```

---

#### `GET /api/reviews`

Get all reviews in the system.

**Auth required:** No

---

#### `GET /api/reviews/transport/{id}`

Get all reviews for a specific transport.

**Auth required:** No

---

#### `DELETE /api/reviews/{id}`

Delete a review by ID.

**Auth required:** No

---

## Quick Reference

### Auth & User

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/auth/signup` | No | — | Register account |
| `PATCH` | `/auth/signup` | Yes | Any | Complete profile |
| `POST` | `/auth/login` | No | — | Login, get tokens |
| `GET` | `/auth/refresh` | Cookie | Any | Refresh access token |
| `GET` | `/user/me` | Yes | Any | Get JWT claims |
| `GET` | `/user/profile` | Yes | Any | Get full profile |
| `PATCH` | `/user/update-profile` | Yes | Any | Update profile |
| `GET` | `/user/admin/all-travellers` | Yes | Admin | List all travellers |
| `GET` | `/user/admin/all-providers` | Yes | Admin | List all providers |
| `PATCH` | `/user/admin/edit-user` | Yes | Admin | Edit any user |
| `PATCH` | `/user/admin/edit-user-role` | Yes | Admin | Change user role |
| `GET` | `/user/admin/traveller/{id}` | Yes | Admin | Get traveller info |
| `GET` | `/user/admin/provider/{id}` | Yes | Admin | Get provider info |

### Hotel

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/hotel-service` | Yes | Provider | Create hotel |
| `GET` | `/hotel-service` | Yes | Any | List active hotels |
| `GET` | `/hotel-service/my` | Yes | Provider | My listings |
| `GET` | `/hotel-service/{id}` | Yes | Any | Get by ID |
| `GET` | `/hotel-service/traveller/{id}` | Yes | Traveller | Traveller view |
| `GET` | `/hotel-service/provider/{id}` | Yes | Provider | Provider view |
| `PUT` | `/hotel-service/{id}` | Yes | Provider | Update listing |
| `DELETE` | `/hotel-service/{id}` | Yes | Provider | Delete listing |
| `GET` | `/hotel-service/admin/all` | Yes | Admin | All hotels |
| `GET` | `/hotel-service/admin/pending` | Yes | Admin | Pending hotels |
| `PUT` | `/hotel-service/admin/{id}` | Yes | Admin | Admin update |
| `PUT` | `/hotel-service/admin/status/{id}` | Yes | Admin | Update status |
| `POST` | `/hotel-booking/request` | Yes | Traveller | Request booking |
| `PATCH` | `/hotel-booking/{id}/respond` | Yes | Provider | Accept/decline |
| `DELETE` | `/hotel-booking/{id}/cancel` | Yes | Any | Cancel booking |
| `GET` | `/hotel-booking/my-bookings` | Yes | Traveller | My bookings |
| `GET` | `/hotel-booking/incoming-requests` | Yes | Provider | Incoming requests |

### Restaurant

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/restaurant-service` | Yes | Provider | Create restaurant |
| `GET` | `/restaurant-service` | Yes | Any | List active |
| `GET` | `/restaurant-service/my` | Yes | Provider | My listings |
| `GET` | `/restaurant-service/{id}` | Yes | Any | Get by ID |
| `PUT` | `/restaurant-service/{id}` | Yes | Provider | Update listing |
| `DELETE` | `/restaurant-service/{id}` | Yes | Provider | Delete listing |
| `GET` | `/restaurant-service/admin/all` | Yes | Admin | All restaurants |
| `GET` | `/restaurant-service/admin/pending` | Yes | Admin | Pending |
| `PUT` | `/restaurant-service/admin/{id}` | Yes | Admin | Admin update |
| `PUT` | `/restaurant-service/admin/status/{id}` | Yes | Admin | Update status |
| `POST` | `/restaurant-booking/request` | Yes | Traveller | Request booking |
| `PATCH` | `/restaurant-booking/{id}/respond` | Yes | Provider | Accept/decline |
| `DELETE` | `/restaurant-booking/{id}/cancel` | Yes | Any | Cancel |
| `GET` | `/restaurant-booking/my-bookings` | Yes | Traveller | My bookings |
| `GET` | `/restaurant-booking/incoming-requests` | Yes | Provider | Incoming |

### Transport

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/transport-service` | Yes | Provider | Create transport |
| `GET` | `/transport-service` | Yes | Any | List active |
| `GET` | `/transport-service/search` | Yes | Any | Search by city |
| `GET` | `/transport-service/my` | Yes | Provider | My listings |
| `GET` | `/transport-service/{id}` | Yes | Any | Get by ID |
| `PUT` | `/transport-service/{id}` | Yes | Provider | Update listing |
| `DELETE` | `/transport-service/{id}` | Yes | Provider | Delete listing |
| `GET` | `/transport-service/admin/all` | Yes | Admin | All transports |
| `GET` | `/transport-service/admin/pending` | Yes | Admin | Pending |
| `PATCH` | `/transport-service/admin/{id}/approve` | Yes | Admin | Approve |
| `GET` | `/transport-service/admin/stats` | Yes | Admin | Stats |
| `POST` | `/transport-service/bookings/request` | Yes | Traveller | Request booking |
| `PATCH` | `/transport-service/bookings/{id}/respond` | Yes | Provider | Accept/decline |
| `PATCH` | `/transport-service/bookings/{id}/cancel` | Yes | Any | Cancel |
| `PATCH` | `/transport-service/bookings/{id}/complete` | Yes | Provider | Complete |
| `GET` | `/transport-service/bookings/my-bookings` | Yes | Traveller | My bookings |
| `GET` | `/transport-service/bookings/provider-requests` | Yes | Provider | Incoming |

### Experience

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/experience/create` | Yes | Provider | Create experience |
| `GET` | `/experience/all` | Yes | Any | All experiences |
| `GET` | `/experience/available` | Yes | Any | Available only |
| `GET` | `/experience/{id}` | Yes | Any | Get by ID |
| `GET` | `/experience/category/{category}` | Yes | Any | By category |
| `GET` | `/experience/location/{location}` | Yes | Any | By location |
| `GET` | `/experience/my-listings` | Yes | Provider | My listings |
| `PATCH` | `/experience/update/{id}` | Yes | Provider | Update |
| `DELETE` | `/experience/delete/{id}` | Yes | Provider | Delete |
| `GET` | `/experience/admin/all` | Yes | Admin | All (any status) |
| `PUT` | `/experience/admin/{id}` | Yes | Admin | Admin update |
| `PUT` | `/experience/admin/avaible/{id}` | Yes | Admin | Toggle availability |
| `GET` | `/experience/admin/stats` | Yes | Admin | Stats |
| `POST` | `/experience/booking/request` | Yes | Traveller | Book experience |
| `PATCH` | `/experience/booking/{id}/respond` | Yes | Provider | Accept/decline |
| `DELETE` | `/experience/booking/{id}/cancel` | Yes | Any | Cancel |
| `GET` | `/experience/booking/{id}` | Yes | Any | Get booking |
| `GET` | `/experience/booking/my-bookings` | Yes | Traveller | My bookings |
| `GET` | `/experience/booking/provider/all` | Yes | Provider | All provider bookings |
| `GET` | `/experience/booking/provider/pending` | Yes | Provider | Pending bookings |
| `POST` | `/experience/review` | Yes | Traveller | Submit review |
| `PUT` | `/experience/review/{id}` | Yes | Traveller | Update review |
| `DELETE` | `/experience/review/{id}` | Yes | Traveller | Delete review |
| `GET` | `/experience/review/experience/{id}` | Yes | Any | Reviews by experience |
| `GET` | `/experience/review/my-reviews` | Yes | Traveller | My reviews |
| `GET` | `/experience/review/summary/{id}` | Yes | Any | Rating summary |

### Transport Reviews

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/reviews` | No | Save a review |
| `GET` | `/api/reviews` | No | All reviews |
| `GET` | `/api/reviews/transport/{id}` | No | Reviews by transport |
| `DELETE` | `/api/reviews/{id}` | No | Delete review |
