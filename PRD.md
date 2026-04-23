# 猫咪伴侣 App PRD 需求文档

## 一、项目概述

### 1.1 项目名称
猫咪伴侣（Cat Together）

### 1.2 项目定位
一款面向养猫人群体的智能猫咪管理应用，集成AI识别、健康记录、饮食管理、社交互动等功能，帮助用户更好地照顾自己的猫咪。

### 1.3 目标用户
- 年龄：18-45岁
- 养有猫咪的宠物爱好者
- 注重猫咪健康管理的人群
- 喜欢社交分享的铲屎官

### 1.4 产品愿景
成为每一位铲屎官最贴心的猫咪管理助手，让养猫更科学、更有趣。

---

## 二、UI/UX设计规范

### 2.1 设计风格
- **整体风格**：简约清新
- **色调**：
  - 主色：#FFB7B2（温柔粉色）
  - 辅助色：#B5EAD7（薄荷绿）
  - 强调色：#FFDAC1（蜜桃橘）
  - 背景色：#FAF9F6（米白色）
- **字体**：圆润无衬线字体，亲和力强
- **图标风格**：线性+圆润填充，可爱但不幼稚
- **避免**：土气配色、过度装饰、复杂布局

### 2.2 交互原则
- 操作流程简单直观
- 重要功能一键可达
- 反馈及时且温和
- 支持单手操作

---

## 三、核心功能模块

### 3.1 用户体系

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 注册登录 | 手机号+验证码/第三方登录 | P0 |
| 个人信息 | 昵称、头像、性别、地区 | P0 |
| 实名认证 | 用于兑换实物（可选） | P2 |
| 会员体系 | 普通用户/会员，区分付费功能 | P1 |

---

### 3.2 猫咪档案

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 添加猫咪 | 基本信息录入 | P0 |
| 基本信息展示 | 品种、性别、生日、毛色等 | P0 |
| 健康数据记录 | 身高、体重、年龄 | P0 |
| 健康数据图表 | 体重变化趋势、健康评分 | P1 |
| 疫苗/驱虫记录 | 记录和提醒功能 | P1 |
| **AI减肥计划** | 基于健康数据制定减肥方案（付费） | P2 |

#### 数据结构
```
CatProfile {
  id: String
  userId: String
  name: String
  breed: String
  gender: Enum
  birthday: Date
  color: String
  avatar: String (OSS URL)
  weight: Float
  height: Float
  healthRecords: HealthRecord[]
}
```

---

### 3.3 AI识别猫咪说话

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 录音识别 | 长按录音识别猫咪叫声 | P0 |
| 语音转文字 | 将叫声转换为"猫咪语言" | P0 |
| 情绪分析 | 分析猫咪当前情绪 | P1 |
| 历史记录 | 保存识别历史 | P1 |
| AI解读 | 解读叫声含义 | P0 |

#### 实现方案
- 调用智谱AI语音识别API
- 自定义提示词进行猫咪叫声"翻译"

---

### 3.4 饮食记录与管理

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 喂食记录 | 猫粮、水、零食、猫条次数 | P0 |
| 快捷记录 | 一键记录常用喂养项 | P0 |
| 喂食统计 | 每日/每周/每月统计 | P0 |
| 喂食提醒 | 自定义提醒时间和内容 | P0 |
| 饮食图表 | 可视化展示饮食情况 | P1 |
| **AI饮食方案** | 基于猫咪数据制定饮食计划（付费） | P2 |

#### 数据结构
```
DietRecord {
  id: String
  catId: String
  type: Enum (water/food/snack/treat)
  amount: Int
  timestamp: Date
  note: String
}
```

---

### 3.5 猫猫相册

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 上传照片/视频 | 支持单选、多选 | P0 |
| 相册分类 | 按时间、标签分类 | P1 |
| 智能分类 | AI识别猫咪表情自动分类 | P2 |
| 美化编辑 | 基础滤镜、贴纸 | P1 |
| 分享到社交圈 | 一键分享 | P0 |
| 云端同步 | 自动备份到阿里云OSS | P0 |

---

