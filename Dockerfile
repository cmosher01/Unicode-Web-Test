FROM gradle:jdk9

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

EXPOSE 8080

USER root
RUN chmod -R a+w /usr/local

RUN echo "org.gradle.daemon=false" >gradle.properties

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN chown -R gradle: ./

USER gradle
RUN gradle build

USER root
RUN tar xf /home/gradle/build/distributions/*.tar --strip-components=1 -C /usr/local

USER gradle

ENTRYPOINT ["/usr/local/bin/unicode-web-test"]
