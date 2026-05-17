# ─── Stage 1: Build ───────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ─── Stage 2: Run ─────────────────────────────────────────────
#FROM eclipse-temurin:17-jre-alpine
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Add non-root user for security
RUN #addgroup -S banking && adduser -S banking -G banking
RUN addgroup --system banking && adduser --system --ingroup banking banking

USER banking

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dcom.sun.jndi.rmi.object.trustURLCodebase=false", \
  "-jar", "app.jar"]
