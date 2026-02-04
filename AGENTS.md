# Repository Guidelines

## Project Structure & Module Organization
- `hr-ai-common/`: shared utilities and constants for backend services.
- `hr-ai-agent-core/`: core agent service and shared business assets.
- `hr-gateway/`: API gateway.
- `hr-org-service/`: organization/permission service; tests live under `hr-org-service/src/test/java/...`.
- `hr-ai-web/`: React + Vite frontend; entry point is `hr-ai-web/src/main.tsx`.
- `docs/`: product specs, plans, and design notes.
- `scripts/` and `docker-compose.yml`: local tooling and infrastructure helpers.

## Build, Test, and Development Commands
Backend (Maven):
- `mvn clean package -DskipTests` builds all Maven modules.
- `mvn spring-boot:run` runs a Spring Boot service from its module directory.

Frontend (Vite):
- `npm run dev` starts the dev server in `hr-ai-web/`.
- `npm run build` produces the production bundle.
- `npm run lint` runs ESLint.
- `npm run preview` previews a production build.

## Coding Style & Naming Conventions
- Java uses `com.hrai.*` packages (for example, controllers in `hr-org-service/src/main/java/com/hrai/org/controller/`).
- Java code follows 4-space indentation; TypeScript/TSX follows 2-space indentation (see `hr-ai-web/src/main.tsx`).
- React components use `*.tsx` with PascalCase filenames (for example, `App.tsx`), while page entries typically use `index.tsx` under `hr-ai-web/src/pages/`.

## Testing Guidelines
- Backend tests are in `hr-org-service/src/test/java/...` and follow `*Test.java` naming. Run with `mvn test`.
- Frontend verification is via `npm run lint` and `npm run build` scripts.

## Commit & Pull Request Guidelines
- The project README recommends: fork, create a feature branch, commit, push, and open a PR.
- Recent history uses short, descriptive Chinese summaries (for example, “完善网关和鉴权功能前后端代码”), while the README’s example is `git commit -m 'Add some AmazingFeature'`.

## Docs & References
- Start with `PROJECT_README.md`, `QUICKSTART.md`, and `PROGRESS.md` for onboarding.
- Feature requirements and designs live in `docs/`.
