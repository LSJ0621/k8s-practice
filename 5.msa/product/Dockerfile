# 필요프로그램 설치
From openjdk:17-jdk-alpine as stage1
# 파일 복사(Copy ~파일을 ~이름으로 복사하겠다. , .인경우에는 WORKDIR/app 현재경로에 복사하겠다.)
WORKDIR /app
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
# 빌드
RUN chmod +x gradlew
RUN ./gradlew bootJar

# 두번째 스테이지
From openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=stage1 /app/build/libs/*.jar app.jar
# 실행: CMD 또는 ENTRYPOINT를 통해 컨테이너를 배열 형태의 명령어로 실행
# ENTRYPOINT ["java", "-jar","/app/build/libs/ordersystem-0.0.1-SNAPSHOT.jar"]
ENTRYPOINT ["java", "-jar","app.jar"]

# docker run --name my-spring -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem -e SPRING_REDIS_HOST=host.docker.internal my-spring:v1.0