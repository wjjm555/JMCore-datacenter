# WJCore · datacenter

[中文](README.md) | English

## 1. Series Introduction

**WJCore** is a series of open-source framework libraries for Android / Java, unified under the `com.wjjm.vehicle` namespace and released under the Apache License 2.0. Each module in the series has a single responsibility and low coupling, so they can be referenced independently as needed.

| Module | Description |
| --- | --- |
| ipccompat | Cross-process communication framework, wrapping AIDL/Binder with bidirectional sync, async and callback invocation |
| log | Logging framework (logcat + disk logging, derived from orhanobut/logger, Apache-2.0) |
| **datacenter** (this repo) | Data center: cross-process data storage based on ContentProvider + Room |
| https | Networking wrapper: unified HTTP/HTTPS client based on OkHttp + Retrofit |
| process | Rules and executor framework (InputRule / Actuator) |
| determiner | Condition evaluation framework (Compare / Determine) |
| filter | Filter framework |
| test | Test annotation framework |

See each repository for the introduction and documentation of the other modules. Development environment: Android Studio, minimum **Android 7.0 (API 24)+**.

## 2. Overview

datacenter is a key-value data center built on **ContentProvider + Room**, consisting of two modules:

- **datacentercore** — storage side. Ships a ContentProvider (authority `com.wjjm.vehicle.datacentercore`) with a Room 2.4.2 database, responsible for persistence and change notifications
- **datacentersdk** — client side. Provides the `DataClient` facade for cross-process CRUD and listening via ContentResolver, with no direct dependency on datacentercore

Features:

- **Cross-process** — the Provider exposes read/write through custom permissions (`normal` level), so data under one authority can be shared across processes / apps
- **Value as JSON** — any object is serialized to a JSON string on write (String values are stored as-is), and deserialized to the target type on read
- **Pluggable JSON** — the default is the self-developed `OrgJsonConverter` (built on Android's built-in org.json + reflection, **zero third-party dependencies**); swap in Gson or any other implementation globally via `JsonUtil.setConverter(...)`
- **Generics support** — built-in `TypeToken` captures generic types, supporting `List<T>` / `Map<K, V>` and nested generics for reads and writes
- **Change listening** — registers a ContentObserver per key; callbacks fire automatically on change with built-in listeners for String / primitives / generics / List / Map
- **Auto-initialization** — the Provider initializes Room on first creation; no manual setup required
- **Debug backdoor (off by default)** — a built-in broadcast receiver `Backdoor` allows CRUD via adb; `exported=false` makes it reachable only from shell / root, and it must be explicitly enabled with `Backdoor.setEnabled(context, true)`

## 3. API

### 3.1 Read & Write (DataClient)

```java
DataClient client = new DataClient(context);

client.insertToDataCenter("key", value);          // insert (or overwrite)
client.updateToDataCenter("key", value);          // update (insert if missing)
client.deleteFromDataCenter("key");               // delete
```

```java
// raw string
String raw = client.queryFromDataCenter("key");

// typed (returns null or a default value on parse failure; never throws)
User user = client.queryFromDataCenter("key", User.class);
int count = client.queryIntFromDataCenter("count");
long ts = client.queryLongFromDataCenter("ts");
boolean ok = client.queryBooleanFromDataCenter("ok");
double score = client.queryDoubleFromDataCenter("score");
float ratio = client.queryFloatFromDataCenter("ratio");

// generics (capture the type with TypeToken)
Type type = new TypeToken<List<User>>() {}.getType();
List<User> users = client.queryFromDataCenter("users", type);
```

### 3.2 Change Listening

```java
client.addListener("key", new SimpleOnChangedListener() {
    @Override
    public void onChanged(String value) {
        // called on data change (main thread)
    }
});

client.removeListener(listener);   // remove all registrations of this listener
```

Built-in listeners: `SimpleOnChangedListener` `StringOnChangedListener` `IntOnChangedListener` `LongOnChangedListener` `FloatOnChangedListener` `DoubleOnChangedListener` `BooleanOnChangedListener` `ClassOnChangedListener<T>` `TypeOnChangedListener<T>` `ListOnChangedListener` `MapOnChangedListener`.

### 3.3 JSON Plugin

```java
// replace the JSON implementation globally (e.g. plug in Gson)
JsonUtil.setConverter(new JsonConverter() {
    @Override
    public String toJson(Object src) { /* ... */ }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) { /* ... */ }

    @Override
    public <T> T fromJson(String json, Type typeOfT) { /* ... */ }
});
```

The default implementation `OrgJsonConverter` supports primitives / boxed types / String / enums / POJOs / List / Map / arrays and nested generics; static and transient fields are ignored during serialization.

### 3.4 Debug Backdoor

```java
Backdoor.setEnabled(context, true);   // off by default; enable explicitly
```

```bash
# insert / delete / update / query (action: 1 insert, 2 delete, 3 update, 4 query, anything else dumps all)
adb shell am broadcast -a com.wjjm.vehicle.datacentercore.BACKDOOR \
    --ei action 1 --es key k --es value v
```

## 4. Notes

1. **The framework never throws at runtime**: query failures and JSON parse failures return null or a typed default value (int → 0, boolean → false, String → "", objects → null).
2. **String values are stored as-is**: String writes skip JSON serialization, and reads return the raw value (a stored "null" returns null).
3. **Listener callbacks run on the main thread**; avoid long-running work inside callbacks.
4. **Database operations are synchronous** (main-thread access allowed); perform bulk writes on a background thread.
5. **Cross-process permissions**: the Provider defines the custom `normal`-level permissions `com.wjjm.vehicle.datacentercore.READ` / `WRITE`; other apps must declare them in their manifest to gain access.
6. **The backdoor is off by default**: the `Backdoor` receiver is declared `enabled=false` and `exported=false`; only after an explicit `Backdoor.setEnabled(context, true)` can shell / root explicit broadcasts reach it.
7. The Provider is declared in datacentercore's manifest and merged into the host app via manifest merger; include both datacentercore and datacentersdk in the same app to use it directly.

## 5. Usage

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // storage side (registers the Provider, persists data)
    implementation 'com.github.wjjm555.WJCore-datacenter:datacentercore:1.0.0'
    // client side (read, write and listen)
    implementation 'com.github.wjjm555.WJCore-datacenter:datacentersdk:1.0.0'

    // or use the aggregate module (pulls in both modules above)
    // implementation 'com.github.wjjm555:WJCore-datacenter:1.0.0'
}
```

## 6. License

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
