# Table Reservation

## About this project

I built this application to make restaurant table reservations easier for guests and day-to-day table management easier for staff. Guests can create an account or sign in with Google, look for tables by date, time, and party size, make a reservation, and review or cancel their bookings. Staff can review reservations for a selected day and manage which tables are available.

The application is currently under development and has not been deployed yet. The restaurant name and visual identity, Olive & Ember, are placeholders for the presentation design.

## Features

- Guest registration and email/password sign-in
- Google sign-in using OAuth 2.0 and OpenID Connect
- Table availability search by date, time, and number of guests
- Reservation creation, review, and cancellation
- Staff reservation list with date filtering and cancellation
- Staff tools to add, pause, and reactivate restaurant tables
- Role-based access for guests and staff
- Responsive pages built with Thymeleaf and CSS

## How the application works

I use Spring MVC to handle browser requests. A controller receives a request, calls the service or repository it needs, and returns a Thymeleaf page. Thymeleaf combines that page with data from the application, and the browser applies the shared stylesheet.

For a reservation, the search page asks for a date, one of the available time slots, and the party size. The table repository finds active tables with enough seats that do not already have a confirmed reservation for that slot. When a guest submits a booking, the service checks availability again before saving it. This second check helps prevent a table from being booked twice if its availability changed after the search.

I store users, tables, and reservations with Spring Data JPA. The default local profile uses an in-memory H2 database. I can use the PostgreSQL profile for persistent local development or deployment preparation.

### Main application flow

    Browser request
        → Spring MVC controller
        → service logic, when a business rule is needed
        → Spring Data repository
        → H2 or PostgreSQL database
        → Thymeleaf page rendered for the browser

Spring Security handles sign-in and access rules. Guests have the USER role. Staff pages require the ADMIN role. Google confirms the user’s identity; the application then finds or creates the matching user record and uses the role stored in the application database.

## User workflows

### Guest workflow

1. A visitor opens the public tables page.
2. The visitor creates an account or signs in with email and password or Google.
3. After signing in, the guest searches by date, time, and number of guests.
4. The application shows tables that have enough seats and are available for that time.
5. The guest books a table and can see the reservation on the My reservations page.
6. The guest can cancel a confirmed reservation. The reservation stays in the database with a CANCELLED status, and the table becomes available again.

The available time slots are currently 16:00, 18:00, and 20:00. Each booking represents a two-hour reservation.

### Staff workflow

1. A staff member with the ADMIN role signs in.
2. Staff can review reservations for a selected date and cancel a booking when needed.
3. Staff can add a table or pause/reactivate an existing table.
4. Paused tables are excluded from guest availability searches.

Creating an account through the public registration form creates a guest account with the USER role. Staff access must be assigned to a user through the database; there is no public staff registration.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring MVC and Thymeleaf
- Spring Security, including Google OAuth 2.0 / OpenID Connect
- Spring Data JPA
- H2 for the default local profile
- PostgreSQL for the postgres profile
- Maven Wrapper

## Run locally on Windows

You need JDK 21. From the project folder, run:

~~~powershell
.\mvnw.cmd spring-boot:run
~~~

Then open http://localhost:8081/tables. The application listens on port 8081.

The default H2 database is in memory, and the default profile uses create-drop for the schema. Its data is cleared when the application stops.

### Google sign-in setup

The application reads the Google OAuth credentials from environment variables. I do not store the client secret in the source code.

1. Create a Web application OAuth client in Google Cloud.
2. Add this authorized redirect URI for local development: http://localhost:8081/login/oauth2/code/google
3. Set both variables in the same PowerShell session used to start the application:

~~~powershell
$env:GOOGLE_CLIENT_ID = "your-client-id"
$env:GOOGLE_CLIENT_SECRET = "your-client-secret"
.\mvnw.cmd spring-boot:run
~~~

Without real Google credentials, the rest of the application can start, but Google sign-in will not work. Never commit the real client secret.

### Use PostgreSQL locally

Docker is optional. The included compose file starts a local PostgreSQL database. In one PowerShell session, run:

~~~powershell
docker compose up -d
.\mvnw.cmd -Dspring-boot.run.profiles=postgres spring-boot:run
~~~

The PostgreSQL profile keeps data between application restarts. The included database name and password are for local development only; replace them with managed secrets and deployment-specific settings before using a hosted environment.

## Run tests

I can run the automated test suite from the project folder with:

~~~powershell
.\mvnw.cmd test
~~~

The tests cover repository queries, reservation service behavior, application startup, and security access.
## Project structure

- src/main/java/.../web — browser routes and page controllers
- src/main/java/.../service — reservation business rules
- src/main/java/.../repository — database queries and persistence
- src/main/java/.../domain — user, role, table, and reservation models
- src/main/java/.../security — login, role access, and Google account handling
- src/main/resources/templates — Thymeleaf pages and shared fragments
- src/main/resources/static/css — shared styles
- src/test/java — repository, service, application, and security tests

## How I would present the project

I built a full-stack table reservation application with Java and Spring Boot. Spring MVC handles requests, Spring Data JPA stores users, tables, and reservations, and Thymeleaf renders the pages. Guests can register or use Google sign-in, find an available table, and manage their reservations. Staff members have a separate role that lets them manage tables and daily reservations. I have also started improving the frontend with reusable Thymeleaf fragments and responsive CSS, while keeping the existing booking and security workflows.

## Development record

### Milestone 1 — Visual foundation

I added a warm restaurant-style design, shared navigation and footer fragments, and responsive layouts for the public tables page, search/results, sign-in, registration, guest reservations, and staff pages. I also made the navigation adapt to whether the visitor is signed in, and allowed public access to static CSS so the browser can render the design before login.

For the presentation, I can explain that Thymeleaf fragments reduce repeated page markup, CSS custom properties keep the visual style consistent, and Spring MVC supplies page data without moving the project to a separate frontend framework.

The application compiled successfully with tests skipped. A local HTTP check confirmed that the public tables page returns successfully and the stylesheet is served as CSS. A full visual review at desktop and mobile sizes is still pending.

### Next steps

- Review the pages at desktop and mobile sizes and refine the layout.
- Improve booking confirmation and date/time guidance.
- Prepare deployment configuration and the final presentation.

