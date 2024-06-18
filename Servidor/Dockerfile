FROM ubuntu:latest
RUN apt -y update
RUN apt install -y net-tools inetutils-ping
RUN apt install -y openjdk-21-jdk
RUN mkdir /app
COPY server1.jar /app/