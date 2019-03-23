# 使用 Feign 实现 声明式 REST 调用

## 一. Feign简介
 Feign 是 Netflix 开发的声明式，模板化的 HTTP 客户端，其灵感来自Retrofit、JAXRS-2.0 以及 WebSocket。Feign 可帮助我们更加便捷、优雅地调用 HTTP API。
 
 在Spring Cloud 中，使用 Feign 非常简单——创建一个接口，并在接口上添加一些注解，代码就完成了。Feign 支持多种注解，例如 Feign 自带的注解或者JAX-RX注解等。
 
 Spring Cloud 对 Feign 进行了增强，使 Feign 支持了 Spring MVC 注解，并整合了 Ribbon 和 Eureka，从而让 Feign 的使用更加方便。