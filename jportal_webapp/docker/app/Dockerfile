FROM tomcat:9-jdk11-openjdk-slim

VOLUME /mcrHome/jportal /mcrHome/jportal/resources/META-INF

ADD https://jdbc.postgresql.org/download/postgresql-42.2.6.jre6.jar /mcrHome/jportal/lib/

ENTRYPOINT ["catalina.sh", "jpda", "run"]