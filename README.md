# Klunge

![eventapis](resources/eventapis.png)
[![Build Status](https://travis-ci.org/kloiasoft/eventapis.svg?branch=master)](https://travis-ci.org/kloiasoft/eventapis) [![Gitter chat](https://badges.gitter.im/hashicorp-terraform/Lobby.png)](https://gitter.im/eventapis/Lobby)
Offical Website: [Klunge.io](https://www.klunge.io)

Enterprise-Scale Eventually Consistent CQRS Framework

Klunge is a Java based Event Sourcing framework which can be benefited by the teams who are planning to make CQRS transitions with minimum learning curve and ease of adaptation.

It has a unique architecture called Operation Store™ together with the stack elements including Docker, Kafka, Hazelcast and Cassandra.

You can reach sample projects from following links:

* [eventapis-example-ecommerce](https://github.com/kloiasoft/eventapis-example-ecommerce) - demonstrates how to maintain data consistency in an Spring Boot, Cassandra and Kafka based microservice architecture using choreography-based sagas.

## Installation

If you're using [MAVEN](https://maven.apache.org/), you have to add this properties to super pom file.

.m2/settings.xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-kloia-eventapis</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/kloia/eventapis</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-kloia-eventapis</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/kloia/eventapis</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
```
If you're using gradle add this property to gradle file

```yaml
repositories {
    maven {
        url  "https://dl.bintray.com/kloia/eventapis" 
    }
}
```
for another using options you can visit [Bintray Repo](https://bintray.com/kloia/eventapis/)

## Usage
You have to add properties to pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>com.kloia.eventapis</groupId>
        <artifactId>spring-integration</artifactId>
        <version>0.7.0</version>
    </dependency>
    <dependency>
        <groupId>com.kloia.eventapis</groupId>
        <artifactId>spring-jpa-view</artifactId>
        <version>0.7.0</version>
    </dependency>
    <dependency>
        <groupId>com.kloia.eventapis</groupId>
        <artifactId>java-api</artifactId>
        <version>0.7.0</version>
    </dependency>
</dependencies>
```
## External Dependencies
You have to add properties to pom.xml, too.
```xml
<dependencies>
	<dependency>
		<groupId>org.apache.commons</groupId>
		<artifactId>commons-lang3</artifactId>
		<version>3.9</version>
	</dependency>
	<dependency>
		<groupId>org.apache.commons</groupId>
		<artifactId>commons-collections4</artifactId>
		<version>4.4</version>
	</dependency>
	<dependency>
		<groupId>com.datastax.cassandra</groupId>
		<artifactId>cassandra-driver-core</artifactId>
		<version>3.8.0</version>
	</dependency>
	<dependency>
		<groupId>pl.touk</groupId>
		<artifactId>throwing-function</artifactId>
		<version>1.3</version>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-openfeign</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.kafka</groupId>
		<artifactId>spring-kafka</artifactId>
	</dependency>
	<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
</dependencies>
```

## Build
If you're using different java versions you have to set in bash prompt before run at bottom of commands, you can use [SDKMAN](https://sdkman.io/)
```bash
  $ mvn clean install
  $ mvn clean compile
```
## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
Please make sure to update tests as appropriate.
## License
[Apache License](https://github.com/kloiasoft/eventapis/blob/master/LICENSE)