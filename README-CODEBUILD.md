# CodeBuild / Docker build notes

This repository's CodeBuild pipeline (see `buildspec.yaml`) builds the application using Gradle and then packages a Docker image.

Key points:

- The Gradle build produces an executable jar into `build/libs/` (filename may vary, e.g. `activity-service-app-build.jar`).
- `buildspec.yaml` resolves the jar path after the Gradle `build` step using the first `*.jar` found in `build/libs`:

```bash
JAR_FILE=$(ls build/libs/*.jar | head -n 1)
```

- The Docker build is invoked with the resolved jar path passed as a Docker build-arg:

```bash
docker build --build-arg JAR_FILE="$JAR_FILE" -t $REPOSITORY_URI:latest .
```

- The `Dockerfile` reads the `JAR_FILE` build-arg and copies the jar into the image:

```dockerfile
ARG JAR_FILE
COPY ${JAR_FILE} /activity-service-app.jar
```

- This setup makes the pipeline robust if the build creates a jar whose name changes across builds or versions.

- Ensure the Gradle task produces a jar in `build/libs/` before Docker build runs (this is handled by `buildspec.yaml` which runs `./gradlew clean build`).

- If you prefer an explicit filename, you can set `JAR_FILE` in the CodeBuild environment variables or hardcode it in `buildspec.yaml`.

Troubleshooting
- If the Docker build fails with `COPY build/libs/*.jar: lstat /build/libs: no such file or directory`, ensure:
  - The Gradle build completed successfully and generated a jar in `build/libs`.
  - `.dockerignore` does not exclude `build/libs` or the jar file (this repo keeps only the built jar included via `.dockerignore`).


