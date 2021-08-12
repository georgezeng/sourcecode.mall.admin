FROM openjdk:8-jdk
ARG project
ARG activeProfiles
ARG accesskey
ARG secret
RUN echo "Asia/Shanghai" > /etc/timezone && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
ADD ${project}.jar ./${project}.jar
CMD java -Daccesskey=${accesskey} -Dsecret=${secret} -Dspring.profiles.active=${activeProfiles} -jar ${project}.jar
EXPOSE 8080