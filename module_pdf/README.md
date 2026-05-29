# module_pdf 使用说明

PDF 编辑 SDK，对外以 **AAR** 形式分发，宿主通过 `com.yung.module_pdf.api` 包下的公开 API 集成。

- **Maven 坐标**：`com.yung.module:pdf:1.0.0`
- **最低 SDK**：26
- **UI 框架**：Jetpack Compose
- **包名**：`com.yung.module_pdf`

---

## 快速开始

### 1. 添加依赖

**方式 A：同工程源码依赖（本地开发推荐）**

```kotlin
// 宿主模块 build.gradle.kts
dependencies {
    api(project(":module_pdf"))
}
```

**方式 B：Maven 依赖（正式集成 / 跨工程）**

```kotlin
// settings.gradle.kts — 确保仓库可解析到 pdf
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://your-maven-repo/") }
        // 本地验证时可加：maven { url = uri("${rootDir}/repo") }
    }
}

// 宿主模块 build.gradle.kts
dependencies {
    api("com.yung.module:pdf:1.0.0")
}
```

> **不推荐** `api(files("libs/pdf-1.0.0.aar"))`：裸 AAR 不含 POM，传递依赖不会自动解析，需手动补全 module_pdf 的全部依赖。

### 2. 初始化 SDK

在宿主 `Application.onCreate()` 中调用（**必须**，且早于任何 PDF 功能）：

```kotlin
import com.yung.module_pdf.api.PdfSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PdfSdk.init(application = this)
    }
}
```

### 3. 打开 PDF 功能

```kotlin
import com.yung.module_pdf.api.PdfLauncher
import com.yung.module_pdf.api.PdfSelectMode

// 预览模式：选文件 → 预览 / 编辑
PdfLauncher.openPdfSelect(context, PdfSelectMode.PREVIEW)

// 管理模式：选文件 → 页面管理
PdfLauncher.openPdfSelect(context, PdfSelectMode.MANAGEMENT)
```

---

## 公开 API

所有对外接口位于 `com.yung.module_pdf.api`，`internal` 包下的类请勿直接使用。

| 类 / 接口 | 说明 |
|-----------|------|
| `PdfSdk` | SDK 入口，`init()` / `recentFiles()` |
| `PdfSdkConfig` | 初始化配置（如自定义最近文件存储） |
| `PdfLauncher` | 启动 PDF 相关页面 |
| `PdfSelectMode` | 选择页模式：`PREVIEW` / `MANAGEMENT` |
| `PdfRecentFileApi` | 最近文件观察与删除 |
| `PdfRecentFile` | 最近文件数据模型 |
| `RecentFileFormat` | 文件类型：PDF / Word / Excel / PPT / Image |
| `RecentFileStore` | 宿主自定义最近文件存储（可选） |

---

## 最近文件

### 默认行为

未配置 `RecentFileStore` 时，SDK 使用内部 Room 数据库（`recent_file.db`）读写最近打开的文件。

### 观察最近文件

```kotlin
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yung.module_pdf.api.PdfSdk
import com.yung.module_pdf.api.RecentFileFormat

val recentFiles by PdfSdk.recentFiles()
    .observeByFormat(RecentFileFormat.PDF)
    .collectAsState(initial = emptyList())
```

### 删除记录

```kotlin
// 删除单条
PdfSdk.recentFiles().delete(id)

// 清空全部
PdfSdk.recentFiles().clearAll()
```

### 自定义存储（可选）

若宿主希望最近文件写入自己的数据库，实现 `RecentFileStore` 并在初始化时传入：

```kotlin
import com.yung.module_pdf.api.PdfSdk
import com.yung.module_pdf.api.PdfSdkConfig

PdfSdk.init(
    application = this,
    config = PdfSdkConfig(recentFileStore = yourRecentFileStore),
)
```

`RecentFileStore` 需实现：

- `observeAll()` / `observeByFormat(format)`
- `upsert(file)` — SDK 打开文件时回调
- `delete(id)` / `deleteByPath(path, format)`

参考宿主工程示例：`app/src/main/java/com/yung/compose/recent/HostRecentFileStore.kt`。

