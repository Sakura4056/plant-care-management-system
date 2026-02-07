-- =================================================================
-- 植物护理管理系统的SQLite模式
-- 数据库文件：./data/user_plant_local.db
-- =================================================================

-- ----------------------------
-- local_plant表结构
-- ----------------------------
CREATE TABLE IF NOT EXISTS local_plant (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           user_id INTEGER NOT NULL,
                                           name TEXT NOT NULL,
                                           genus TEXT,
                                           species TEXT,
                                           description TEXT,
                                           audit_status TEXT DEFAULT 'UNSUBMITTED', -- UNSUBMITTED, PENDING, APPROVED, REJECTED, 已注销
                                           create_time TEXT
);

-- 用于查询用户植物的索引
CREATE INDEX IF NOT EXISTS idx_user_id ON local_plant(user_id);
