# crawler-engine
<h3>INTRODUCE</h3>
crawler-engine with <b>HTTP, proxy, JS-Java Interoperability, MQ task consumption, dynamic crawler scripts execution</b>. support deployment in distribution style.
<h3>REQUIREMENTS</h3>
<ul>
<li>
JDK 8 (note: if require <b>Nashorn</b>feature<i>ClassFilter</i>, then 1.8u45+ is mandatory);
</li>

<li>
build tool:<a href="http://maven.apache.org/install.html" target="_blank">Maven</a> 3.2.x+;
</li>

<li>
MQ service(Erlang OTP):<a href="http://www.rabbitmq.com/download.html" target="_blank">Rabbitmq</a> 3.6.0+;
</li>

<li>
Mozilla <a href="https://developer.mozilla.org/zh-CN/docs/Mozilla/Projects/Rhino/Download_Rhino" target="_blank">Rhino</a>
</li>

<li>
Apache<a href="http://hc.apache.org/" target="_blank">HttpComponents</a>
</li>
</ul>
<h3>Steps</h3>

1. PREPARE dev env: read rabbitmq_quickstart.txt;
2. BUILD: mvn clean package -Dmaven.test.skip
3. TESTCASE: mvn test
4. RUN: java -jar engineClient.jar [clientName]

