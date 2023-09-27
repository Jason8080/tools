### 一、功能说明

​		该方案针对分布式微服务下的集群节点进行**全链路灰度流量控制**。

​		具有灰度验收、优先体验、业务试点等功能用途, **方便产品的试点与推广**。

​		具有内侧/自测、滚动发布、平滑发版等技术用途, **极大提升产品的稳定性和可用性**。

###### 背景

- 在高可用作为刚需技术特性的当下, 应当杜绝停机更新、生产测试等危险操作。 
- 但是无论是滚动发布还是生产Mock本质上均属于停机更新和生产测试的范畴。
- 产品的升级与维护不可避免, 停机更新和生产测试的问题迫在眉睫, 灰度发布应运而生。




### 二、使用步骤

- 引用依赖

  > 网关 或 客户端 引入 JAR; 目前只对Gateway/Nacos做了支持, 但理论上支持任何架构的扩展。

  ```xml
  <dependency>
      <groupId>cn.gmlee.tools</groupId>
      <artifactId>tools-gray</artifactId>
      <version>${laster.version}</version>
  </dependency>
  ```


- 配置开启

  > 该内容配置在网关的配置中心, 即: 全局配置。

  ```yaml
  tools:
    gray:
      # 令牌属性名
      token: Ldw-Auth
      # 全局开关
      enable: true
      # 应用配置
      apps:
        # 应用名称
        ldw-mall-mer:
          # 应用开关
          enable: true
          # 灰度版本
          # 如果没有该配置的实例则进入最新节点 (即: 最新版本作为灰度节点)
          # 如果没有该版本的实例则进入正式节点
          # 如果多个该版本的实例则进入最新节点
          versions:
            - 1.0.0.1024
          # 灰度匹配规则 (按照顺序取第一个匹配成功的结果; 如均匹配失败则进入正式节点)
          rules:
            ip:
              enable: true
              content: # IP规则: 支持*通配符
                - 127.0.0.1
            user:
              enable: true
              content: # USER规则: 用户名 (常用)
                - D301756
            weight:
              enable: true
              content: # 权重规则: 0-100 (随机选取指定比例的请求进入灰度)
                - 0
            custom:
              enable: true
              content: # 自定义规则: 理论上支持任何定制化的灰度规则; 如有疑惑可联系开发者
                # 自定义的规则默认支持填写用户编号; 类似USER规则
                - 164063523869891788900
  ```




- 工具说明

  - 目前(5.3.11)全链路灰度仅对Feign进行了支持, 理论上可以扩展到任何中间件/工具。

  - 该方案无需前端配合, 后端可以独立完成改造。

  - 如前端也需要灰度改造只需要前端传递version请求头到后端即可完成改造(后端无需处理)。



### 三、重要规则

#### 关闭灰度

> 以下场景均属于灰度没有开启的场景; 
>
> 灰度关闭场景下流量采用轮询机制访问节点;

- 网关未引入灰度
- 客户端未引入灰度
- 灰度配置总开关未配置或关闭
- 灰度配置应用开关未配置或关闭



#### 禁用灰度

> 满足以下条件属于禁止进入灰度的场景; 
>
> 禁用灰度场景下流量不会进入最新版本(或开发指定版本)的节点; 

- 当前请求的应用存在新旧版本

  > 如果仅有一个版本则无论灰度配置如何, 都会轮询这仅有的一个版本

- 当前请求不符合灰度配置的规则



#### 允许灰度

> 满足以下条件属于允许进入灰度的场景; 
>
> 允许灰度场景下流量只会进入最新版本(或开发指定版本)的节点;

- 当前请求的应用存在新旧版本

  > 如果仅有一个版本则无论灰度配置如何, 都会轮询这仅有的一个版本

- 当前请求符合灰度配置的规则




### 四、工具原理

###### 逻辑

- 前端流量进入网关
- 网关引入的SDK拦截请求并过滤实例节点
  - 客户端注册服务 (提交项目版本号) 元数据。
- 请求指定的灰度节点并透传版本号
  - 是否灰度节点取决于元数据中的版本号。
- 灰度节点收到请求并进行处理
- 灰度节点过滤依赖服务的实例节点
- 请求依赖服务的灰度节点并透传版本号
  - 将原版本号添加到请求头中。
- 依赖服务灰度节点收到请求并进行处理
- 返回业务处理结果

![image-20230911172926649](C:\project\owner\tools\doc\Gray-全链路灰度发布解决方案说明.assets\image-20230911172926649.png) 

 

###### 代码

1. 网关拦截器GrayBalancerFilter拦截请求
2. 通用负载均衡GrayReactorServiceInstanceLoadBalancer过滤灰度实例节点
3. 透传版本号拦截器: GrayFeignRequestInterceptor
4. 灰度检测服务GrayServer处理请求并检测请求是否使用灰度
5. 客户端自动装配GrayClientAutoConfiguration注册版本号元数据



###### 数据

- 如果权重规则开启, 则会随机生成权重坐标并存储在内存中
  - 权重配置更新时被覆盖; 数据无需清除/处理


- 自定义规则如果开启, 并对非用户ID规则进行了支持, 则会将自定义数据存储在redis中
  - 扩展自定义规则, 需要自行维护redis, 一般开发者无感。



### 五、使用示例

