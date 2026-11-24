# Imagen unica de Pantera Fitness: frontend Next.js (estatico) + API Spring Boot.
# Se usa para el hosting gratuito (Render). La base de datos es Supabase (PostgreSQL).

# Etapa 1: compilar el frontend como sitio estatico (carpeta out/)
FROM node:22-alpine AS frontend
WORKDIR /build/panterafitnes
COPY panterafitnes/package.json panterafitnes/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY panterafitnes/ ./
ENV NEXT_TELEMETRY_DISABLED=1 \
    NEXT_PUBLIC_API_URL=""
RUN npm run build

# Etapa 2: compilar el backend e incluir el frontend dentro del JAR
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /build/panterafitnes-backend
COPY panterafitnes-backend/ ./
COPY --from=frontend /build/panterafitnes/out ../panterafitnes/out
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar -PwithFrontend -x test

# Etapa 3: imagen final liviana, solo JRE, usuario sin privilegios
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 pantera
COPY --from=backend /build/panterafitnes-backend/build/libs/pantera-fitness.jar app.jar
USER pantera
# Ajustado al plan gratuito (512 MB de RAM)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