---

## Manifest 与权限

SDK 的 `AndroidManifest.xml` 会在合并时自动注入，宿主**无需**重复声明 Activity / FileProvider。

### 自动合并内容

- **权限**：存储读写、图片读取、管理所有文件（按需申请）
- **Activity**：文件选择、预览、编辑、搜索、页面管理
- **FileProvider**：authority 为 `{applicationId}.module_pdf.fileProvider`

宿主只需保证运行时权限申请流程正常（SDK 内部使用 PermissionX 处理）。

---

## 功能页面说明

| 页面 | 触发方式 |
|------|----------|
| 文件选择 | `PdfLauncher.openPdfSelect()` |
| PDF 预览 | 选择文件后（PREVIEW 模式） |
| PDF 编辑 | 预览页内进入 |
| 页面管理 | 选择文件后（MANAGEMENT 模式）或编辑页内 |

---

## Maven 发布

module_pdf 已配置 `maven-publish`，发布脚本见项目根目录 `nexus_maven_push.gradle`。

### 配置项（module_pdf/build.gradle.kts）

```kotlin
extra.apply {
    set("aarId", "pdf")                          // artifactId
    set("aarDescription", "PDF Editor Module")
    set("versionName", "1.0.0")                  // 版本号
    set("publishToServer", false)                // false=本地 repo/，true=远程
    set("groupID", project.findProperty("GROUP_MODULE"))  // com.yung.module
    set("localPath", "${rootProject.projectDir}/repo")
}
```

### 发布命令

```bash
# 本地发布到项目根目录 repo/
./gradlew :module_pdf:publishReleasePublicationToMavenRepository

# 远程发布：先将 publishToServer 改为 true，并配置 gradle.properties 中的 REPOSITORY_URL、NEXUS_*
./gradlew :module_pdf:publishReleasePublicationToMavenRepository
```

发布后产物：

```
repo/com/yung/module/pdf/1.0.0/
├── pdf-1.0.0.aar
├── pdf-1.0.0-sources.jar
├── pdf-1.0.0.pom
└── pdf-1.0.0.module
```

---

## 宿主工程要求

- **minSdk ≥ 26**
- 宿主 Activity 建议使用 `FragmentActivity`（Compose 页面需要）
- 宿主需启用 **Jetpack Compose**（SDK UI 基于 Compose）
- 使用 Maven 集成时，Gradle 会通过 POM 自动拉取传递依赖（Room、Compose、pdfbox 等）

---

## 常见问题

### Q：`PdfSdk.init()` 报未初始化？

确保在 `Application.onCreate()` 最早阶段调用 `PdfSdk.init()`，且调用发生在任何 `PdfLauncher` / `recentFiles()` 之前。

### Q：Maven 依赖找不到 `com.yung.module:pdf:1.0.0`？

1. 确认已执行 publish 任务
2. `settings.gradle.kts` 中配置了对应 Maven 仓库（本地 `repo/` 或远程）
3. 版本号与 `module_pdf/build.gradle.kts` 中 `versionName` 一致

### Q：`api(libs.pdf)` 和 `api(project(":module_pdf"))` 选哪个？

| 场景 | 推荐 |
|------|------|
| 同仓库本地开发 | `project(":module_pdf")` |
| 给其他 App / 工程集成 | Maven `com.yung.module:pdf:x.x.x` |

### Q：SDK 和 AAR 是什么关系？

- **AAR**：Android 库的二进制文件格式（`.aar`）
- **SDK**：对外提供的能力与接入规范（`PdfSdk`、`PdfLauncher`、Maven 发布等）

module_pdf 按 SDK 设计，发布产物是 AAR。

### Q：能否只拷贝 AAR 到 libs 目录？

可以但不推荐。`files("libs/pdf-1.0.0.aar")` 不会携带传递依赖，需手动在宿主 `build.gradle.kts` 中声明 module_pdf 的全部依赖，维护成本高。

---

## 版本记录

| 版本 | 说明 |
|------|------|
| 1.0.0 | 初始版本：PDF 预览 / 编辑 / 页面管理 / 最近文件 |
