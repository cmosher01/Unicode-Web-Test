FROM amazoncorretto:8 AS build

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

USER root
ENV HOME /root
WORKDIR $HOME

RUN echo "org.gradle.daemon=false" >gradle.properties

COPY gradle/ gradle/
COPY gradlew ./
RUN ./gradlew --version

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

COPY fetch_unicode.sh ./
RUN ./fetch_unicode.sh

RUN ./gradlew build



FROM amazoncorretto:8

USER root
ENV HOME /root
WORKDIR $HOME

RUN yum -y install tar shadow-utils

COPY --from=build /root/build/distributions/*.tar ./
RUN tar xvf *.tar --strip-components=1 -C /usr/local

RUN useradd user
USER user
ENV HOME /home/user
WORKDIR $HOME

EXPOSE 8080
CMD ["unicode-web-test"]
