# Alpine Linux with OpenJDK JRE
FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk add --no-cache bash
RUN apk add --no-cache curl

# copy JAR into image
COPY KairosK8sMsgIngest.jar /KairosK8sMsgIngest.jar 

#expose port
EXPOSE 10007

# java heap size settings xms (initial); xmx (max)
ENV JAVA_OPTS=" -Xms1024m -Xmx2048m "
ENTRYPOINT [ "bash", "-c", "java $JAVA_OPTS -jar /KairosK8sMsgIngest.jar \"$@\"", "--" ]
