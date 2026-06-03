# module_iot 使用说明

IoT MQTT 模块，基于 **Eclipse Paho** 连接 MQTT Broker，提供 Compose 调试页面。

- **最低 SDK**：26
- **UI 框架**：Jetpack Compose
- **包名**：`com.yung.iot`
- **路由**：`/iot/main`（`RoutePath.Iot.MAIN`）

---

## 快速开始

### 1. 添加依赖

```kotlin
// 宿主模块 build.gradle.kts
dependencies {
    api(project(":module_iot"))
}
```

`settings.gradle.kts` 需包含模块：

```kotlin
include(":module_iot")
```

> 模块依赖 `jitpack.io` 解析 `com.github.hannesa2:paho.mqtt.android`（适配 Android 13+），根工程 `settings.gradle.kts` 中需已有 JitPack 仓库。

### 2. 打开 IoT 页面

**ARouter 跳转**

```kotlin
import com.alibaba.android.arouter.launcher.ARouter
import com.yung.route.RoutePath

ARouter.getInstance().build(RoutePath.Iot.MAIN).navigation(context)
```

**HostNavigator（module_host 已封装）**

```kotlin
HostNavigator.toIot(context)
```

---

## MQTT 配置

连接参数写在 `IotMainActivity` 的 `companion object` 中，按需修改：

```kotlin
private const val SERVER_URI = "ssl://your-broker.example.com:8883"
private const val USERNAME = "your_username"
private const val PASSWORD = "your_password"//EMQX->访问控制->客户端认证->创建密码
private const val CLIENT_ID = "android_iot_client"  // 每个客户端需唯一，不是MQTTX上的Client ID，是自定义的
private const val TOPIC = "a/b"//MQTTX上添加的订阅主题
```

| 参数 | 说明 |
|------|------|
| `SERVER_URI` | Broker 地址，见下方协议对照 |
| `USERNAME` / `PASSWORD` | 认证凭据 |  **EMQX->访问控制->客户端认证->创建用户名|密码**
| `CLIENT_ID` | 客户端 ID，**勿与其他客户端（如 MQTTX）重复** |
| `TOPIC` | 进入页面后自动订阅的主题 |

### 协议对照（MQTTX ↔ Paho）

| MQTTX | Paho `SERVER_URI` |
|-------|-------------------|
| `mqtt://` + 1883 | `tcp://host:1883` |
| `mqtts://` + 8883 | `ssl://host:8883` |

使用 TLS（8883）时，代码中需设置：

```kotlin
socketFactory = SSLSocketFactory.getDefault()
```

对应 MQTTX 的 **CA signed server certificate**（系统 CA 校验）。

---

## 页面功能

`IotMainActivity` 进入后自动：

1. 连接 MQTT Broker
2. 订阅 `TOPIC`
3. 展示连接状态与最近收到的消息

手动操作：

- **发布消息**：向 `TOPIC` 发送 `Hello from Android`
- **断开连接**：主动断开 MQTT

---

## 依赖说明

| 库 | 版本 | 用途 |
|----|------|------|
| `org.eclipse.paho.client.mqttv3` | 1.2.5 | MQTT 协议客户端 |
| `com.github.hannesa2:paho.mqtt.android` | 4.2.4 | Android 后台 Service（修复 Android 13+ `RECEIVER_EXPORTED` 问题） |

> 勿使用已停更的 `org.eclipse.paho:org.eclipse.paho.android.service:1.1.1`，在 Android 13+ 上会崩溃。

---

## Manifest 合并

模块已声明以下权限与 Service，合并到宿主 App 后生效：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<service
    android:name="info.mqtt.android.service.MqttService"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

---

## 常见问题

### 连接失败

1. 确认 `SERVER_URI` 协议与端口正确（`mqtts://8883` → `ssl://...:8883`）
2. 确认用户名、密码与 Broker 一致
3. 确认 `CLIENT_ID` 未被其他客户端占用
4. 查看 Logcat 标签 `IotMainActivity` 与页面上的错误详情

### `cannot start service org.eclipse.paho.android.service.MqttService`

代码与依赖版本不一致。应使用 `info.mqtt.android.service.MqttAndroidClient`，并确保依赖为 `hannesa2` 维护版。修改后 **Clean + Rebuild** 重新安装。

### `RECEIVER_EXPORTED` 崩溃

旧版 Paho Android Service 在 Android 13+ 的已知问题，已通过 `hannesa2:paho.mqtt.android:4.2.4` 解决。

---

## 参考
- [创建部署] (https://docs.emqx.com/zh/cloud/latest/deployments/tls_ssl.html)
- [Android 使用 Kotlin 连接 MQTT（EMQX）](https://www.emqx.com/zh/blog/android-connects-mqtt-using-kotlin)
- [paho.mqtt.android（hannesa2 维护版）](https://github.com/hannesa2/paho.mqtt.android)
