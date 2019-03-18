Ttitle: 服务发现与注册—Eureka
-----
**示例项目**：
- eureka-simple: 简单的eureka实现
- eureka-cluster: Eureka Server 的高可用集群
- Eureka-authencating: Eureka的用户认证
- Eureka-metadata： Eureka 的元数据信息

### 理解Eureka server的自我保护模式

默认情况下，如果Eureka 在一定时间内没有接收到某个微服务实例的心跳是，Eureka将注销该实例（默认90 s）
。但是当网络分区故障发生时，微服务与Eureka Server无法正常通信，以上行为就变得非常危险了——（因为微服务本身实际上是健康的，但是Eureka server已经注销该服务）

Eureka Server 通过【自我保护模式】来解决这个问题
——当Eureka Server 节点在短时间内丢失过多客户端时(可能发生了网络分区故障)，那么这个节点就会进入自我保护模式

一旦进入自我保护模式，Eureka Server 就会保护注册表中的信息，不在删除注册服务表中的数据，当网络故障恢复后，
该Eureka Server节点会自动退出自我保护模式。使用自我保护模式，可以让Eureka Server 集群更加健壮、稳定

在Spring cloud中，可以使用Eureka.server.enable-self-preservation=false
禁用自我保护模式

### 多网卡环境下的IP选择

对于多网卡的服务器，指定IP在某些场景下很有用。例如某台服务器有
eth0,eth1,eth2三块网卡，但是只有eth1可以被其他的服务器访问；如果Eureka Client将eth0或者
eth2 注册到 Eureka Server 上，其他微服务就无法通过这个IP调用该微服务的接口

Spring Cloud 提供了按需选择IP的能力，从而避免以上的问题
1. 忽略指定名称的网卡。
    ~~~yaml
    spring:
        cloud:
          inetutils:
            ignored-interfaces:
              - docker0
              - veth.*
    
    eureka:
        instance:
          prefer-ip-address: true
    ~~~
    这样就可以忽略docker0网卡以及所有以veth开头的网卡。

2. 指定使用正则表达式，指定使用的网络地址
    ~~~yaml
    spring:
       cloud:
         inetutils:
           preferredNetworks:
             - 192.168
             - 10.0
    eureka:
       instance:
         prefer-ip-address: true  
    ~~~
3. 只使用站点本地地址，示例如下：
    ~~~yaml
    spring:
     cloud:
       inetutils:
         useOnlySiteLocalInterfaces: true
             
    eureka:
       instance:
         prefer-ip-address: true 
    ~~~
    这样就可强制使用站点本地地址
4. 手动指定IP地址
    ~~~yaml
    eureka:
       instance:
         ip-address: 127.0.0.1
         prefer-ip-address: true
    ~~~
### Eureka 的健康检查

Eureka Server 与 Eureka Client 之间使用心跳机制来确定Eureka Client
的状态，默认情况下，服务器端与客户端心跳保持正常，应用程序就很后悔
始终“UP”状态。

以上机制并不能完全反映应用程序的状态。举个例子，微服务与Eureka Server
之间心跳正常，Eureka Server认为改为服务“UP”，然而，改为服务的数据源
发生了问题，根本无法正常工作。

前文说过，Spring Boot Actuator 提供了/health 端点，改端点可以展示应用程
序的健康信息。Eureka 可以实现将微服务的健康状态传播到Eureka Server：
~~~yaml
eureka:
    client:
      healthcheck:
        enabled: true
~~~
某些情境下，可能希望更细粒度地了解微服务的健康情况，此时可实现
com.netflix.appinfo.HealthCheckHandler接口
>Eureka 健康检查相关博客; https://jmnarloch.wordpress.com/2015/09/02/spring-cloud-fixing-eureka-application-status/。