FROM openjdk:8-jdk
ARG project
ARG activeProfiles
ARG accesskey
ARG secret
ENV envProject=${project}
ENV envActiveProfiles=${activeProfiles}
ENV envAccesskey=${accesskey}
ENV envSecret=${secret}
RUN echo "Asia/Shanghai" > /etc/timezone && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
ADD ${project}.jar ./${project}.jar
CMD java -Daliyun.accesskey=${envAccesskey} -Daliyun.secret=${envSecret} -Dspring.profiles.active=${envActiveProfiles} -jar ${envProject}.jar
EXPOSE 8080