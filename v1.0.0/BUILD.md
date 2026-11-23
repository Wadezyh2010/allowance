# v1.0.0 构建说明

本版本源码使用标准的 NeoForge ModDevGradle 工程结构（`build.gradle` 使用
`net.neoforged.moddev` 1.0.17 插件，目标 NeoForge 21.1.235）。

## 环境要求

- JDK 21（本机可使用 Minecraft 启动器自带的 `java-runtime-delta`，含 javac）
- Gradle 8.14+
- 网络可访问：`maven.neoforged.net`、Maven Central、Mojang 官方源

## 标准 Gradle 构建

```bash
cd v1.0.0
gradle build
```

产物：`build/libs/allowance-1.0.0.jar`

## 国内网络问题与替代方案

`maven.neoforged.net` 在国内可能被阻断（DNS 污染 / 连接重置），且 ModDevGradle
所需的 `net.neoforged:neoform-runtime` 组件目前没有公开镜像。
`build.gradle` / `settings.gradle` 已额外配置以下后备仓库，能缓解大部分依赖下载：

1. 阿里云公共仓库 `https://maven.aliyun.com/repository/public`
2. 阿里云 Gradle 插件镜像
3. BMCLAPI `https://bmclapi2.bangbang93.com/maven/`（可提供 neoforge / neoform 工件）

若 Gradle 管线仍卡在 `createArtifactManifest`（NFRT 不可获取），可采用与
v1.0.0 成品 jar 相同的手动编译管线：

1. 从 Mojang 官方源下载 1.21.1 `client.jar` 与官方混淆映射表 `client_mappings.txt`
   （`piston-data.mojang.com` 国内可直接访问）；
2. 使用 AutoRenamingTool 将混淆 jar 重映射为官方命名（注意加 `--reverse`）；
3. 以 `mc-official.jar + neoforge-universal.jar + FML loader/bus + 常规库`
   为 classpath 直接 `javac` 编译；
4. `jar --create` 将 classes 与 `src/main/resources` 一并打包。

成品 jar 已附带在本目录（`allowance-1.0.0.jar`），无需自行构建即可使用。
