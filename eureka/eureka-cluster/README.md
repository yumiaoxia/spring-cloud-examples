title: 高可用的Eureka Server集群。
-----
主要在simple项目的基础上，修改下yaml配置，让Eureka Srver集群并相互同步
provider、consumer配置文件略作修改，使其依赖到多个Eureka Server上


启动方式：
   1. 命令行指定profile
   ~~~shell
   java -jar 包名 --spring.profiles.active=peer1
   java -jar 包名 --spring.profiles.active=peer2