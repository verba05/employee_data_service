# Employee Data Service

## How to run locally

1. **Download and extract the project** to a folder on your machine, then navigate into it:
```bash
   cd employee_data_service
```

2. **Create a `.env` file** in the project's root folder with the following variables:

   | Variable | Description |
   |---|---|
   | `POSTGRES_USER` | Username for the PostgreSQL database |
   | `POSTGRES_PASSWORD` | Password for the PostgreSQL database |
   | `POSTGRES_DB` | Name of the database to create |
   | `POSTGRES_URL` | JDBC connection URL used by the app to reach the database |
   | `ENCRYPTION_KEY` | Base64-encoded AES-256 key used to encrypt/decrypt the SSN field |

   Example `.env` file (ready to copy and use for local testing):
```dotenv
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=employee-data-service-db
POSTGRES_URL=jdbc:postgresql://postgres:5432/employee-data-service-db
ENCRYPTION_KEY=mRdTor//Lh8Np+qQQu+o9XA/MJaSFyYl4eE/AWeVWn0=
```

3. **Start the whole app** (database + Spring Boot service) with a single command:
```bash
   docker compose up -d
```

4. The API will be available at `http://localhost:8080/employees`.

## Technology choices

- **Spring Boot / Spring Web** — standard and fast to build a REST API with, backed by a large ecosystem that automates a lot of processes instead of writing it by hand.
- **Lombok** — a Java library that adds annotations to auto-generate repetitive code (getters, setters, constructors), making the codebase shorter and easier to read.
- **PostgreSQL** — reliable, free, well-supported relational database. A solid, standard choice for this kind of data.
- **Encryption for SSN** — SSNs are automaticly encrypted at the field level before being stored and decrypted after getting it from db, using a custom `EncryptionConverter`. Encryption was chosen over hashing because, unlike hashing, it allows the original value to be recovered later. Hashing only lets you check whether a given value matches what's stored — it can't reverse back to the original. Since the SSN may need to be read back (not just verified) as the project grows, encryption is the right fit.
- **Jakarta Bean Validation (`@NotBlank`, `@Pattern`, `@Past`, etc.)** — validation rules live directly on the entity fields instead of being hand-written per controller. This keeps validation centralized: any future endpoint that accepts the same entity automatically inherits the same rules, keeping the code more readable and reusable.
- **JUnit 5 + Mockito** — unit tests for the controller, service, encryption logic, and validation constraints. This allows testing a class in isolation, without needing a real database or any of its dependencies to already be implemented.

## What I'd do differently with more time

- Add authentication/authorization using JWT.
- Once authentication/authorization exists, add a separate endpoint for getting the SSN from backend, encrypted in transit similarly to how HTTPS works.
- Support Unicode characters in name validation (currently ASCII-only).
- Move the encryption key and DB password out of `.env` and into a proper secrets manager (e.g. AWS Secrets Manager, Vault) for anything beyond local development.

## AI tool usage

I used Claude and GitHub Copilot as coding assistants throughout this project.

I used them for:
- Building the SSN encryption converter and figuring out the right approach for encrypting/decrypting automatically.
- Writing and refining `EmployeeController` and `EmployeeService`, going back and forth a few times until the behavior matched what I wanted.
- Adding data format restriction annotations to the entity and adding validation functions for checking it to the controller.
- Generating unit tests for the controller, service, and encryption logic, then adjusting them as the code changed.
- Simplifying `EmployeeService`'s update logic from two separate methods for partial and full updates to a just one with universal update function.

**One thing I changed/rejected:** When I asked Claude how to structure the SSN encryption, it suggested writing a separate service class that would manually encrypt and decrypt the value wherever needed. I didn't like that idea, because it puts the responsibility on the programmer to remember to call encrypt/decrypt every single time the SSN is touched, which is easy to forget, especially as the codebase grows. Instead, I went with a JPA `AttributeConverter` (`@Convert`), which handles the encryption and decryption automatically every time the field is read from or written to the database.