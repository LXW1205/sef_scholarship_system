# ── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy source files and dependencies
COPY src/ ./src/
COPY lib/ ./lib/

# Compile all Java sources
RUN find src -name "*.java" > sources.txt \
    && mkdir -p bin \
    && javac -d bin -cp "lib/*" @sources.txt \
    && rm sources.txt

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy compiled classes, libraries, and frontend static files
COPY --from=builder /app/bin ./bin
COPY --from=builder /app/lib ./lib
COPY www/ ./www/
COPY sql_queries/ ./sql_queries/

# Render sets the PORT env var — default to 8080 for local Docker runs
EXPOSE 8080

# Start the Java HTTP server
CMD ["java", "-cp", "bin:lib/*", "Main"]