- 网关触发灰度的日志

  ```properties
  2023-09-11 17:50:54.106  INFO 8 --- [ndedElastic-204] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-mall-mer 开发指定: [1.0.0.1023, 0.0.0.0000, 1.0.0.1058, 1.0.0.1059] 实例列表: 
  {
          "1.0.0.1059":[
                  {
                          "serviceId":"ldw-mall-mer",
                          "host":"10.244.2.85",
                          "port":8044,
                          "secure":false,
                          "metadata":{
                                  "nacos.instanceId":"10.244.2.85#8044#DEFAULT#DEFAULT_GROUP@@ldw-mall-mer",
                                  "nacos.weight":"1.0",
                                  "nacos.cluster":"DEFAULT",
                                  "nacos.ephemeral":"true",
                                  "nacos.healthy":"true",
                                  "preserved.register.source":"SPRING_CLOUD",
                                  "version":"1.0.0.1059"
                          },
                          "uri":"http://10.244.2.85:8044",
                          "instanceId":null,
                          "scheme":null
                  }
          ]
  }
  2023-09-11 17:50:54.106  INFO 8 --- [ndedElastic-204] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-mall-mer 最新版本: 1.0.0.1059 实例列表: 
  [
          {
                  "serviceId":"ldw-mall-mer",
                  "host":"10.244.2.85",
                  "port":8044,
                  "secure":false,
                  "metadata":{
                          "nacos.instanceId":"10.244.2.85#8044#DEFAULT#DEFAULT_GROUP@@ldw-mall-mer",
                          "nacos.weight":"1.0",
                          "nacos.cluster":"DEFAULT",
                          "nacos.ephemeral":"true",
                          "nacos.healthy":"true",
                          "preserved.register.source":"SPRING_CLOUD",
                          "version":"1.0.0.1059"
                  },
                  "uri":"http://10.244.2.85:8044",
                  "instanceId":null,
                  "scheme":null
          }
  ]
  2023-09-11 17:50:54.109  INFO 8 --- [ndedElastic-204] cn.gmlee.tools.gray.server.GrayServer    : 灰度服务: ldw-mall-mer 处理器: user 允许灰度
  2023-09-11 17:50:54.112  INFO 8 --- [ndedElastic-204] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-mall-mer 进入灰度: true 预选实例: 
  [
          {
                  "serviceId":"ldw-mall-mer",
                  "host":"10.244.2.85",
                  "port":8044,
                  "secure":false,
                  "metadata":{
                          "nacos.instanceId":"10.244.2.85#8044#DEFAULT#DEFAULT_GROUP@@ldw-mall-mer",
                          "nacos.weight":"1.0",
                          "nacos.cluster":"DEFAULT",
                          "nacos.ephemeral":"true",
                          "nacos.healthy":"true",
                          "preserved.register.source":"SPRING_CLOUD",
                          "version":"1.0.0.1059"
                  },
                  "uri":"http://10.244.2.85:8044",
                  "instanceId":null,
                  "scheme":null
          }
  ]
  2023-09-11 17:50:54.112  INFO 8 --- [ndedElastic-204] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-mall-mer 命中实例: 10.244.2.85:8044 
  ```
  
- 客户端未触发灰度日志

  ```properties
  2023-09-11 17:55:33.171  INFO [ldw-mall-mer,7adc8e535425add5,7adc8e535425add5] 8 --- [  XNIO-1 task-3] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-ops 外部指定: 1.0.0.1059 实例列表: 
  2023-09-11 17:55:33.171  INFO [ldw-mall-mer,7adc8e535425add5,7adc8e535425add5] 8 --- [  XNIO-1 task-3] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-ops 进入灰度: false 预选实例: 
  [
  
  ]
  2023-09-11 17:55:33.171  INFO [ldw-mall-mer,7adc8e535425add5,7adc8e535425add5] 8 --- [  XNIO-1 task-3] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-ops 命中实例: 10.244.2.42:8106
  ```

  

- 客户端触发灰度的日志

  ```properties
  2023-09-11 18:01:46.753  INFO 8 --- [r-http-epoll-12] cn.gmlee.tools.gray.server.GrayServer    : 灰度服务: ldw-mall-mer 处理器: user 允许灰度
  2023-09-11 18:01:46.754  INFO 8 --- [r-http-epoll-12] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-mall-mer 进入灰度: true 预选实例: 
  [
          {
                  "serviceId":"ldw-mall-mer",
                  "host":"10.244.2.85",
                  "port":8044,
                  "secure":false,
                  "metadata":{
                          "nacos.instanceId":"10.244.2.85#8044#DEFAULT#DEFAULT_GROUP@@ldw-mall-mer",
                          "nacos.weight":"1.0",
                          "nacos.cluster":"DEFAULT",
                          "nacos.ephemeral":"true",
                          "nacos.healthy":"true",
                          "preserved.register.source":"SPRING_CLOUD",
                          "version":"1.0.0.1059"
                  },
                  "uri":"http://10.244.2.85:8044",
                  "instanceId":null,
                  "scheme":null
          }
  ]
  2023-09-11 18:01:46.754  INFO 8 --- [r-http-epoll-12] b.GrayReactorServiceInstanceLoadBalancer : 灰度服务: ldw-mall-mer 命中实例: 10.244.2.85:8044
  ```

  

  
