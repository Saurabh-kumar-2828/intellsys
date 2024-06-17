FROM openjdk:latest
#FROM openjdk:latest
WORKDIR /usr/src/app
COPY src/main/java/. .
RUN javac IntellsysHadesApplication.java
ENV PORT=8000
CMD ["java", "Main"]
