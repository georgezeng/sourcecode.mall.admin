FROM java:8
ARG project
ENV artifact=${project}
RUN echo "Asia/Shanghai" > /etc/timezone
ADD ${project}.jar ./${artifact}.jar
CMD java -jar ${artifact}.jar
EXPOSE 8080