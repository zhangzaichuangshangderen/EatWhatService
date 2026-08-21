# Fitfit Service

fitfit 认真吃饭的 Java 后端服务，提供小程序登录、食材、饮食记录、营养目标、好友和邀请等 API。

## 目录结构说明
~~~
.
├── Dockerfile                      Dockerfile 文件
├── LICENSE                         LICENSE 文件
├── README.md                       README 文件
├── docker-compose.yml              Java 应用编排
├── docker-compose.mysql.yml        MySQL 独立编排
├── pom.xml                         pom.xml文件
├── settings.xml                    maven 配置文件
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

## 运行配置

启动服务前需配置以下环境变量：
- MYSQL_ADDRESS
- MYSQL_PASSWORD
- MYSQL_USERNAME
- MYSQL_DATABASE（可选，默认 `eatwhat`）
- INVITE_CLEANUP_ADMIN_OPENIDS（可选，邀请脏数据清洗接口白名单，逗号分隔 openid）
具体配置项参考 `.env.example`，实际密码和密钥不要提交到仓库。

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

部署拓扑为 `公网 80/443 -> 宿主机 Caddy -> 127.0.0.1:8080 -> app -> MySQL`。app 与
MySQL 分别由 `docker-compose.yml`、`docker-compose.mysql.yml` 管理，通过共享网络
`eatwhat-backend` 通信；Caddy 作为独立 Docker 容器管理 HTTPS 和证书，不参与 app 构建。

1. 确认 `fitfit.cn` 和 `www.fitfit.cn` 的 A 记录指向服务器，并在云安全组放行 TCP 80/443。
   已有数据库的服务器在首次拆分前先执行 `docker volume inspect eatwhatservice_mysql_data`；如果找不到，
   用 `docker volume ls` 查出原卷名，并同步修改 `docker-compose.mysql.yml` 中的 `volumes.mysql_data.name`，
   不要在未确认数据卷时初始化生产数据库。
2. 准备环境变量并启动服务：

   ```bash
   cd /root/code/EatWhatService
   cp .env.example .env
   vi .env
   docker compose -f docker-compose.mysql.yml config -q
   docker compose -f docker-compose.mysql.yml up -d --wait
   docker compose config -q
   docker compose up -d --build app
   curl -I http://127.0.0.1:8080/
   ```

   MySQL 编排会创建共享网络，并沿用拆分前的 `eatwhatservice_mysql_data` 数据卷。app 只绑定
   宿主机 `127.0.0.1:8080`，公网不能直接访问 8080。
3. 将宿主机 `/root/caddy/Caddyfile` 设置为：

   ```caddyfile
   fitfit.cn, www.fitfit.cn {
       reverse_proxy 127.0.0.1:8080
   }
   ```

4. 首次启动独立 Caddy 容器：

   ```bash
   docker volume create eatwhat-caddy-data
   docker volume create eatwhat-caddy-config
   docker run -d --name eatwhat-caddy --restart always --network host \
     -v /root/caddy/Caddyfile:/etc/caddy/Caddyfile:ro \
     -v eatwhat-caddy-data:/data \
     -v eatwhat-caddy-config:/config \
     caddy:2.11.4-alpine
   ```

   Caddy 使用 host network 访问宿主机 `127.0.0.1:8080`；证书保存在独立数据卷中，容器更新不会丢失。
5. 校验并验证：

   ```bash
   docker exec eatwhat-caddy caddy validate --config /etc/caddy/Caddyfile
   docker ps --filter name=eatwhat-caddy
   docker logs --tail=50 eatwhat-caddy
   curl -I http://fitfit.cn
   curl -I https://fitfit.cn
   curl -I https://www.fitfit.cn
   ```

   Caddy 会自动申请并续期公开可信证书，同时把 HTTP 请求重定向到 HTTPS。若证书签发失败，检查
   DNS、服务器时间、80/443 端口和云安全组。最后在微信公众平台将 `https://fitfit.cn` 加入
   `request 合法域名`。

两套 Compose 可以独立运维：

```bash
# 仅查看/更新 MySQL
docker compose -f docker-compose.mysql.yml ps
docker compose -f docker-compose.mysql.yml logs -f mysql
docker compose -f docker-compose.mysql.yml up -d --wait

# 仅构建/更新 Java app
docker compose build app
docker compose up -d app
```

不要对 MySQL 编排执行 `down -v`，否则会删除数据库数据卷。

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

推送 `master` 后由 GitHub Actions 通过 SSH 登录服务器，只重新构建和更新 app。独立 Caddy 容器
和 MySQL 编排都不会参与 app 的构建；数据库使用独立 Compose 和命名卷。

### 工作流位置
`.github/workflows/deploy.yml`（push 到 `master` 触发，也可在 Actions 页面手动触发）。

### 服务器一次性准备
```bash
# 1) 在服务器生成“部署密钥”，公钥加到 GitHub 仓库 Settings → Deploy keys（只读），
#    用于服务器后续 git pull
ssh-keygen -t ed25519 -f ~/.ssh/github_deploy -N ""
cat ~/.ssh/github_deploy.pub   # 复制内容到 GitHub Deploy keys

# 2) clone 仓库到部署目录（与 deploy.yml 的 DEPLOY_PATH 保持一致）
git clone git@github.com:zhangzaichuangshangderen/EatWhatService.git /root/code/EatWhatService
cd /root/code/EatWhatService

# 3) 准备本地 .env（含数据库密码、WX_*/JWT_*，切勿提交）
cp .env.example .env
vi .env

# 4) 先启动独立 MySQL，再启动 app；按上文在宿主机配置 Caddy HTTPS
docker compose -f docker-compose.mysql.yml config -q
docker compose -f docker-compose.mysql.yml up -d --wait
docker compose config -q
docker compose up -d --build app
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
  'cd /root/code/EatWhatService && docker compose -f docker-compose.mysql.yml ps && docker compose ps && docker compose logs --tail=20 app && docker ps --filter name=eatwhat-caddy'
```

## License

[MIT](./LICENSE)
