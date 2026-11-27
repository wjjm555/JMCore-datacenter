# WJCore · datacenter

[English](README_EN.md) | 中文

## 一、系列介绍

**WJCore** 是一系列面向 Android / Java 的开源框架库,统一使用 `com.wjjm.vehicle` 命名空间,全部采用 Apache License 2.0 协议发布。系列内模块职责单一、低耦合,可按需单独引用。

| 模块 | 说明 |
| --- | --- |
| ipccompat | 跨进程通信框架,封装 AIDL/Binder,提供双向同步、异步、回调调用 |
| log | 日志框架(logcat + 磁盘落盘,源自 orhanobut/logger,Apache-2.0) |
| **datacenter**(本仓库) | 数据中心,基于 ContentProvider + Room 的跨进程数据存储 |
| https | 网络封装,基于 OkHttp + Retrofit 的统一 HTTP/HTTPS 客户端 |
| process | 规则与执行器框架(InputRule / Actuator) |
| determiner | 条件判定框架(Compare / Determine) |
| filter | 过滤器框架 |
| test | 测试注解框架 |

其余模块的介绍与文档见各自仓库。本仓库开发环境:Android Studio,最低支持 **Android 7.0(API 24)及以上**。

## 二、框架介绍

datacenter 是一个基于 **ContentProvider + Room** 的键值数据中心,由两个模块组成:

- **datacentercore** —— 存储端。内置 ContentProvider(authority 为 `com.wjjm.vehicle.datacentercore`)与 Room 2.4.2 数据库,负责数据落盘与变更通知
- **datacentersdk** —— 客户端。提供 `DataClient` 门面,通过 ContentResolver 完成跨进程增删改查与监听,不直接依赖 datacentercore

特点如下:

- **跨进程** —— Provider 通过自定义权限(`normal` 级)开放读写,同一 Authority 下的数据可被多个进程 / 应用共享
- **值即 JSON** —— 任意对象写入时自动序列化为 JSON 字符串(String 原样存储),读取时按目标类型反序列化
- **JSON 可插拔** —— 默认使用自研 `OrgJsonConverter`(基于 Android 自带 org.json + 反射,**零三方依赖**),可通过 `JsonUtil.setConverter(...)` 全局替换为 Gson 或其他实现
- **泛型支持** —— 内置 `TypeToken` 捕获泛型类型,支持 `List<T>` / `Map<K, V>` 等嵌套泛型的读写
- **变更监听** —— 按 key 注册 ContentObserver,数据变化自动回调,内置 String / 基础类型 / 泛型 / List / Map 等多种监听器
- **自动初始化** —— Provider 首次创建时自动完成 Room 初始化,使用方无需手动调用
- **调试后门(默认关闭)** —— 内置广播后门 `Backdoor`,可经 adb 对数据执行增删改查,`exported=false` 仅 shell / root 可达,需显式调用 `Backdoor.setEnabled(context, true)` 开启

## 三、API

### 1. 数据读写(DataClient)

```java
DataClient client = new DataClient(context);

client.insertToDataCenter("key", value);          // 插入(或覆盖)
client.updateToDataCenter("key", value);          // 更新(不存在则插入)
client.deleteFromDataCenter("key");               // 删除
```

```java
// 原始字符串
String raw = client.queryFromDataCenter("key");

// 指定类型(解析失败返回 null 或默认值,不抛异常)
User user = client.queryFromDataCenter("key", User.class);
int count = client.queryIntFromDataCenter("count");
long ts = client.queryLongFromDataCenter("ts");
boolean ok = client.queryBooleanFromDataCenter("ok");
double score = client.queryDoubleFromDataCenter("score");
float ratio = client.queryFloatFromDataCenter("ratio");

// 泛型(借助 TypeToken 捕获类型)
Type type = new TypeToken<List<User>>() {}.getType();
List<User> users = client.queryFromDataCenter("users", type);
```

### 2. 变更监听

```java
client.addListener("key", new SimpleOnChangedListener() {
    @Override
    public void onChanged(String value) {
        // 数据变化时回调(主线程)
    }
});

client.removeListener(listener);   // 移除该监听器的全部注册
```

内置监听器:`SimpleOnChangedListener` `StringOnChangedListener` `IntOnChangedListener` `LongOnChangedListener` `FloatOnChangedListener` `DoubleOnChangedListener` `BooleanOnChangedListener` `ClassOnChangedListener<T>` `TypeOnChangedListener<T>` `ListOnChangedListener` `MapOnChangedListener`。

### 3. JSON 插件

```java
// 全局替换 JSON 实现(如接入 Gson)
JsonUtil.setConverter(new JsonConverter() {
    @Override
    public String toJson(Object src) { /* ... */ }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) { /* ... */ }

    @Override
    public <T> T fromJson(String json, Type typeOfT) { /* ... */ }
});
```

默认实现 `OrgJsonConverter` 支持:基本类型 / 包装类型 / String / 枚举 / POJO / List / Map / 数组及嵌套泛型,序列化忽略 static / transient 字段。

### 4. 调试后门

```java
Backdoor.setEnabled(context, true);   // 默认关闭,需显式开启
```

```bash
# 插入 / 删除 / 更新 / 查询(action: 1 插入,2 删除,3 更新,4 查询,其他为全量打印)
adb shell am broadcast -a com.wjjm.vehicle.datacentercore.BACKDOOR \
    --ei action 1 --es key k --es value v
```

## 四、注意事项

1. **框架运行时不抛异常**:查询失败、JSON 解析失败均返回 null 或类型默认值(int → 0、boolean → false、String → ""、对象 → null)。
2. **String 原样存取**:String 类型写入时不做 JSON 序列化,读取时原样返回(存储值为 "null" 时返回 null)。
3. **监听回调在主线程**,请勿在回调中执行耗时操作。
4. **数据库操作为同步执行**(允许主线程访问),大量数据写入请在后台线程进行。
5. **跨进程权限**:Provider 定义了 `com.wjjm.vehicle.datacentercore.READ` / `WRITE` 两个 `normal` 级自定义权限,其他应用需在 Manifest 中声明对应权限方可访问。
6. **后门默认关闭**:`Backdoor` 的 receiver 在 Manifest 中 `enabled=false` 且 `exported=false`,只有显式调用 `Backdoor.setEnabled(context, true)` 后,shell / root 的显式广播才能触达。
7. Provider 由 datacentercore 的 Manifest 提供,经 manifest merger 合并进宿主应用;同一应用内同时引入 datacentercore 与 datacentersdk 即可直接使用。

## 五、使用

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // 存储端(注册 Provider,负责落盘)
    implementation 'com.github.wjjm555.WJCore-datacenter:datacentercore:1.0.0'
    // 客户端(读写与监听)
    implementation 'com.github.wjjm555.WJCore-datacenter:datacentersdk:1.0.0'

    // 或直接引入聚合模块(同时包含以上两个模块)
    // implementation 'com.github.wjjm555:WJCore-datacenter:1.0.0'
}
```

## 六、License

```
Copyright 2026 WJJM

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
