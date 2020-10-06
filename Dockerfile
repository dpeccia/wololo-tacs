ARG VERSION=8u151

FROM gradle:6.6-jdk AS BUILD

COPY . /src
WORKDIR /src
RUN gradle build --stacktrace || return 0
# RUN ./gradlew build --stacktrace

FROM openjdk:11-jre

COPY --from=BUILD /src/build/libs/wololo-0.0.1-SNAPSHOT.jar /bin/runner/run.jar
WORKDIR /bin/runner

CMD ["java","-jar","run.jar"]

# FROM gradle:6.6-jdk AS BUILD
# # ENV APP_HOME=.
# COPY . /src
# ENV APP_HOME=/src
# WORKDIR $APP_HOME

# COPY gradle $APP_HOME/gradle
# COPY --chown=gradle:gradle . /home/gradle/src
# USER root
# RUN chown -R gradle /home/gradle/src

# RUN gradle build --stacktrace || return 0
# COPY . .
# RUN gradle clean build

# # actual container
# FROM openjdk:11-jre

# COPY --from=BUILD /src/build/libs/wololo-0.0.1-SNAPSHOT.jar /bin/runner/run.jar
# WORKDIR /bin/runner
# CMD ["java","-jar","run.jar"]
