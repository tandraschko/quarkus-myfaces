# NOTE: it has been moved to MyFaces directly: https://github.com/apache/myfaces/tree/master/extensions/quarkus/

# quarkus-myfaces

Prototype implementation of a JSF / MyFaces Core module for Quarkus. 
It's completely based on MyFaces 3.x as some changes were required.

## Differences compared to a normal Servlet container
- You need to put your views under src/main/resources/META-INF/resources as Quarkus doesn't create a WAR and src/main/webapp is ignored!
- Session replication / passivation / clustering is not supported by Quarkus

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
