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

所有业务接口均按用户隔离：用户身份来自登录 token 解析出的 `openid`（过渡期仍兼容云托管注入的 `X-WX-OPENID`）；客户端不得提交或覆盖 `userId`。

### 食材目录

#### `GET /api/foods`

返回基础食材与当前用户自定义食材的合并列表。基础食材由服务端 `foods.json` 提供，自定义食材来自 MySQL。响应项的 `custom` 字段用于区分是否为用户自定义食材。小程序首页使用该接口，不再读取前端本地基础食材文件。

### 食材管理 CRUD

> 说明：该接口为**按用户隔离**的「我的食材」库，用户身份来自微信云托管注入的 `X-WX-OPENID` 请求头（小程序需使用 `wx.cloud.callContainer` 调用）。删除采用软删除（`isDeleted=1`）。

> 鉴权：无 openid 时返回 `{"code":0,"errorMsg":"未登录，请从小程序访问"}`。

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
  "fiber": 2.5,
  "approxUnit": "1根约40g",
  "unit": "g"
}
```


> `category` 可选值：`combo`、`carbs`、`protein`、`fiber`、`fat`  
> `unit` 可选值：`g`、`ml`、`个`、`份`

#### `GET /api/ingredients`

查询当前用户未删除的食材列表，按 `updatedAt desc` 排序。

#### `PUT /api/ingredients/{id}`

更新指定食材，参数与创建接口一致。

#### `DELETE /api/ingredients/{id}`

软删除指定食材（更新 `isDeleted` 为 `1`）。

### 指定日期饮食记录

#### `GET /api/diet-records/month/{month}`

拉取当前用户指定月份的轻量食记摘要，`month` 必须为 `yyyy-MM`。只返回有记录的日期和餐次键，供月历标记使用；不返回食材与营养详情。

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {
    "month": "2026-07",
    "days": [
      { "date": "2026-07-30", "mealKeys": ["breakfast", "lunch"] }
    ]
  }
}
```

#### `GET /api/diet-records/{date}`

拉取当前用户指定日期的饮食记录，`date` 必须为 `yyyy-MM-dd`。`meals` 固定按早餐、午餐、加餐、晚餐返回；无记录的单餐 `record` 为 `null`，全天无数据时四个 `record` 均为 `null`。

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {
    "date": "2026-07-30",
    "meals": [
      { "mealKey": "breakfast", "mealLabel": "早餐", "record": null },
      { "mealKey": "lunch", "mealLabel": "午餐", "record": null },
      { "mealKey": "snack", "mealLabel": "加餐", "record": null },
      { "mealKey": "dinner", "mealLabel": "晚餐", "record": null }
    ]
  }
}
```

#### `PUT /api/diet-records/{date}/{mealKey}`

幂等新增或覆盖指定餐次。`mealKey` 仅允许 `breakfast`、`lunch`、`snack`、`dinner`。请求体示例：

```json
{
  "id": "log_1785396600000",
  "score": 89.4,
  "totals": { "kcal": 400.7, "carbs": 51.9, "protein": 26.8, "fat": 10.1, "fiber": 5.3 },
  "items": [
    { "id": "c11", "name": "蒸土豆/马铃薯", "category": "carbs", "unit": "g", "amount": 150, "kcal": 69, "carbs": 15.3, "protein": 2, "fat": 0.1 }
  ],
  "acceptedAt": "2026-07-30T07:30:00.000Z",
  "dayGoalKcal": 1800
}
```

成功响应与查询接口相同，返回更新后的四个餐次槽位。

#### `DELETE /api/diet-records/{date}/{mealKey}`

清空指定餐次；即使该餐原本为空也返回成功。成功响应为删除后的四个餐次槽位。

### 用户能量目标

#### `GET /api/nutrition-goal`

查询当前用户的能量目标。尚未设置时 `data` 为 `null`。

#### `PUT /api/nutrition-goal`

幂等创建或更新当前用户的能量目标。客户端不能提交 `userId`。

```json
{
  "targetKcal": 1800,
  "source": "calculator",
  "bmrKcal": 1420,
  "tdeeKcal": 2110,
  "goalType": "lose"
}
```

- `targetKcal`：800–4500 kcal。
- `source`：`manual` 或 `calculator`。
- `goalType`：`maintain`、`lose` 或 `gain`。
- `source=calculator` 时必须同时提供 `bmrKcal` 与 `tdeeKcal`；手动目标可为空。

### 邀请活动

#### `POST /api/invites/bind`

绑定邀请关系，请求体：

```json
{
  "inviterUserId": "邀请人 userId"
}
```

说明：
- 被邀请人身份由服务端 openid 识别，客户端不能提交被邀请人 `userId`。
- 同一被邀请人只允许绑定一次邀请关系，且不能绑定自己。

#### `GET /api/invites/progress`

查询当前用户邀请进度，当前活动门槛为邀请 `20` 人。响应示例：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {
    "totalInvited": 6,
    "qualifiedCount": 3,
    "targetCount": 20,
    "items": []
  }
}
```

