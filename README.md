# quarkus-myfaces

Prototype implementation of a JSF / MyFaces Core module for Quarkus. 
It's completely based on MyFaces 3.x as some changes were required.

## Whats not supported?
- JSF: Flows
- JSF: Injection into JSF arftifacts
- JSF: Websockets
- Quarkus: src/main/webapp! You need to put your views under src/main/resources/META-INF/resources!
- Quarkus: session replication / passivation / clustering
- some features probably :D

## Whats not tested?
- some features probably :D
- native images

## How to try it?

### Build MyFaces (SNAPSHOTS may not be up to date)
- https://github.com/apache/myfaces.git 
- mvn clean install -DskipTests

### Build quarkus-myfaces
- cd quarkus-myfaces
- mvn clean install

### Run showcase
- cd quarkus-myfaces-showcase
- mvn compile quarkus:dev
