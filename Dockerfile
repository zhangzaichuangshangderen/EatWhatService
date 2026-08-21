FROM maven:3.6.0-jdk-8-slim AS build

WORKDIR /app

COPY src /app/src
COPY settings.xml pom.xml /app/

RUN mvn -s /app/settings.xml -f /app/pom.xml clean package

FROM alpine:3.13

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tencent.com/g' /etc/apk/repositories \
    && apk add --update --no-cache openjdk8-jre-base ca-certificates \
    && rm -f /var/cache/apk/*

WORKDIR /app

COPY --from=build /app/target/*.jar .

EXPOSE 8080

CMD ["java", "-jar", "/app/springboot-wxcloudrun-1.0.jar"]
