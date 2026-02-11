FROM amazoncorretto:17 AS build

WORKDIR /app

COPY gradlew build.gradle settings.gradle .
COPY gradle/ ./gradle/

RUN chmod +x ./gradlew

RUN ./gradlew dependencies || true

COPY src/ src/

RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:17-alpine-jdk AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java","-jar","app.jar"]
