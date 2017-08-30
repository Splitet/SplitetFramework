![eventapis](resources/eventapis.png)
An event database to enable event sourcing and distributed transactions for applications that use the microservice architecture.

- Website: https://eventapis.kloia.com
- [![Gitter chat](https://badges.gitter.im/hashicorp-terraform/Lobby.png)](https://gitter.im/eventapis/Lobby)

# Build
![Image of Yaktocat](https://travis-ci.org/kloiasoft/eventapis.svg?branch=master)
```
mvn package
```

# Build Docker Image
```
docker build -t kloiasoft/eventapis:latest .
```

# Run
```
docker-compose up
```
