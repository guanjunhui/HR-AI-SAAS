# Repository Guidelines

## Project Structure & Module Organization
- Multi-module Maven project.
- `hr-ai-common/`: shared utilities, DTOs, constants, exceptions (`src/main/java/com/hrai/common`).
- `hr-ai-agent-core/`: main Spring Boot service (`src/main/java/com/hrai/agent`).
- Config & data: `hr-ai-agent-core/src/main/resources/application.yml` and `hr-ai-agent-core/src/main/resources/db/`.
- Local specs/notes live under `docs/` (e.g., `docs/更换技术栈/`, `docs/更换技术栈/2026-01-29-V1/`).
- Quick references: `PROJECT_README.md`, `QUICKSTART.md`, and `docker-compose.yml`.

## Build, Test, and Development Commands
- `./scripts/verify-setup.sh`: sanity-check structure and run a quick Maven compile.
- `./scripts/start-dev.sh`: start MySQL/Redis/Qdrant/RabbitMQ via Docker Compose.
- `./scripts/stop-dev.sh`: stop local dependency containers.
- `mvn clean package -DskipTests`: build all modules.
- `mvn test`: run unit tests (when present).
- `cd hr-ai-agent-core && mvn spring-boot:run`: run the main service locally.

## Coding Style & Naming Conventions
- Java 17, Spring Boot conventions.
- Indentation: 4 spaces; braces on same line.
- Names: `UpperCamelCase` for classes, `lowerCamelCase` for methods/fields, `com.hrai...` package prefix.
- Keep configuration in `application.yml`; DB init scripts in `resources/db/`.

## Testing Guidelines
- Frameworks: Spring Boot Starter Test (JUnit 5 + Mockito).
- Place tests under `src/test/java` mirroring package structure; name tests `*Test`.
- No enforced coverage gate yet; add tests for new logic and bug fixes.

## Commit & Pull Request Guidelines
- Git history is not available in this workspace; use concise, imperative commit messages (or Conventional Commits if unsure).
- PRs should include: summary of changes, how to run/verify, and any config or schema updates. Add screenshots only when UI changes exist.

## Security & Configuration Tips
- Do not commit secrets (e.g., `DASHSCOPE_API_KEY`). Prefer environment variables or local overrides.
- If changing database schemas, update `resources/db/schema.sql` and note it in docs.
