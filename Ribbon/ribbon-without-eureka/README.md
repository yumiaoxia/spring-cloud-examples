## 脱离Eureka使用Ribbon
### 改造过程
  1. 复制模块 comsumer-movie，修改模块名：ribbon-consumer-movie-without-eureka

  2. 去掉 Eureka 的 client 依赖,只使用 ribbon 的依赖

     找到

     ~~~xml
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
     </dependency>	
     ~~~

     修改为：

     ~~~xml
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
     </dependency>
     ~~~

     

  3. 去掉启动类上的 `@EnableDiscoveryClient`注解（**如果有的话**）

  4. 修改 yaml 文件的配置

     ~~~yaml
     server:
       port: 8010
     
     spring:
       application:
         name: eureka-consumer-movie
     
     ribbon-provider-user:
       ribbon:
         listOfServers: localhost:8080,localhost:8001
     ~~~

     其中，属性 `ribbon-provider-user.ribbon.listOfServers` 用于名为 `ribbon-provider-user` 的Ribbon 客户端设置请求的地址列表。

### 测试

1. 在simple项目中，启动两个`ribbon-provider-user`的实例
2. 启动本项目的 `consumer-movie`模块
3. 多次访问 `http://localhost:8010/log-user-instance

![](http://pooccqcjj.bkt.clouddn.com/QQ%E6%88%AA%E5%9B%BE20190324001955.png)

>如果想单独使用 Ribbon ,而不使用Eureka的服务发现功能，需要添加配置：`ribbon.eureka.enable=false`


### 饥饿加载
Spring Cloud 会为每个名称的 Ribbon Client 维护一个子应用程序上下文(还记得Spring-framework中的父子上下文吗?),
这个上下文是默认懒加载的。指定名称的 Ribbon Client 第一次请求时，对应的上下文才会被加载，因此，首次请求往往会比较慢
。

从 Spring cloud Dalston 开始，我们可配置饥饿加载，例如：
    
    ~~~yaml
    ribbon:
        eager-load:
            enabled: rule
            clients: client1,client2
    ~~~
这样，对于名为 client1,client2 的 Ribbon Client,将在启动时就加载对应的子应用程序上下文，从而提高首次请求的访问速度
