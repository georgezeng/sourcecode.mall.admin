FROM openjdk:8-jdk
ARG project
ARG extraEnv
ENV artifact=${project}
ENV extraJavaEnv=${extraEnv}
RUN echo "Asia/Shanghai" > /etc/timezone && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
ADD ${project}.jar ./${project}.jar
CMD java -Dspring.profiles.active=${extraJavaEnv} -jar ${artifact}.jar
EXPOSE 8080