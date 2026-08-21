FROM postgis/postgis:18-3.6-alpine

RUN rm -f /docker-entrypoint-initdb.d/10_postgis.sh

COPY ./init/ /docker-entrypoint-initdb.d/

RUN chmod -R +x /docker-entrypoint-initdb.d/