title: 简单的使用微服务网关

本工程非常简单，主要是创建 zuul-service的maven模块。Eureka-server 和 provider-user是Eureka-simple的，consumer-movie是ribbon-simple的
,并把它们的模块名、spring.application.name做相应的适配修改

## 测试：

 - 按顺序启动 zuul-eureka-server-simple,zuul-provider-user-simple,zuul-consumer-movie-simple,zuul-service
 - 访问 http://localhost:8040/zuul-consumer-movie/user/1,请求会转发到 http://localhost:8080/user/1
 - 访问 http://localhost:8040/zuul-provider-user/user/1,请求会被转发到 http://localhost:8010/user/1
 
 说明在默认情况下，Zuul会代理所有注册到Eureka Server的微服务，并且Zuul的路由规则如下：
 http://ZUUL_HOST:ZUUL_PORT/微服务在Eureka上的serviceId/**会被转发到对应的
 微服务
 
 ## 其他测试
 
 1. 本工程的电影微服务整合了 Ribbon,可以测试负载均衡效果，测试方法参考前面的ribbon-simple
 
 2. 可以启动 hystrix-turbine 的 hystrix-dashboard 模块，按照 hystrix-turbine 的测试方法
 可以看到可视化数据监控，说明了 zuul 整合了 hystrix
 
## 管理端点
当 @EnableZuulProxy 与 Spring Boot Actuator 配合使用时，
Zuul会暴露两个端点：`/routes` 和 `/filters`
### routes端点

