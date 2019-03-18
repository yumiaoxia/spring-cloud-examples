title: Eureka Server 的元数据
-----
Eureka 的 元数据有两种，分别是标准元数据和自定义元数据。标准元数据是
主机名、IP地址、端口号、状态页和健康检查等信息，这些信息都会被发布在服务注册表中。

自定义元数据可以使用Eureka.instance.metadata-map配置，这些元数据可以在远程客户端
访问，但一般不会改变客户端的行为

**改造内容**：
1. provider-user 添加自定义元数据信息
~~~yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    metadata-map:
      my-metadata: 我自定义的元数据
~~~
2. comsumer-movie 添加访问provider-user元数据信息的接口
3. 其他不变
**验证测试**：
1. 服务启动：server>provider>consumer
2. 访问 http://localhost:8761/eureka/apps 可查看Eureka的metadata
3.访问 http://localhost:8020/user-instance, 可以返回 provider-user的元数据信息
