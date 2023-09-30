FROM ubuntu:latest
LABEL authors="bighaeil"

ENTRYPOINT ["top", "-b"]