# OpenJDK 베이스 이미지를 사용합니다.
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 로컬에서 빌드된 JAR 파일을 Docker 이미지로 복사합니다.
COPY build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod

# JAR 파일을 실행합니다.
ENTRYPOINT ["java", "-Xms512m", "-Xmx768m", "-XX:+UseG1GC", "-jar", "app.jar"]

## docker build --platform linux/amd64 -t tonglink:0.0.5 .
## docker run -d -p 8082:8080 tonglink:0.0.5


