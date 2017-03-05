# light-rpc

## 简介
Java Rpc 框架，更轻，更快

服务发现，也可指定服务方，支持重载调用，异步调用

## 使用说明

### rpc调用方

#### 步骤
1. 配置文件
2. 启动rpc容器
3. 获取代理对象
4. 调用函数

#### 范例

```
// 创建rpc容器
RpcContext context = new RpcContext("Configure.json", null);
// 启动容器
context.start();
// 获取代理对象
IFoo foo = context.getProxy(IFoo.class);
// 同步调用echo方法
String s = foo.echo("hello world!");
// 异步调用echo方法
Future<String> future = context.asyncCall(IFoo.class, "echo", new Class[]{String.class}, 
new Object[]{"hello world!"}, String.class);
// 获取返回值
s = future.get();
```

### rpc服务方

#### 步骤

1. 配置文件
2. 实现ServiceBeanProvider接口，框架将使用它提供的对象执行调用方法
3. 启动rpc容器

#### 范例

```
// 创建实现ServiceBeanProvider实现类
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

// 创建创建实现ServiceBeanProvider对象
IFoo obj = new Foo();
Map<Class, Object> map = new HashMap<>();
map.put(IFoo.class, obj);
MyServiceBeanProvider provider = new MyServiceBeanProvider(map);

// 创建rpc容器
RpcContext context = new RpcContext("Configure.json", provider);

// 启动容器
context.start()

    
```

## 配置文件说明

```
{
  "common":{ /* 通用配置 */
    "registryAddress": "127.0.0.1:2181", /* ZooKeeper服务地址,可不填 */
    "asyncClientPort": 9999 /* 异步调用结果监听端口 */
  },

  "server":{ /* rpc服务方配置,如果不需要提供服务,将此字段设置为null */
    "appId": "foo", /* 服务id */
    "protocol": "json", /* 序列化方式,目前仅支持json */
    "port": 8888, /* 服务端口 */
    "interfaces": [ /* 服务方支持的调用接口列表 */
      "demo.service.IFoo"
    ],
    "threadPoolSize": 100 /* 服务方线程池大小,对于每个rpc请求,将提交至线程池中处理 */
  },

  "clients":[ /* rpc使用方列表 */
    {
      "appId": "foo", /* 服务id */
      "protocol": "json", /* 序列化方式,目前仅支持json */
      "threadPoolSize": 100, /* 线程池大小,客户端在每次rpc请求时,会将请求放入线程池中处理 */
      "methodDefaultTimeoutMillisecond": 1000, /* 调用超时时间 */
      "serverProviders":[], /* 指定服务方地址。如果设置该列表,rpc调用将使用配置的地址;如果没有设置,将从注册中心获取服务方地址 */
      "interfaces": [ /* 将使用的接口配置 */
        {
          "name": "demo.service.IFoo", /* 接口名称 */
          "methods": [ /* 函数超时设置,若不设置,将使用methodDefaultTimeoutMillisecond作为超时时间 */
            {
              "name": "sleep", /* 方法名 */
              "paramTypes": [ /* 方法参数类型列表 */
                "int"
              ],
              "timeoutMillisecond": 1000 /* 超时毫秒 */
            }
          ]
        }
      ]
    }
  ]
}
```

## 范例

light.rpc.core.RpcContextTest 中有示范代码

## 下一步

* 支持protocol buffer序列化方式
* 完善测试用例