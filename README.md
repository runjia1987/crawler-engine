# crawler-engine
crawler-engine with HTTP, proxy, JS-Java Interoperability, MQ task consumption, dynamic crawler scripts execution. support deployment in distribution style.

REQUIREMENTS:

JDK 8(if require Nashorn ClassFilter, then 1.8u45+ is mandatory);

Maven 3.2.x+;

Rabbitmq 3.6.0+;

read rabbitmq_quickstart.txt;


BUILD:

mvn clean package -Dmaven.test.skip

TESTCASE:

mvn test


RUN:

java -jar engineClient.jar [clientName]