### 3.6 猫猫社交圈

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 发布动态 | 图文+视频分享 | P0 |
| 动态浏览 | 瀑布流展示 | P0 |
| 点赞评论 | 基础互动功能 | P0 |
| 关注/粉丝 | 社交关系 | P1 |
| 话题标签 | #我的猫猫日记 等 | P1 |
| 热门推荐 | 精选优质内容 | P1 |
| 举报审核 | 内容审核机制 | P0 |

---

### 3.7 猫猫管家（AI客服）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 智能问答 | 基于智谱AI的养猫知识问答 | P0 |
| 场景对话 | 不同场景下智能建议 | P1 |
| 语音交互 | 支持语音输入 | P1 |
| 常见问题 | FAQ快速访问 | P0 |
| 历史对话 | 保存对话记录 | P1 |
| 人工客服 | 转接人工客服（可选） | P2 |

---

### 3.8 积分与商城系统

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 每日签到 | 连续签到奖励 | P2 |
| 活动任务 | 完成任务获得猫条 | P2 |
| 猫条（代币） | 通过活动获得 | P2 |
| 鱼干（充值） | 现金兑换 | P2 |
| 积分商城 | 兑换实物猫用品 | P2 |
| 兑换订单 | 订单管理与物流追踪 | P2 |

#### 积分规则
- 签到：每日+10猫条
- 发布动态：+20猫条
- 获得点赞：+5猫条/次
- 兑换比例：100猫条 ≈ 1元价值

---

## 四、技术架构

### 4.1 技术选型

| 层级 | 技术 |
|------|------|
| 移动端 | Android (Kotlin/Java) |
| 后端 | Node.js + Express |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis |
| 对象存储 | 阿里云OSS |
| AI服务 | 智谱AI API |
| 推送 | 阿里云推送/极光推送 |

### 4.2 后端架构

```
┌─────────────────────────────────┐
│         客户端 (Android)         │
└─────────────┬───────────────────┘
              │ HTTPS
              ▼
┌─────────────────────────────────┐
│         API Gateway              │
│   (认证、限流、日志)              │
└─────────────┬───────────────────┘
              │
    ┌─────────┼─────────┐
    ▼         ▼         ▼
┌──────┐ ┌──────┐ ┌──────┐
│用户  │ │猫咪  │ │内容  │
│服务  │ │服务  │ │服务  │
└──────┘ └──────┘ └──────┘
    │         │         │
    └─────────┼─────────┘
              ▼
       ┌──────────────┐
       │   MySQL      │
       │   Redis      │
       └──────────────┘
```

### 4.3 数据库表设计

