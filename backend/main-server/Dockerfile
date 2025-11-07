FROM eclipse-temurin:21-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

ENV TZ=Asia/Seoul

# JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행 (prod 프로파일 사용, JVM 옵션 설정)
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-jar", "app.jar"]
