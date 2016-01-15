# crawler-engine
crawler-engine with HTTP, proxy, routing, MQ, dynamic JS scripts evaluation. support deployment in distribution style.

REQUIREMENTS:
JDK 8;
Maven 3.2.x;
read rabbitmq_quickstart.txt;

BUILD:
mvn clean package -Dmaven.test.skip

TESTCASE:
mvn test

RUN:
java -jar engineClient.jar [clientName]
