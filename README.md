### 一、项目简介

- 项目名称 

  - 中文: 图尔斯
  - 简写: TOOLS

- 项目背景

  > 图尔斯历经多家上市公司和大厂的验证, 工具和方案逐步完善但仍属于闭门造车。
  
  > 为了图尔斯的未来, 决定永久性开源, 希望携手广大Java同行共创TOOLS未来。

  > 图尔斯的工具定位: 绝对优雅、绝对完整、相对全面。

### 二、模块预览

![TOOLS模块分布图](C:\project\owner\tools\README.assets\5f83d799e401fd06fd82d8c4.png)

#### 2.1 tools-base

> 该模块只是工具类的聚合, 没有任何依赖传递, 可以在项目中直接引用, 无视冲突与影响

- 很多常见的实体类, 比如相应对象

  ```java
  @Data
  public class R<T> implements Serializable {
      /**
       * 操作失败.
       */
      public static final R FAIL = new R(XCode.FAIL);
      /**
       * 操作成功.
       */
      public static final R OK = new R(XCode.OK);
  
      private Integer code; // 响应码: 系统返回的响应码值; 必填
      private String msg; // 提示语: 系统返回的提示信息; 必填; 一般展示
      private String desc; // 自述文: 系统返回的详细说明; 非必填; 一般不展示
      private T data; // 响应文: 系统返回的数据对象; 非必填
      private Long responseTime = System.currentTimeMillis(); // 响应时: 系统处理完成的时间戳; 必填
      // ...
  }
  ```

- 很多常用的枚举类, 比如XCode

  ```java
  public enum XCode {
      // 基本成功
      HTTP_OK(200, "请求成功"),
      // 资源丢失
      HTTP_MISSING(404, "资源丢失"),
      // 系统离线
      HTTP_OFFLINE(502, "系统离线"),
      // -----------------------------------------------------------------------------------------------------------------
      OK(1, "操作成功"), // 绿色
      WARN(0, "操作异常"), // 黄色
      FAIL(-1, "操作失败"), // 红色
      // ...
  }
  ```

- 很多实用的工具类, 比如AssertUtil、BigDecimalUtil、BoolUtil、IdUtil、QuickUtil、DesUtil...

  ```java
  // 非常丰富, 详见模块介绍
  ```



#### 2.2 tools-api

> 该模块是web项目, 主要包含: 应用在API接口上的实用组件

- 物理防刷: @Once旨在指定时间内只接受1次请求
- 接口共存: @ApiCoexist旨在1个应用内提供多个版本的相同接口
- 接口签名: SignController旨在继承后对接口进行签名鉴权服务



#### 2.3 tools-cache2

> 该模块是缓存方案的实现, 旨在0侵入解决`二次查询`性能问题



#### 2.4 tools-log

> 该模块是OpenFeign日志的补充实现, 旨在0侵入实现feign调用的日志打印



#### 2.5 tools-code

> 该模块是代码生成器工具, 基于mybatis[-plus]生成三层架构的代码



#### 2.6 tools-datalog

> 该模块是持久层项目, 用于0侵入写出`数据变更日志`到数据库的功能实现



#### 2.7 tools-ds

> 该模块是持久层项目, 用于0侵入读写分离、多数据源集成管理的功能实现



#### 2.8 tools-gray

> 该模块是微架构项目, 旨在0侵入支持客户端灰度发布方案



#### 2.9 tools-jackson

> 该模块是Jackson序列化能力集成模块, 旨在0侵入无忧引入Jackson配置、常见功能



#### 2.10 tools-mate

> 该模块是持久层项目, 用于实现数据权限(包含行、列)的功能实现



#### 2.11 tools-mybatis

> 该模块是持久层项目, 用于0侵入无忧引入Mybatis[-plus]框架的依赖、配置、常见功能



#### 2.12 tools-overstep

> 该模块是web项目, 用于0侵入实施接口越权的编解码方案



#### 2.13 tools-profile

> 该模块是持久层项目, 用于0侵入实施生产、测试的`数据隔离`解决方案



#### 2.14 tools-request

> 该模块是web项目, 旨在0侵入实现, 请求/响应流复用读写, 数据脱敏功能



#### 2.15 tools-sharding

> 该模块是持久层项目的微架构, 旨在0侵入集成ShardingJdbc并实现, 自动扩缩容解决方案



#### 2.16 tools-redis

> 该模块是缓存项目, 主要包含: 分布式ID, 分布式锁 / 变量锁, 以及redis工具类



#### 2.17 tools-kv

> 该模块是缓存项目, 旨在用mysql代替redis的变态问题的解决方案实现



#### 2.18 tools-logback

> 该模块是日志模块, 旨在优雅的集成logback日志打印框架



#### 2.19 tools-mail

> 该模块是web项目, 用于优雅的监控应用异常并邮件告警



#### 2.20 tools-webapp

> 该模块是web项目, 旨在0侵入无忧的集成各项常见的工具; 如全局异常处理、跨域处理、登录处理、验证码处理



#### 2.xx 暂不列举

> 详见各模块文档..



### 三、使用说明

- 继承示例
  > 统一版本控制、引用依赖以及常规配置 (非必须)

  ```xml
  <parent>
      <groupId>${project.groupId}</groupId>
      <artifactId>tools-parent</artifactId>
      <version>${latest.version}</version>
  </parent>
  ```

- 引用示例

  > 复杂的旧系统可以直接加版本号引用工具包, 否则请选用继承, 新系统建议系统继承

  ```xml
  <dependency>
      <groupId>${project.groupId}</groupId>
      <artifactId>tools-{module}</artifactId>
      <!-- 未继承请添加以下版本号 -->
      <!-- <version>${latest.version}</version> -->
  </dependency>
  ```



