# ===========================================
# Stage 1: Build with Maven (JDK 21)
# ===========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache
COPY pom.xml .

# Pre-download dependencies to cache them
RUN mvn dependency:go-offline -B

# Copy source code into container
COPY src ./src

# Verify ONNX model is not a Git LFS pointer (should be > 1MB)
RUN ONNX_SIZE=$(stat -c%s src/main/resources/onnx/model.onnx 2>/dev/null || echo "0") && \
    echo "ONNX model size: $ONNX_SIZE bytes" && \
    if [ "$ONNX_SIZE" -lt 1000000 ]; then \
        echo "ERROR: ONNX model is too small ($ONNX_SIZE bytes). Git LFS files were not pulled!" && \
        echo "The model.onnx file is a Git LFS pointer, not the actual model." && \
        echo "Please ensure Git LFS is configured on Railway or upload the actual model file." && \
        cat src/main/resources/onnx/model.onnx && \
        exit 1; \
    fi

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# ===========================================
# Stage 2: Runtime (JRE 21 - Debian for ONNX Runtime compatibility)
# ===========================================
FROM eclipse-temurin:21-jre-jammy

# Set timezone to Vietnam
ENV TZ=Asia/Ho_Chi_Minh
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata libgomp1 && \
    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run with percentage-based memory allocation for Railway
# Requires at least 1.5GB memory in Railway settings
ENTRYPOINT ["java", \
    "-XX:InitialRAMPercentage=40", \
    "-XX:MaxRAMPercentage=70", \
    "-XX:MaxMetaspaceSize=256m", \
    "-XX:+UseG1GC", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxGCPauseMillis=200", \
    "-Duser.timezone=Asia/Ho_Chi_Minh", \
    "-jar", "app.jar"]
