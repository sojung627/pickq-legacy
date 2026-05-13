# =========================
# Build Stage
# =========================
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# gradle build 실행 (test 제외)
RUN gradle build -x test

# =========================
# Run Stage
# =========================
FROM eclipse-temurin:21

WORKDIR /app

# build 결과 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Railway 포트
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]