```sql
-- 用户表
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  phone VARCHAR(20) UNIQUE,
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  gender TINYINT,
  region VARCHAR(100),
  member_level TINYINT DEFAULT 0,
  member_expire_time DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 猫咪表
CREATE TABLE cats (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  name VARCHAR(50),
  breed VARCHAR(50),
  gender TINYINT,
  birthday DATE,
  color VARCHAR(50),
  avatar VARCHAR(255),
  weight DECIMAL(5,2),
  height DECIMAL(5,2),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 健康记录表
CREATE TABLE health_records (
  id VARCHAR(36) PRIMARY KEY,
  cat_id VARCHAR(36),
  record_type TINYINT,
  value DECIMAL(10,2),
  record_date DATE,
  note TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (cat_id) REFERENCES cats(id) ON DELETE CASCADE
);

-- 饮食记录表
CREATE TABLE diet_records (
  id VARCHAR(36) PRIMARY KEY,
  cat_id VARCHAR(36),
  type TINYINT COMMENT '1:水 2:猫粮 3:零食 4:猫条',
  amount INT,
  timestamp DATETIME,
  note VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (cat_id) REFERENCES cats(id) ON DELETE CASCADE
);

-- 喂食提醒表
CREATE TABLE feeding_reminders (
  id VARCHAR(36) PRIMARY KEY,
  cat_id VARCHAR(36),
  type TINYINT,
  time TIME,
  repeat_rule VARCHAR(50),
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (cat_id) REFERENCES cats(id) ON DELETE CASCADE
);

-- 媒体表（相册）
CREATE TABLE media (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  cat_id VARCHAR(36),
  type TINYINT COMMENT '1:图片 2:视频',
  url VARCHAR(500),
  thumb_url VARCHAR(500),
  tags VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (cat_id) REFERENCES cats(id) ON DELETE SET NULL
);

-- 社交动态表
CREATE TABLE posts (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  cat_id VARCHAR(36),
  content TEXT,
  media_ids JSON,
  hashtags JSON,
  like_count INT DEFAULT 0,
  comment_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (cat_id) REFERENCES cats(id) ON DELETE SET NULL
);

-- 点赞表
CREATE TABLE likes (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  post_id VARCHAR(36),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_post (user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- 评论表
CREATE TABLE comments (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  post_id VARCHAR(36),
  parent_id VARCHAR(36),
  content VARCHAR(1000),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- 关注表
CREATE TABLE follows (
  id VARCHAR(36) PRIMARY KEY,
  follower_id VARCHAR(36),
  following_id VARCHAR(36),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_follow (follower_id, following_id),
  FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 积分记录表
CREATE TABLE points (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  type TINYINT COMMENT '1:猫条 2:鱼干',
  amount INT,
  reason VARCHAR(100),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 商品表
CREATE TABLE products (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(100),
  description TEXT,
  image_url VARCHAR(500),
  cat_points INT,
  fish_points INT,
  stock INT,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE orders (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  product_id VARCHAR(36),
  cat_points INT,
  fish_points INT,
  address JSON,
  status TINYINT COMMENT '1:待发货 2:已发货 3:已完成 4:已取消',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- AI对话记录表
CREATE TABLE ai_conversations (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  messages JSON,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 签到记录表
CREATE TABLE check_ins (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36),
  check_in_date DATE,
  points INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_date (user_id, check_in_date),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 五、API接口设计

### 5.1 认证相关
```
POST /api/auth/send-code      发送验证码
POST /api/auth/login          登录
POST /api/auth/register       注册
POST /api/auth/logout         登出
POST /api/auth/refresh        刷新token
```

### 5.2 用户相关
```
GET  /api/user/profile        获取个人信息
PUT  /api/user/profile        更新个人信息
POST /api/user/avatar         上传头像
```

### 5.3 猫咪相关
```
POST /api/cats                添加猫咪
GET  /api/cats                获取猫咪列表
GET  /api/cats/:id            获取猫咪详情
PUT  /api/cats/:id            更新猫咪信息
DELETE /api/cats/:id          删除猫咪
POST /api/cats/:id/health     添加健康记录
GET  /api/cats/:id/health     获取健康记录
POST /api/cats/:id/weight     更新体重
GET  /api/cats/:id/chart      获取健康图表数据
POST /api/cats/:id/diet-plan  获取AI减肥计划(付费)
```

### 5.4 饮食相关
```
POST /api/diet/record         记录喂食
GET  /api/diet/records/:catId 获取饮食记录
POST /api/diet/reminder       设置喂食提醒
GET  /api/diet/reminders      获取提醒列表
PUT  /api/diet/reminder/:id   更新提醒
DELETE /api/diet/reminder/:id 删除提醒
GET  /api/diet/stats/:catId   获取饮食统计
POST /api/diet/ai-plan        获取AI饮食方案(付费)
```

### 5.5 AI识别相关
```
POST /api/ai/cat-speak        AI识别猫咪说话
POST /api/ai/upload-audio     上传音频
```

### 5.6 相册相关
```
POST /api/media/upload        上传媒体
GET  /api/media/list          获取相册列表
DELETE /api/media/:id          删除媒体
PUT  /api/media/:id/tags      更新标签
```

### 5.7 社交相关
```
POST /api/posts               发布动态
GET  /api/posts               获取动态列表
GET  /api/posts/:id           获取动态详情
DELETE /api/posts/:id          删除动态
POST /api/posts/:id/like      点赞
DELETE /api/posts/:id/like    取消点赞
POST /api/posts/:id/comments  评论
GET  /api/posts/:id/comments  获取评论列表
POST /api/follow/:userId      关注用户
DELETE /api/follow/:userId    取消关注
GET  /api/user/following      获取关注列表
GET  /api/user/followers      获取粉丝列表
```

### 5.8 AI管家相关
```
POST /api/ai/chat             发送消息
GET  /api/ai/history          获取对话历史
DELETE /api/ai/history        清空历史
GET  /api/ai/faq              获取常见问题
```

### 5.9 积分商城相关
```
POST /api/check-in            每日签到
GET  /api/points/balance      获取积分余额
GET  /api/points/history      获取积分记录
GET  /api/products            获取商品列表
GET  /api/products/:id        获取商品详情
POST /api/orders              创建订单
GET  /api/orders              获取订单列表
GET  /api/orders/:id          获取订单详情
POST /api/recharge            充值鱼干
```

---

## 六、AI集成方案

### 6.1 智谱AI能力应用

| 功能 | API | 说明 |
|------|-----|------|
| 猫咪说话识别 | GLM-4-Audio | 语音识别+语义理解 |
| 减肥计划 | GLM-4 | 基于健康数据生成方案 |
| 饮食方案 | GLM-4 | 个性化饮食建议 |
| AI客服 | GLM-4 | 养猫知识问答 |

### 6.2 Prompt设计示例

#### 猫咪说话识别Prompt
```
你是一个专业的猫咪语言翻译官。请根据用户提供的猫咪叫声音频，翻译成人类能理解的"猫咪语言"，并分析猫咪的情绪状态。

