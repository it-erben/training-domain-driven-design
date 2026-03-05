# Lab 07: REST Adapter - Viewings API

## Learning Objective

Implement a `@RestController` as an inbound adapter, define DTOs for the API boundary, and implement error handling with `ProblemDetail` (RFC 9457).

## Duration

45 minutes

## Prerequisites

- Lab 06 completed
- Slides Module 09

## Task

Implement a REST adapter that receives HTTP requests, translates them into commands, and delegates to the use case.

### Step 1: Create the Request DTO

Create the request DTO `CreateViewingRequest` as a Java Record in the package `de.realestate.brokerage.adapter.web`:

```java
public record CreateViewingRequest(
    @NotBlank String prospectName,
    @NotNull LocalDateTime appointmentDate
) {}
```

**Note:** The validation annotations (`@NotBlank`, `@NotNull`) belong to the adapter layer - the domain model validates itself.

### Step 2: Create the Response DTO

Create the response DTO `CreateViewingResponse` as a Java Record in the same package:

```java
public record CreateViewingResponse(
    UUID viewingId,
    UUID processId
) {}
```

### Step 3: Implement the Controller

Create the `ViewingController` in the package `de.realestate.brokerage.adapter.web`:

```java
@RestController
@RequestMapping("/api/brokerage/processes/{processId}/viewings")
public class ViewingController {

    private final CreateViewingUseCase createViewingUseCase;

    // Constructor Injection

    @PostMapping
    public ResponseEntity<CreateViewingResponse> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateViewingRequest request) {
        // 1. Map request DTO to command
        // 2. Call use case
        // 3. Map result to response DTO
        // 4. Return 201 Created with Location header
    }
}
```

**Important:** The controller contains no business logic. It is a pure adapter that translates between HTTP and the application layer.

### Step 4: Implement the Exception Handler

Create the `GlobalExceptionHandler` in the package `de.realestate.brokerage.adapter.web`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProcessNotFoundException.class)
    public ProblemDetail handleNotFound(ProcessNotFoundException ex) {
        // Return ProblemDetail with status 404 and error message
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // Return ProblemDetail with status 422 and validation errors
    }
}
```

**Note:** `ProblemDetail` is natively supported since Spring Boot 4 and implements RFC 9457 (formerly RFC 7807).

### Step 5: Test with curl

Start the application and test the endpoints (see Verification).

### Bonus: GET Endpoint

Implement a GET endpoint that lists all viewings of a brokerage process:

```java
@GetMapping
public List<CreateViewingResponse> list(@PathVariable UUID processId) {
    // Load BrokerageProcess and return viewings as response DTOs
}
```

## Verification

Start the application and run the following curl commands:

### Create viewing (expected: 201 Created)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/{processId}/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "Max Mustermann",
    "appointmentDate": "2025-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Expected response: HTTP 201, JSON with `viewingId` and `processId`.

### Use a non-existing process (expected: 404 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/00000000-0000-0000-0000-000000000000/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "Max Mustermann",
    "appointmentDate": "2025-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Expected response: HTTP 404, ProblemDetail JSON:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "BrokerageProcess with ID 00000000-0000-0000-0000-000000000000 not found"
}
```

### Validation error (expected: 422 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/{processId}/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "",
    "appointmentDate": null
  }' \
  -w "\n%{http_code}\n"
```

Expected response: HTTP 422, ProblemDetail JSON with validation errors.

## Tips

- The controller is an inbound adapter in Clean Architecture terminology. It depends on the application layer, not the other way around.
- DTOs (Request/Response) belong to the adapter layer and are **not** used in the domain or application layer.
- `ProblemDetail` is the standard for error responses in REST APIs and is natively supported by Spring Boot 4.
- The `Location` header in the 201 response tells the client where the newly created resource can be found.
