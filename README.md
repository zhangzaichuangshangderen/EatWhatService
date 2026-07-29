# wxcloudrun-springboot
[![GitHub license](https://img.shields.io/github/license/WeixinCloud/wxcloudrun-express)](https://github.com/WeixinCloud/wxcloudrun-express)
![GitHub package.json dependency version (prod)](https://img.shields.io/badge/maven-3.6.0-green)
![GitHub package.json dependency version (prod)](https://img.shields.io/badge/jdk-11-green)

微信云托管 Java Springboot 框架模版，实现简单的计数器读写接口，使用云托管 MySQL 读写、记录计数值。

![](https://qcloudimg.tencent-cloud.cn/raw/be22992d297d1b9a1a5365e606276781.png)


## 快速开始
前往 [微信云托管快速开始页面](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/basic/guide.html)，选择相应语言的模板，根据引导完成部署。

## 本地调试
下载代码在本地调试，请参考[微信云托管本地调试指南](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/guide/debug/)。

## 实时开发
代码变动时，不需要重新构建和启动容器，即可查看变动后的效果。请参考[微信云托管实时开发指南](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/guide/debug/dev.html)

## Dockerfile最佳实践
请参考[如何提高项目构建效率](https://developers.weixin.qq.com/miniprogram/dev/wxcloudrun/src/scene/build/speed.html)

## 目录结构说明
~~~
.
├── Dockerfile                      Dockerfile 文件
├── LICENSE                         LICENSE 文件
├── README.md                       README 文件
├── container.config.json           模板部署「服务设置」初始化配置（二开请忽略）
├── mvnw                            mvnw 文件，处理mevan版本兼容问题
├── mvnw.cmd                        mvnw.cmd 文件，处理mevan版本兼容问题
├── pom.xml                         pom.xml文件
├── settings.xml                    maven 配置文件
├── springboot-cloudbaserun.iml     项目配置文件
└── src                             源码目录
    └── main                        源码主目录
        ├── java                    业务逻辑目录
        └── resources               资源文件目录
~~~


## 服务 API 文档

### 食材管理 CRUD

> 说明：该接口为全局食材库（不按用户隔离），删除采用软删除（`isDeleted=1`）。

#### `POST /api/ingredients`

创建食材。

请求体示例：

```json
{
  "name": "低脂鸡肉肠",
  "category": "protein",
  "kcal": 170,
  "carbs": 6.5,
  "protein": 15,
  "fat": 6,
  "unit": "g"
}
```

> `category` 可选值：`combo`、`carbs`、`protein`、`fiber`、`fat`  
> `unit` 可选值：`g`、`ml`、`个`、`份`

#### `GET /api/ingredients`

查询未删除的食材列表，按 `updatedAt desc` 排序。

#### `PUT /api/ingredients/{id}`

更新指定食材，参数与创建接口一致。

#### `DELETE /api/ingredients/{id}`

软删除指定食材（更新 `isDeleted` 为 `1`）。

### `GET /api/count`

获取当前计数

#### 请求参数

无

#### 响应结果

- `code`：错误码
- `data`：当前计数值

##### 响应结果示例

```json
{
  "code": 0,
  "data": 42
}
```

#### 调用示例

```
curl https://<云托管服务域名>/api/count
```



### `POST /api/count`

更新计数，自增或者清零

#### 请求参数

- `action`：`string` 类型，枚举值
  - 等于 `"inc"` 时，表示计数加一
  - 等于 `"clear"` 时，表示计数重置（清零）

##### 请求参数示例

```
{
  "action": "inc"
}
```

#### 响应结果

- `code`：错误码
- `data`：当前计数值

##### 响应结果示例

```json
{
  "code": 0,
  "data": 42
}
```

#### 调用示例

```
curl -X POST -H 'content-type: application/json' -d '{"action": "inc"}' https://<云托管服务域名>/api/count
```

## 使用注意
如果不是通过微信云托管控制台部署模板代码，而是自行复制/下载模板代码后，手动新建一个服务并部署，需要在「服务设置」中补全以下环境变量，才可正常使用，否则会引发无法连接数据库，进而导致部署失败。
- MYSQL_ADDRESS
- MYSQL_PASSWORD
- MYSQL_USERNAME
- MYSQL_DATABASE（可选，默认 `eatwhat`）
以上变量的值请按实际情况填写。如果使用云托管内 MySQL，可以在控制台 MySQL 页面获取相关信息。

## 关联前端仓库与联调

- 前端仓库路径：`/Users/wuyingming/code/wechat/eatwhat`
- 小程序通过 `wx.request` 访问本服务 `/api/ingredients` 完成“我的食材”CRUD。
- 前端需要设置后端域名（建议存储键：`EATWHAT_API_BASE_URL`），例如：
  - `https://<你的云托管服务域名>`
  - 本地联调可使用可访问的内网穿透域名


## License

[MIT](./LICENSE)