#### `GET /api/invites/leaderboard?limit=50`

查询邀请榜单，按“最早完成邀请 20 人”排序，默认返回前 50 名。响应示例：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {
    "targetCount": 20,
    "rewardLimit": 50,
    "list": [
      {
        "rank": 1,
        "inviterUserId": "openid_xxx",
        "inviterNickName": "认真吃饭",
        "inviterAvatarUrl": "https://...",
        "invitedCount": 21,
        "completedAt": "2026-08-17T09:12:00"
      }
    ]
  }
}
```

#### `POST /api/invites/cleanup-dirty`

清洗邀请活动脏数据（仅限白名单管理员调用）。请求体示例：

```json
{
  "dryRun": true,
  "syncQualifiedFromDietRecords": true
}
```

字段说明：
- `dryRun`：`true` 时仅返回预计影响条数，不执行更新；`false` 时执行清洗。默认 `true`。
- `syncQualifiedFromDietRecords`：是否按“DietRecords 至少一条记录”回填 `UserInvites.isQualified`。默认 `true`。

响应示例：

```json
{
  "code": 0,
  "errorMsg": "",
  "data": {
    "dryRun": true,
    "syncQualifiedFromDietRecords": true,
    "dirtyNickNameCount": 12,
    "cleanedNickNameCount": 0,
    "canBeQualifiedCount": 3,
    "qualifiedSyncedCount": 0
  }
}
```

鉴权说明：
- 仅 `INVITE_CLEANUP_ADMIN_OPENIDS` 环境变量中配置的 openid 可调用（逗号分隔）。
- 未登录或不在白名单时返回无权限错误。

### 用户档案（昵称/头像）

#### `GET /api/users/me`

按当前用户 `openid` 查询档案；若不存在会自动创建空档案后返回。用于小程序启动时完成“首次建档”。

#### `PUT /api/users/me`

更新当前用户昵称与头像，入参可为空；空值不会覆盖已有内容。

```json
{
  "nickName": "认真吃饭",
  "avatarUrl": "https://thirdwx.qlogo.cn/mmopen/..."
}
```

## 使用注意
如果不是通过微信云托管控制台部署模板代码，而是自行复制/下载模板代码后，手动新建一个服务并部署，需要在「服务设置」中补全以下环境变量，才可正常使用，否则会引发无法连接数据库，进而导致部署失败。
- MYSQL_ADDRESS
- MYSQL_PASSWORD
- MYSQL_USERNAME
- MYSQL_DATABASE（可选，默认 `eatwhat`）
- INVITE_CLEANUP_ADMIN_OPENIDS（可选，邀请脏数据清洗接口白名单，逗号分隔 openid）
以上变量的值请按实际情况填写。如果使用云托管内 MySQL，可以在控制台 MySQL 页面获取相关信息。

## 数据库升级（已有环境）

若 `Ingredients` 表已存在但缺少 `userId` 字段，请先执行：

`src/main/resources/db_migration_add_userId.sql`

再执行以下脚本，增加食材的膳食纤维/份量说明字段与饮食记录表：

`src/main/resources/db_migration_add_diet_records_and_ingredient_fields.sql`

最后执行用户能量目标表迁移：

`src/main/resources/db_migration_add_nutrition_goals.sql`

最后执行用户档案表迁移：

`src/main/resources/db_migration_add_users.sql`

迁移脚本不会自动执行，也不要直接在生产库试跑；应先备份并在隔离库验证。若 `fiber` 或 `approxUnit` 已由其他变更添加，请跳过脚本中对应的 `ALTER TABLE`。

## 部署到自建云服务

1. `cp .env.example .env` 并填好数据库、微信小程序与 JWT 配置。
2. `docker compose up -d` 启动 `app`（Spring Boot）+ `mysql` 两个容器，app 监听本机 `8080`。
3. 用已有的域名 + 证书，在服务器上配置 Nginx 反代（示例见 `nginx/nginx.conf`），将
   `443 -> 127.0.0.1:8080`，小程序通过 `https://你的域名/api/...` 访问。
