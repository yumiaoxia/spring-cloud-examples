title：Eureka Server 的用户认证
-----
描述： 在simple的基础上，server模块依赖于spring-boot-starter-security。
加入配置
~~~yaml
security:
  basic:
    enabled: true
  user:
    name: user
    password: a123
~~~
然后server,provider和comsumer的注册服务url都加上user:password
如下：
~~~yaml
eureka:
  client:
    service-url:
      defaultZone: http://user:a123@localhost:8761/eureka/
~~~
