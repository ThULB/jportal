FROM tomcat:9-jdk11-openjdk-slim

RUN apt-get update && apt-get -y install libfreetype6

VOLUME /mcrHome/jportal /mcrHome/jportal/resources/META-INF

ADD https://jdbc.postgresql.org/download/postgresql-42.2.14.jre6.jar /mcrHome/jportal/lib/

ENTRYPOINT ["catalina.sh", "jpda", "run"]