# Registration Form – Unit Tests

> **QA Assignment** – Create New Account Page Validation  
> JUnit 5 · Maven · GitHub Actions

---

## Project Structure

```
registration-tests/
├── .github/
│   └── workflows/
│       └── ci.yml                  ← GitHub Actions pipeline
├── src/
│   ├── main/java/com/registration/
│   │   ├── RegistrationForm.java    ← Data model (form fields)
│   │   ├── RegistrationValidator.java ← Business logic (class under test)
│   │   └── ValidationResult.java   ← Validation output holder
│   └── test/java/com/registration/
│       └── RegistrationValidatorTest.java ← 25 unit tests
└── pom.xml
```

---

## Test Cases Summary

| #    | Field            | Technique | Description                            | Expected  |
|------|------------------|-----------|----------------------------------------|-----------|
| TC-01 | All fields       | EP        | All valid inputs                       | PASS ✅   |
| TC-02 | First Name       | EP        | Null value                             | FAIL ❌   |
| TC-03 | First Name       | BVA       | 1 char (below min=2)                   | FAIL ❌   |
| TC-04 | First Name       | BVA       | 2 chars (at min boundary)              | PASS ✅   |
| TC-05 | First Name       | BVA       | 51 chars (above max=50)                | FAIL ❌   |
| TC-06 | First Name       | EP        | Contains digits                        | FAIL ❌   |
| TC-07 | Email            | EP        | Multiple invalid formats (parameterized)| FAIL ❌  |
| TC-08 | Email            | EP        | Valid subdomain email                  | PASS ✅   |
| TC-09 | Email            | BVA       | Exactly 100 chars (max boundary)       | PASS ✅   |
| TC-10 | Email            | BVA       | 101 chars (above max)                  | FAIL ❌   |
| TC-11 | Date of Birth    | EP        | Wrong format (yyyy-mm-dd)              | FAIL ❌   |
| TC-12 | Date of Birth    | BVA       | Exactly age 13 (min boundary)          | PASS ✅   |
| TC-13 | Date of Birth    | BVA       | Age 12 (below min)                     | FAIL ❌   |
| TC-14 | Date of Birth    | BVA       | Age > 120 (unrealistic)                | FAIL ❌   |
| TC-15 | Password         | BVA       | 7 chars (below min=8)                  | FAIL ❌   |
| TC-16 | Password         | BVA       | 8 chars (at min boundary)              | PASS ✅   |
| TC-17 | Password         | EP        | Missing special character              | FAIL ❌   |
| TC-18 | Password         | EP        | All lowercase                          | FAIL ❌   |
| TC-19 | Password         | EP        | Multiple weak passwords (parameterized)| FAIL ❌   |
| TC-20 | Confirm Password | EP        | Passwords don't match                  | FAIL ❌   |
| TC-21 | Confirm Password | EP        | Confirm field empty                    | FAIL ❌   |
| TC-22 | All fields       | EP        | All blank → multiple errors            | FAIL ❌   |
| TC-23 | First Name       | EP        | SQL injection attempt                  | FAIL ❌   |
| TC-24 | Email            | EP        | XSS script tag                         | FAIL ❌   |
| TC-25 | Password         | BVA       | Exactly 64 chars (max boundary)        | PASS ✅   |

**Total: 25 test cases** (15 required + 10 extra)

---

## How to Run Locally

### Prerequisites
- Java 11+
- Maven 3.8+

### Run all tests
```bash
mvn test
```

### View HTML report
```
open target/surefire-reports/*.txt
# or open the HTML version with mvn surefire-report:report
```

---

## GitHub Actions Setup

1. Push this project to a GitHub repository.
2. The workflow at `.github/workflows/ci.yml` triggers automatically on:
   - Every `push` to `main`, `develop`, or `feature/**` branches
   - Every Pull Request targeting `main`
3. After the run, go to **Actions → Build & Run Unit Tests → Artifacts** to download the Surefire test report.

### Workflow Steps
```
Checkout → Set up JDK 11 → Compile → Run Tests → Upload Report
```

---

## Techniques Used

| Technique | Definition | Example in this project |
|---|---|---|
| **Equivalence Partitioning** | Divide inputs into valid/invalid groups; one test per group | TC-07: invalid email formats |
| **Boundary Value Analysis** | Test at exact boundaries (min, max, min-1, max+1) | TC-03/04/05: name length 1,2,51 |
| **Parameterized Testing** | One test method, multiple data sets | TC-07, TC-19 |
| **Setup & Teardown** | `@BeforeEach` / `@AfterEach` for resource management | Creates fresh validator per test |
