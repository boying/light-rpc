# light-rpc makes writing java rpc easier

light-rpc是一款java rpc框架，目标是方便地集成到现有系统，像使用本地java方法一样进行远程调用。

### 基本说明

light-rpc是基于yaml配置文件的运行的，需要配置注册中心，服务提供方，服务使用方等信息。

RpcContext是框架启动类，提供了，启动服务、获取远程接口代理对象等功能。

LightRpcStarter是为方便接入Spring容器的启动类。

MainTest是本地可运行的示例。

### 注册中心

light-rpc以Apache ZooKeeper为注册中心。rpc服务方向注册中心注册服务，rpc调用方从注册中心获取服务信息。

*Example*

```yaml
# 注册中心配置 非必填
registry:
  # 注册中心地址 非必填（默认null） 例：127.0.0.1:2181
  address: 127.0.0.1:2181
```

不配置注册中心也是可以的。调用方需要配置服务方的ip才能调用。

### 远程接口的定义——Java接口

light-rpc用Java interface表示远程调用的定义。调用方引入接口jar包，框架将会自动创建接口的代理对象供程序使用。

*Example*

```java
public interface IFoo {
    Response<FuncData> f(String str, Long val);
    
    String echo(String s);

    int add(int a, int... b);

    String concat(String s, int i, Integer in);

    void sleep(int timeout);
}
```

### rpc调用方

框架创建了远程接口的代理对象，代理对象根据方法参数创建http请求，访问服务方。

当有多个服务方时，将采用round robin策略访问，保证负载均衡。

#### 配置文件

```yaml
# 客户端配置 非必填
clients:
    # 客户端对应服务的id 必填
  - appId: foo
    # 远程方法默认超时时间（毫秒） 非必填（默认200）
    methodDefaultTimeoutMillisecond: 1000
    # 远程服务地址 非必填 若配置则覆盖配置中心提供的地址
    serverProviders:
      - ip: 127.0.0.1
        port: 8888
    # 远程方法的包路径，将扫描该包下所有接口作为远程调用接口 非必填
    basePackage: demo.service
    # 远程方法所在的接口，可用于配置方法超时时间 非必填
    interfaces:
        # 接口路径 必填
      - name: demo.service.IFoo
        # 方法超时配置 非必填
        methods:
            # 方法名 必填
          - name: sleep
            # 超时时间（毫秒） 必填
            timeoutMillisecond: 1000
```

#### 使用示例

```java
// 创建RpcContext
RpcContext clientContext = new RpcContext("light_rpc_config_client.yaml", null);
// 启动框架
clientContext.start(false);
// 获取远程服务代理对象
IFoo foo = clientContext.getProxy(IFoo.class);
// 调用远程服务
String s = foo.echo("hello");

```

### rpc服务方

#### ServiceBeanProvider

服务方通过ServiceBeanProvider向框架提供具体实现接口的对象，每个远程调用最终会调用实现对象的方法。

```java
public static class MyServiceBeanProvider implements ServiceBeanProvider {
    private Map<Class, Object> map;

    public MyServiceBeanProvider(Map<Class, Object> map) {
        this.map = map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) map.get(clazz);
    }
}
```

如果准备让框架在Spring容器中使用，可以直接使用SpringServiceBeanProvider。

#### 配置文件

```yaml
# 服务配置 非必填
server:
  # 服务id 必填
  appId: foo
  # 服务端口号 非必填（默认8888）
  port: 8888
  # 提供服务接口的包路径，将扫描该包下所有接口作为服务接口 非必填
  basePackage: demo.service
  # 提供服务的接口 非必填 basePackage与interfaces至少填一个，合并二者
  interfaces:
    - demo.service.IFoo
  # 服务线程池大小 非必填（默认 cpu核数 * 2)
  threadPoolSize: 10

```

#### 使用示例

```java
// 创建ServiceBeanProvider
IFoo obj = new Foo();
Map<Class, Object> map = new HashMap<>();
map.put(IFoo.class, obj);
ServiceBeanProvider provider = new MyServiceBeanProvider(map);

// 创建RpcContext
RpcContext serverContext = new RpcContext("light_rpc_config_server.yaml", provider);
// 启动框架
serverContext.start(false);

```

### 接入Spring

为了方便框架接入Spring容器，light-rpc做了相应支持，只需做如下配置：

```xml
    <bean name="springServiceBeanProvider" class="light.rpc.spring.SpringServiceBeanProvider" />

    <bean name="lightRpgStarter" class="light.rpc.spring.LightRpcStarter">
        <constructor-arg value="ConfigureNoZoo_gai.json" />
        <constructor-arg ref="springServiceBeanProvider" />
    </bean>
```

SpringServiceBeanProvider会从Spring容器中获取具体的服务对象注册到light-rpc框架中。

LightRpcStarter会自动启动框架。

### 熔断

框架提供熔断机制，如果调用不成功的情况满足一定条件，将自动触发熔断，防止雪崩。

```yaml
# 熔断器配置 非必填
circuitBreaker:
  # 至少有requestVolumeThreshold个请求，熔断器才进行错误率的计算 非必填（默认10）
  requestVolumeThreshold: 10
  # 熔断器中断请求sleepWindowInMilliseconds毫秒后会进入半打开状态,放部分流量过去重试 非必填（默认5000）
  sleepWindowInMilliseconds: 5000
  # 错误率达到errorThresholdPercentage个百分点开启熔断保护 非必填（默认50）
  errorThresholdPercentage: 50
```


