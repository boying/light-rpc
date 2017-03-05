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
// 创建rpc上下文
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

## 配置文件说明

## 范例

## 下一步
