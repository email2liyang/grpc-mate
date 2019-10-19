FROM java:openjdk-8-alpine

ADD helloworld-service.tar /

RUN chmod +x /helloworld-service/bin/helloworld-service

# default command to start
CMD ["/helloworld-service/bin/helloworld-service"]