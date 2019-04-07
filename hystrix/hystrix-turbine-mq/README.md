title: 使用消息中间件收集数据

一些场景下，可借助消息中间件实现数据收集。各个微服务将hystrix Command 的监控数据发送至消息中间件

## 改造
 1. 在虚拟机上通过 docker 安装 rabbitmq，注意镜像带 management 的是带web管理功能的
 2. 改造微服务，复制 hystrix-turbine 的 consumer-movie 模块，artifactId改为 hystrix-consumer-movie-turbine-mq
 3. 添加一下依赖
 ~~~xml
<dependency>
 <groupId>org.springframework.cloud</groupId>
 <artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-stream-rabbitmq</artifactId>
</dependency>
~~~
4. 复制 hystrix-turbine 的 turbine 模块，修改 artifactId 为 turbine-mq
5. 在turbine-mq模块添加依赖
~~~xml
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-netflix-turbine-stream</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-stream-rabbitmq</artifactId>
</dependency>
~~~
同时，删除
~~~xml
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-netflix-turbine</artifactId>
</dependency>
~~~
6.修改启动类，将启动类上注解 @EnableTurbine 修改为 @EnableTurbineStream。
7.在上面两个模块配置文件中添加以下内容
~~~yaml
spring:
    rabbitm1:
      host: 192.168.0.21
      port: 5672
      username: guest
      password: guest
~~~
8. turbine-mq 模块中删除
~~~yaml
turbine:
    appconfig: hystrix-consumer-movie
    clusterNameExpression: "'default'"
~~~

## 测试
1. 启动 hystrix-simple 的 eureka-server 和 provider-user 模块
2. 启动 hystrix-consumer-movie-turbine-mq 模块
3. 启动 hystrix-stream-turbine-mq
4. 访问 http://localhost:8080/user/1
5. 访问 http://localhost:8035