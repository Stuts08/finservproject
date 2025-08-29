# BFHL Java Qualifier — Spring Boot Solution

This app automates the Bajaj Finserv Health qualifier flow:

1. **On startup**, it calls `POST /hiring/generateWebhook/JAVA` with your name, regNo and email.
2. It **picks Question 1 or 2** based on the **last two digits** of `regNo` (odd ⇒ Q1, even ⇒ Q2).
3. It loads the SQL from `src/main/resources/queries/question{1,2}.sql`.
4. It **stores** the outcome in an **H2 DB** (`submission_result` table).
5. It **submits** the SQL to the provided **webhook URL** (or the fallback submit path) with `Authorization: <accessToken>`.

> **Note:** The PDF problem statements are not included here. Paste your final SQL into the appropriate file before running.

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Internet access for Maven dependencies

### Configure your candidate info
Edit `src/main/resources/application.yml`:

```yaml
app:
  candidate:
    name: "John Doe"
    regNo: "REG12347"   # last two digits decide the question
    email: "john@example.com"
```

### Add your final SQL
Open **one** of the files and paste your query (no semicolon needed):
- `src/main/resources/queries/question1.sql` (odd last-two digits)
- `src/main/resources/queries/question2.sql` (even last-two digits)

### Build
```bash
mvn -q -DskipTests package
```
This produces: `target/bfhl-java-qualifier-1.0.0.jar`

### Run
```bash
java -jar target/bfhl-java-qualifier-1.0.0.jar
```

On startup you should see logs for:
- Webhook generation
- Selected question
- Submission ACK (or error)

### H2 Console (optional)
- H2 console enabled at `/h2-console` (when running)
- JDBC URL: `jdbc:h2:mem:bfhl`, user: `sa`, password: (empty)

---

## Implementation Notes

- **WebClient** is used for both API calls.
- The **Authorization** header is set to the returned `accessToken` exactly as provided.
- A safety mask is stored for the token in DB (`XXXXXX...YYYY`).
- If the provided `webhook` is blank, the app posts to the documented fallback path: `/hiring/testWebhook/JAVA`.
- Outcome (success/error) is persisted in `submission_result` table.

---

## Submission Checklist

When you're ready:
1. Push this repo to GitHub (public).
2. Include **code**, **final JAR**, and **RAW downloadable** link to the JAR from GitHub releases or artifacts.
3. Provide links in the form as requested.

Good luck!