/routes 端点使用非常简单，如下
 1. 使用 GET 方法访问该端点，即可返回 Zuul 当前映射的路由列表。
 2. 使用 POST 方法访问该端点就会强制刷新 Zuul 当前映射列表，尽管路由会
 自动刷新，Spring Cloud 依然提供了强制立即刷新的方式。
 3. Spring Cloud Edgware 对 `/routes` 端点进行了进一步增强，我们可以使用`/routes?format=details` 查看更多与路由相关的详情设置
 
 由于 spring-cloud-starter-netflix-zuul 已经包含了 spring-boot-starter-actuator,因此
 之前编写的 zuul-service 已经具备了路由管理的能力
 
 修改模块 zuul-service 的配置 ,设置`management.security.enabled=false`。启动所有模块，
 访问 http://localhost:8040/routes,可获得如下结果
 ~~~json5
    {
         /zuul-consumer-movie/**: "zuul-consumer-movie",
         /zuul-provider-user/**: "zuul-provider-user"
     }
 ~~~
 访问 http://localhost:8040/routes?format=details,可获得如下结果
 ~~~json5
 {
     /zuul-consumer-movie/**: {
         id: "zuul-consumer-movie",
         fullPath: "/zuul-consumer-movie/**",
         location: "zuul-consumer-movie",
         path: "/**",
         prefix: "/zuul-consumer-movie",
         retryable: false,
         customSensitiveHeaders: false,
         prefixStripped: true
     },
     /zuul-provider-user/**: {
         id: "zuul-provider-user",
         fullPath: "/zuul-provider-user/**",
         location: "zuul-provider-user",
         path: "/**",
         prefix: "/zuul-provider-user",
         retryable: false,
         customSensitiveHeaders: false,
         prefixStripped: true
     }
 }
 ~~~
 
 ### filter 端点
 
 从 Spring Cloud Edgware 版本开始，Zuul 提供了 /filter 端点。访问
 该端点即可返回 zuul 中当前所有过滤器的详情。访问 http://localhost:8040/filters,
 展示结果如下：
 ~~~json5
 {
     error: [
         {
             class: "org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter",
             order: 0,
             disabled: false,
             static: true
         }
     ],
     post: [
         {
             class: "org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter",
             order: 1000,
             disabled: false,
             static: true
         }
     ],
     pre: [
         {
             class: "org.springframework.cloud.netflix.zuul.filters.pre.DebugFilter",
             order: 1,
             disabled: false,
             static: true
         },
         {
             class: "org.springframework.cloud.netflix.zuul.filters.pre.FormBodyWrapperFilter",
             order: -1,
             disabled: false,
             static: true
         },
         {
             class: "org.springframework.cloud.netflix.zuul.filters.pre.Servlet30WrapperFilter",
             order: -2,
             disabled: false,
             static: true
         },
         {
             class: "org.springframework.cloud.netflix.zuul.filters.pre.ServletDetectionFilter",
             order: -3,
             disabled: false,
             static: true
         },
         {
             class: "org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter",
             order: 5,
             disabled: false,
             static: true
         }
     ],
     route: [
         {
             class: "org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter",
             order: 100,
             disabled: false,
             static: true
         },
         {
             class: "org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter",
             order: 10,
             disabled: false,
             static: true
         },
         {
             class: "org.springframework.cloud.netflix.zuul.filters.route.SendForwardFilter",
             order: 500,
             disabled: false,
             static: true
         }   
     ]
 }
 ~~~
 ## 路由配置详解
 Zuul 的路由配置非常灵活、简单，本书通过几个示例，详细讲解 Zuul 的路由配置。
 
 1.自定义指定微服务的访问路径
 
 配置 `zuul.routes.` 指定微服务的 `sericeId = ` 指定路径即可，例如
 ~~~yaml
 zuul:
  route:
    zuul-provider-user: /user/**
 ~~~
 2. 忽略指定微服务
 
 忽略服务可以使用 `zuul.ignored-services` 配置需要忽略的服务，多个服务间用分号分隔。例如
 ~~~yaml
 zuul:
  ignored-services: zuul-provider-user,zuul-consumer-movie
 ~~~
 
 3.忽略所有的微服务，只路由指定的微服务
 
 ~~~yaml
 zuul:
  ignored-services: '*'
  routes:
    zuul-consumer-movie: /user/**
 ~~~
 4. 同时指定微服务的 serviceId 和对应的路径
 
 ~~~yaml
 zuul:
  route:
    user-route: # 该配置方式中，user-route 只是给路由一个名称，可以任意起名
      service-id: zuul-consumer-movie
      path: /user/**
 ~~~
 
 5. 同时指定path和url
 
 ~~~yaml
 zuul:
  routes:
    user-route:
      url: http://localhost:8080/
      path: /user/**
 ~~~
 
6. 同时指定 path 和 url, 并且不破坏 Zuul 和 Hystrix、Ribbon 的特性

 ~~~yaml
 zuul:
  routes:
    user-route:
      path: /user/**
      service-id: zuul-provider-user
 ribbon:
  eureka:
    enabled: false
 zuul-provider-user:
  ribbon:
    listOfServer: localhost:8000,localhost:8001
 ~~~
 这样就可以指定 path 与 URL，又不会破坏 Zuul 的 Hystrix 与 Ribbon 特性了。
 
 7. 使用正则表达式指定 Zuul 的路由匹配规则
 
 借助 PatternServiceRouteMapper,实现从微服务到映射路由的正则配置。例如：
 
 ~~~java_holder_method_tree
 @Bean
 public PatternServiceRouteMapper serviceRouteMapper(){
    //调用构造函数 PatternServiceRouteMapper(String servicePatern,String routePattern)
    // servicePattern 指定微服务的正则
   // RoutePattern 指定路由的正则
    return new PatternServiceRouteMapper("(?<name>^.+)-(?<version>v.+$)","${version}/${name}");
 }
 ~~~
 通过这段代码即可实现诸如 zuul-consumer-movie-v1 这个微服务映射到 /v1/zuul-consumer-movie/** 这个路径。
 
 8. 路由前缀
  示例1：
  ~~~yaml
  zuul:
    prefix: /api
    strip-prefix: false
    routes:
      zuul-consumer-movie: /user/**
  ~~~
 这样，访问 Zuul 的 `/api/zuul-consumer-movie/1` 的路径，请求将会被转发到 `zuul-consumer-movie` 的 `/api/1`
 
  示例2：
  ~~~yaml
  zuul:
    routes:
      zuul-consumer-movie:
        path: /user/**
        strip-prefix: false
  ~~~
 这样访问 Zuul 的 /user/1 路径，请求将被转发到 zuul-consumer-movie 的 /user/1
 
 9. 忽略某些路径
  上面讲到如何忽略微服务，但有时还需要更细粒度的路由控制。例如，想让 Zuul 代理某个微服务，
  同时又想保护某个微服务的某些敏感路径。此时，可使用 ignored-Patterns,指定忽略的正则。例如
  
  ~~~yaml
  zuul:
      ignoredPatterns: /**/admin/**
      routes:
        zuul-consumer-movie: /user/**
  ~~~
  这样就可将 zuul-consumer-movie 微服务映射到 `/user/**` 路径，但会忽略
  该微服务中所有包含 `/admin/` 的路径
  
 10. 本地转发
 
 ~~~yaml
 zuul:
  routes:
    route-name:
      path: /path-a/**
      url: forward:/path-b
 ~~~
 这样，当访问 Zuul 的 `/path-a/**` 路径，将转发到 Zuul 的 `/path-b/**`
 
 当初学者无法掌握 Zuul 的路由规律，可将 com.netflix 包的日志级别设为 DEBUG。这样，Zuul就
 会打印出转发的具体细节，有助于理解 Zuul 的路由配置
 
 ## Zuul 的安全和 Header
 
 ### 敏感 Header 配置
  一般来说，可在同一个系统中的服务之间共享 Header ，不过应尽量让一些敏感的 Header 
  外泄。因袭很多情境下，需要通过路由指定一些列敏感 Header 列表。例如：
  
  ~~~yaml
  zuul:
    routes:
      zuul-consumer-movie:
        path: /user/**
        sensitive-headers: Cookie,Set-Cookie,Authorization
        url: http://dowmstream
  ~~~
  
  也可用 `zuul.sensitive-headers` 全局指定敏感 Header,例如：
  ~~~yaml
  zuul:
    sensiive-headers:  Cookie,Set-Cookie,Authorization #这三个也是默认的
  ~~~
  需要注意的是，如果使用 `zuul.routes.*.sensitive-headers` 方式会覆盖全局的配置
  
  ### 忽略Header
  可使用 `zuul.ignoredHeaders` 属性丢弃一些 Header,例如：
  ~~~yaml
  zuul:
    ignoredHeaders: header1,header2
  ~~~
  
  默认情况下，zuul.ignored-headers 是空值，但如果 Spring Security 在项目的 classpath 中，
  那么 zuul.ignoredHeader 的默认值就是 Pragma,Cache-Control,X-Frame-Options,X-Content-Type-Options,X-XSS-Protection,Expires。
  所以，当Spring Security在项目的classpath中，同时又需要使用下游微服务的 Spring Security
  的 Header 时,可以将 zuul.ignoredSecurity-Headers 设置为 false。