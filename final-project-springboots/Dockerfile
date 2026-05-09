# Java 21 이미지 사용
FROM eclipse-temurin:21

# 작업 폴더 생성
WORKDIR /app

# jar 파일 복사
COPY build/libs/*.jar app.jar

# 8080 포트 오픈
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]