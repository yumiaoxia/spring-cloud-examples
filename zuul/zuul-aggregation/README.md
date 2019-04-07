title: 使用 Zuul 聚合微服务
---
许多场景下，外部请求需要查询 Zuul 后端多个微服务。那么对于网络开销，流量耗费可能都无法令我们满意，那么对于这种场景，可使用
Auul 聚合微服务请求。

使用这种方式，在手机端只需要发送一次请求即可，简化了客户端侧的开发；不仅如此，由于
Zuul、微服务一般都集群在同一个局域网中，因此速度非常快，效率非常高。

## 本例项目测试
 1. 启动 zuul- simple 的 zuul-eureka-server-simple，zuul-provider-user-simple，zuul-consumer-movie-simple
 2. 启动 zuul-service-aggregation
 3. 访问 http://localhost:8040/aggregate/1,可获得如下结果
 ~~~json
 {
    movieUser: {
        id: 2,
        username: "account2",
        email: null,
        name: "李四",
        age: 28,
        balance: 180
    },
    user: {
        id: 2,
        username: "account2",
        email: null,
        name: "李四",
        age: 28,
        balance: 180
        }
}
 ~~~
 4. 停掉 zuul-provider-user-simple，zuul-consumer-movie-simple,可获得结果
 ~~~json
 {
    movieUser: {
        id: -1,
        username: null,
        email: null,
        name: null,
        age: null,
        balance: null
    },
    user: {
        id: -1,
        username: null,
        email: null,
        name: null,
        age: null,
        balance: null
    }
}
 ~~~
 说明 fallback 方法被触发，能够正常回退