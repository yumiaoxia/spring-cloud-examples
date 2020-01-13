# spring-cloud 学习

包含模块


- [eureka](eureka/README.MD) 服务组件发现预注册
    - [简单入门](eureka/eureka-simple/README.md)
    - [元数据](eureka/Eureka-metadata/README.md)
    - [集群](eureka/eureka-cluster/README.md)
    - [用户认证](eureka/eureka-anthentication/README.md)
- [Feign](Feign/README.md)  各服务之间远程调用
    - [简单入门](Feign/feign-simple/README.md)
    - [用户认证](Feign/feign-auth/README.md)
- [hystrix](hystrix/README.md) 熔断器，实现为服务的容错处理
    - [简单入门](hystrix/hystrix-simple/README.md)
    - [hystrix 监控](hystrix/hystrix-turbine/README.md)
    - [使用消息中间件收集数据](hystrix/hystrix-turbine-mq/README.md)
- [Ribbon](Ribbon/README.md) 负载均衡
    - [基本实现](Ribbon/ribbon-simple/README.md)
    - [自定义规则Ribbon](Ribbon/ribbon-customizing/README.md)
    - [脱离Eureka使用Ribbon](Ribbon/ribbon-without-eureka/README.md)
- [zuul](zuul/README.md) 网关
    - [简单的使用微服务网关](zuul/zuul-simple/README.md)
    - [Zuul 的过滤器](zuul/zuul-filter/README.md)
    - [文件上传](zuul/zuul-file-upload/README.md)
    - [容错与回退](zuul/zuul-fallback/README.md)
    - [聚合微服务](zuul/zuul-aggregation/README.md)

每个大模块下面包含的小模块，每个小模块对应一个小工程，可用于测试

每个模块下面都有一个 markdown 格式的说明文件，
内容包括组件的概念、理论、代码摘要、实现原理、测试方法等