4. 微信公众平台「开发管理 - 开发设置」中，将服务器域名 `request 合法域名` 加入你的 HTTPS 域名。

## 用户登录流程（迁移核心）

离开云托管后，微信不再自动注入 `openid`，改为标准的小程序登录态：

1. 小程序调用 `wx.login()` 获取 `code`；
2. `POST /api/auth/login`，请求体 `{"code":"<code>"}`；
3. 后端用 `code` 调微信 `code2session` 换 `openid`，签发 **JWT** 返回 `{"token":"...","openid":"..."}`；
4. 小程序把 `token` 存入本地存储，后续请求在请求头携带 `Authorization: Bearer <token>`；
5. 后端 `AuthInterceptor` 统一校验 token，把 `openid` 注入请求，业务控制器无需改动。

> 过渡期仍兼容云托管注入的 `X-WX-OPENID` 请求头，便于灰度双跑。

## 关联前端仓库与联调

- 前端仓库路径：`/Users/lemon.wu/Code/wechat/EatWhat`
- 小程序**不再**使用 `wx.cloud.callContainer`，改为直接用 `wx.request` 访问 `https://你的域名/api/...`，并在请求头带上登录 token。
- 前端需新增登录逻辑：启动时 `wx.login()` → `/api/auth/login` 换取并缓存 token；所有 `/api/*` 请求附加 `Authorization: Bearer <token>`。
- 用户身份来源由云托管头注入改为 token 解析，其余接口地址与返回结构保持不变。


## CI/CD 自动部署（GitHub Actions）

推送 `master` 后由 GitHub Actions 通过 SSH 登录服务器，自动拉取最新代码并重建 `app` 容器。数据库 `mysql` 走命名卷，数据不受影响。

### 工作流位置
`.github/workflows/deploy.yml`（push 到 `master` 触发，也可在 Actions 页面手动触发）。

### 服务器一次性准备
```bash
# 1) 在服务器生成“部署密钥”，公钥加到 GitHub 仓库 Settings → Deploy keys（只读），
#    用于服务器后续 git pull
ssh-keygen -t ed25519 -f ~/.ssh/github_deploy -N ""
cat ~/.ssh/github_deploy.pub   # 复制内容到 GitHub Deploy keys

# 2) clone 仓库到部署目录（与 deploy.yml 的 DEPLOY_PATH 保持一致）
git clone git@github.com:zhangzaichuangshangderen/EatWhatService.git /opt/EatWhatService
cd /opt/EatWhatService

# 3) 准备本地 .env（含数据库密码、WX_*/JWT_*，切勿提交）
cp .env.example .env
vi .env     # 填好 MYSQL_PASSWORD / MYSQL_ROOT_PASSWORD / WX_SECRET / JWT_SECRET 等

# 4) 首次启动
docker compose up -d
```

### GitHub 仓库 Secrets（Settings → Secrets and variables → Actions）
| Secret | 说明 |
| --- | --- |
| `SERVER_HOST` | 服务器公网 IP（如 `146.56.250.103`） |
| `SERVER_USERNAME` | SSH 登录用户名（如 `root`） |
| `SERVER_SSH_KEY` | **你本地机器的 SSH 私钥内容**（用于 Actions runner 登录服务器；对应公钥需加入服务器 `~/.ssh/authorized_keys`） |

> 注意区分两把密钥：
> - `SERVER_SSH_KEY`：GitHub Actions **连服务器**用（私钥在本地，公钥在服务器 `authorized_keys`）。
> - 服务器上的 `github_deploy` 密钥：服务器 **git pull GitHub** 用（公钥在 GitHub Deploy keys）。

### 验证
推送后到仓库 **Actions** 页看运行日志；或直接：
```bash
ssh root@146.56.250.103 \
  'cd /opt/EatWhatService && docker compose ps && docker compose logs --tail=20 app'
```

## License

[MIT](./LICENSE)
