# Production Dockerfile for Render Deployment
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy all project files
COPY . /app

# Compile Java source files
RUN javac -cp ".:mysql-connector-j-8.3.0.jar:*" java/*.java

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Copy built application from build stage
COPY --from=build /app /app

# Expose default port
EXPOSE 8080

# Run Java Web Application
CMD ["java", "-cp", ".:java:mysql-connector-j-8.3.0.jar:*", "Main"]
