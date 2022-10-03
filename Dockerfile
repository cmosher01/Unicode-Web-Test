FROM eclipse-temurin:17-jdk-jammy AS build

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

USER root
ENV HOME /root
WORKDIR $HOME

COPY gradle/ gradle/
COPY gradlew ./
RUN ./gradlew --version

COPY settings.gradle ./

RUN mkdir -p $HOME/src/main/resources/ucd
WORKDIR $HOME/src/main/resources/ucd
RUN curl -LO 'https://www.unicode.org/Public/UCD/latest/ucd/UnicodeData.txt'

RUN mkdir -p $HOME/src/main/resources/ucsur
WORKDIR $HOME/src/main/resources/ucsur
RUN curl -LO -H 'user-agent: Mozilla/5.0' 'http://www.kreativekorp.com/ucsur/UNIDATA/UnicodeData.txt'

WORKDIR $HOME

COPY build.gradle ./
COPY src/ ./src/

RUN ./gradlew -i build



FROM tomcat:jdk17-temurin-jammy AS run

USER root
ENV HOME /root
WORKDIR $HOME

COPY src/main/tomcat /usr/local/tomcat/conf

COPY --from=build /root/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
