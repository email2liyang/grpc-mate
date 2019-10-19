FROM java:openjdk-8-alpine

ADD elasticsearch-service.tar /

RUN chmod +x /elasticsearch-service/bin/elasticsearch-service

# default command to start
CMD ["/elasticsearch-service/bin/elasticsearch-service"]