输出格式：
- 翻译内容：xxx
- 情绪状态：xxx（开心/饥饿/撒娇/生气/害怕/求关注）
- 可能需求：xxx
```

#### AI减肥计划Prompt
```
你是一位专业的猫咪营养师。请根据以下猫咪信息，制定一份科学合理的减肥计划：

猫咪信息：
- 品种：{breed}
- 年龄：{age}岁
- 当前体重：{weight}kg
- 目标体重：{targetWeight}kg
- 活动量：{activityLevel}
- 健康状况：{healthCondition}

请输出详细的减肥计划，包括：
1. 减肥周期和阶段性目标
2. 每日喂食量和喂食次数
3. 推荐食物类型
4. 运动建议
5. 注意事项
```

---

## 七、付费功能设计

### 7.1 会员体系

| 等级 | 价格 | 权益 |
|------|------|------|
| 普通用户 | 免费 | 基础功能 |
| 月度会员 | ¥19/月 | AI减肥计划、AI饮食方案、去广告 |
| 年度会员 | ¥199/年 | 全部会员权益 + 专属头像框 + 优先客服 |

### 7.2 单次购买
- AI减肥计划：¥9.9/次
- AI饮食方案：¥9.9/次

---

## 八、项目里程碑

### 第一阶段：MVP版本（4周）
- 用户注册登录
- 猫咪档案管理
- 健康数据记录
- 饮食记录
- 喂食提醒
- 基础相册功能

### 第二阶段：核心功能（3周）
- AI识别猫咪说话
- 猫猫社交圈
- AI客服基础版
- 数据统计图表

### 第三阶段：增值功能（3周）
- AI减肥计划
- AI饮食方案
- 积分签到系统
- 商城功能

### 第四阶段：优化上线（2周）
- 性能优化
- Bug修复
- 上线准备

---

## 九、非功能性需求

### 9.1 性能要求
- API响应时间 < 500ms
- 图片加载时间 < 2s
- 支持并发用户数 > 1000

### 9.2 安全要求
- 用户数据加密存储
- HTTPS传输
- 敏感信息脱敏
- 接口鉴权

### 9.3 兼容性要求
- Android 8.0+
- 主流屏幕分辨率适配

---

## 十、运营指标

| 指标 | 目标 |
|------|------|
| 日活用户(DAU) | 1000+ |
| 月活用户(MAU) | 5000+ |
| 用户留存率 | 30%+ |
| 付费转化率 | 5%+ |

---

*文档版本：v1.0*
*更新日期：2026-04-17*
