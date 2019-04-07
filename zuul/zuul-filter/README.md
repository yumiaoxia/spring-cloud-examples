title: Zuul 的过滤器
 
 ## 一. 过滤器类型与生命周期
 
Zuul 的大部分功能都是通过过滤器实现的。Zuul 定义了 4 中 标准过滤器类型，这些过滤器类型
对应请求的典型生命周期

 - PRE：这种过滤器在请求被路由之前调用。可利用这种过滤器实现身份验证、在集群中选择请求的微服务，
 记录调试信息等
 - ROUTING: 这种过滤器将请求路由到微服务。这种过滤器用于构建发送给微服务的请求，并使用 Apache
 HttpClient 或 Netflix Ribbon 请求微服务
 - POST: 这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的 Http Header
 ，收集统计信息和指标，将响应从微服务发送给客户端等。
 - ERROR: 在其它阶段发生错误时执行该过滤器
 
 除了默认过滤器类型，Zuul 还允许创建自定义的过滤器类型。例如，可以定制一种 STATIC 类型
 的过滤器，直接在Zuul中生成响应，而不将请求转发到后端微服务
 
 ## 内置过滤器详解
 
 Spring Cloud 默认为 Zuul 编写并启用了一些过滤器，关于这些过滤器的作用，我们结合 @EnableZuulServer、
 @EnableZuulProxy 两个注解进行讨论
 
 可将 @EnableZuulProxy 简单理解为 @EnableZuulServer 的增强版。事实上，当 Zuul 与
 Eureka、Ribbon 等组件配合使用时，@EnableZuulProxy 是我们最常用的注解。
 
 先了解一下什么是 RequestContext , 其用于在过滤器之间传递消息。它的数据保存在每个请求的 ThreadLocal 中。它用于
 请求路由到哪里，错误、HttpServletRequest、HttpServletResponse等信息。RequestContext 扩展了
 ConcurrentHashMap,所以，理论上任何数据都可以存储在RequestContext中。
 
 ###  @EnableZuulServer 所启用的过滤器
  #### pre 类型过滤器
  
  1. ServletDetectionFilter: 该过滤器检查请求是否通过 Spring Dispatcher。检查后，通过
  FilterConstants.IS_DISPATCHER_SERVLET_REQUEST_LEY 设置布尔值。
  
  2. FormBodyWrapperFilter: 解释表单数据，并为请求重新编码。
  3. DebugFilter: 顾名思义，调试用的过滤器。但设置 `zuul.include-debug-header=true` 抑或
  设置 `zuul.debug.request=true`，并在请求时加上了debug=true参数，例如$ZUUL_HOST:ZUUL_PORT/some-path?debug=true,就会开启开启该过滤器
  
#### route 类型过滤器
  
  SendForwardFilter: 该过滤器使用Servlet.RequestDispatcher 转发请求，转发位置存储在 RequestContext 的属性
  FilterConstants.ROWARD_TO_KEY 中。这对转发到 Zuul 自身的端点很有用。可将路由设成：
  
  ~~~yaml
  zuul:
    route:
      abc:
        path: /path-a/**
        url: forward:/path-b
  ~~~
#### post 类型过滤器
  
  SendResponseFilter: 将代理请求的响应写入当前响应。
  
#### error 类型过滤器
 
 SendErrorFilter: 若RequestContext.getThrowable()不为null，则默认转发到 /error,
 也可以设置 error.path 属性来修改默认的转发路径
 
### @EnableZuulProxy 所启用的过滤器

#### pre 类型过滤器

PreDecorationFilter: 该过滤器根据提供的 RouteLocator 确定路由到的地址，以及怎样去路由。
同时，该过滤器还为下游请求设置各种代理相关的 header。

#### route 类型过滤器

1. RibbonRoutingFilter: 该过滤器使用 Ribbon、Hystrix 和可插拔的 HTTP 客户端发送请求。ServiceId
在RequestContext 的属性 FilterConstants.SERVICE_ID_KEY 中。该过滤器可使用如下这些不同的 HTTP 
客户端。

 - Apache HttpClient: 默认的Http客户端
 - Squareup OKHttpClient v3: 若需要使用该客户端，需保证 com.squareup.okhttp3的依赖
 在classpath 中，并设置 ribbon.okhttp.enabled=true
 - Netflix Ribbon HTTP Client: 设置 `ribbon.restclient.enabled=true 即可启用该 HTTP
 客户端。该客户端有一定限制，例如不支持 PACH 方法。另外，它有内置的重试机制。
 
 2. SimpleHostRoutingFilter: 该过滤器通过 Apache HttpClient 向指定的 URL 发送请求。URL
 在 RequestContext.getRouteHost() 中。
 
 
 形如以下的内容的路由不会经过 RibbonRoutingFilter,而是走SimpleHostRoutingFilter。
 ~~~yaml
 zuul:
  routes:
    user-route:
      url: http://localhost:8000/
      path: /user/**
  ~~~

## 二. 编写自定义的 zuul 过滤器
### 改造
 1. 复制 zuul-simple 的 zuul-service 模块，将 artifactId 修改为 zuul-service-filter
 2. 编写过滤器类
 ~~~java
 public class PreRequestLogFilter extends ZuulFilter {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ZuulFilter.class);
 
     @Override
     public String filterType() {
         return FilterConstants.PRE_TYPE;
     }
 
     @Override
     public int filterOrder() {
         return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;
     }
 
     @Override
     public boolean shouldFilter() {
         return true;
     }
 
     @Override
     public Object run() {
         RequestContext ctx = RequestContext.getCurrentContext();
         HttpServletRequest request = ctx.getRequest();
         PreRequestLogFilter.LOGGER.info("send {} request to {}",request.getMethod(),request.getRequestURL().toString());
         return  null;
     }
 }
 ~~~
 2.修改启动类，将编写的Filer加入到 Spring Bean 管理
 ~~~java_holder_method_tree
 @Bean
     public PreRequestLogFilter preRequestLogFilter(){
         return new PreRequestLogFilter();
     }
 ~~~
 ### 测试
 
 1.启动 zuul-simple 的 zuul-eureka-server-simple,zuul-provider-user-simple
 2.启动 zuul-service-filter
 3.访问 http://localhost:8040/zuul-provider-user/user/1，可看到打印日志：
 ~~~
 2019-04-06 17:09:46.703  INFO 12648 --- [nio-8050-exec-1] com.netflix.zuul.ZuulFilter              : send GET request to http://localhost:8050/zuul-provider-user/user/1
~~~
说明自定义的zuul过滤器被执行了

## 三. 禁用 Zuul 过滤器

Spring Cloud 默认为Zuul编写并启用了一些过滤器。这些过滤器都存放在 Spring-cloud-netflix-core 的这个 jar 包的
org.springframework.cloud.netflix.zuul.filters 包中

在 zuul 的配置文件中，只需设置 `zuul.SimpleClassName>.<filterType>.disable=true,即可禁用
SimpleClassName 所对应的过滤器。