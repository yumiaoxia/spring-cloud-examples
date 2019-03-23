title: 自定义规则Ribbon
---
### 改造
 1. 拷贝simple项目的consumer-movie模块
 2.创建Ribbon的配置类
 ~~~java
 @Configuration
 public class RibbonConfiguration {
     
     @Bean
     public IRule ribbonRule(){
         return new RandomRule();
     }
 }
 ~~~
 3.创建空的配置类，添加@Configuration注解和@RibbonClient注解
 ~~~java
 @Configuration
 @RibbonClient(name="microservice-provider-user",configuration = RibbonConfiguration.class)
 public class TestConfiguration {
 }

 ~~~
 ### 测试方式
 如同simple
 
 ## 全局配置
 2.添加@RibbonClients 注解为所有Ribbon Client提供默认配置
 ~~~java
 @RibbonClients(defaultConfiguration = DefaultRibbonConfig.class)
 public class RibbonClientDefaultConfigurationTestsConfig {
     
     public static  class BazServiceList extends ConfigurationBasedServerList{
         public BazServiceList(IClientConfig config){
             super.initWithNiwsConfig(config);
         }
     }
 }
 
 @Configuration
 class DefaultRibbonConfig{
     
     @Bean
     public IRule ribbonRule(){
         return  new BestAvailableRule();
     }
     
     @Bean
     public IPing ribbonPing(){
         return new PingUrl();
     }
     
     @Bean
     public ServerList<Server> ribbonServerList(IClientConfig config){
         return new RibbonClientDefaultConfigurationTestsConfig.BazServiceList(config);
     }
     
     @Bean
     public ServerListSubsetFilter serverListSubsetFilter(){
         ServerListSubsetFilter filter = new ServerListSubsetFilter();
         return filter;
     }
 }
 ~~~
 
### 使用配置文件自定义配置
**这部分没在源码中展示**

可配置项：
 - NFLoadBalancerClassName: 配置 ILoadBalancer 的实现类
 - NFLoadBalancerRuleClassName: 配置 IRule 的实现类
 - NFLoadBalancerPingClassName: 配置 IPing 的实现类
 - NIWSServerListClassName: 配置 ServerList 的实现类
 - NIWSServerListFilterClassName: 配置 ServerListFilter 的实现类
 
 具体配置过程：
 1. 在 consumer-movie 的 yaml 文件配置
 ~~~yaml
 ribbon-provider-user:
  ribbon
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
 ~~~
 这样，就可将名为 ribbon-provider-user 的 Ribbon Client 的负载均衡设计设为随机。
 
 如配置如下形式，则表示对所有的 Ribbon Client 都使用 RandomRule
 ~~~yaml
 ribbon:
  NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
~~~