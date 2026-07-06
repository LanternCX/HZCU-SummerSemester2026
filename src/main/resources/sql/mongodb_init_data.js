const hzcuDb = db.getSiblingDB("hzcu_mongo");

const actionTypes = ["VIEW", "SEARCH", "CREATE_ORDER", "APPROVE_ORDER", "CANCEL_ORDER"];
const clientTypes = ["DESKTOP", "WEB", "MOBILE"];
const logTypes = ["LOGIN", "LOGIN_FAILED", "ORDER_APPROVE", "ITEM_UPDATE", "SYSTEM_ERROR"];
const logLevels = ["INFO", "WARN", "ERROR"];
const tags = [["查询", "完整"], ["操作", "稳定"], ["资料", "待确认"], ["流程", "及时"]];

const itemDetails = [];
for (let i = 1; i <= 20; i += 1) {
  itemDetails.push({
    item_id: String(i),
    description: `详细描述 ${i}`,
    images: [`image-${String(i).padStart(3, "0")}.png`],
    metadata: {
      language: "zh-CN",
      source: "mock"
    },
    updated_at: new Date(Date.UTC(2026, 6, 1 + (i % 5), 8, i, 0))
  });
}
hzcuDb.item_details.insertMany(itemDetails);

const comments = [];
for (let i = 1; i <= 20; i += 1) {
  comments.push({
    user_id: String(((i - 1) % 10) + 1),
    item_id: String(i),
    content: `测试评论 ${i}`,
    rating: (i % 5) + 1,
    tags: tags[i % tags.length],
    created_at: new Date(Date.UTC(2026, 6, 3, 9, i, 0))
  });
}
hzcuDb.comments.insertMany(comments);

const actionLogs = [];
for (let i = 1; i <= 100; i += 1) {
  actionLogs.push({
    user_id: String(((i - 1) % 10) + 1),
    item_id: String(((i - 1) % 20) + 1),
    action_type: actionTypes[(i - 1) % actionTypes.length],
    duration_seconds: 30 + (i % 12) * 10,
    client_info: {
      client_type: clientTypes[(i - 1) % clientTypes.length],
      ip: `192.0.2.${(i % 50) + 1}`
    },
    created_at: new Date(Date.UTC(2026, 6, 4 + (i % 5), i % 24, i % 60, 0))
  });
}
hzcuDb.action_logs.insertMany(actionLogs);

const systemLogs = [];
for (let i = 1; i <= 100; i += 1) {
  const logType = logTypes[(i - 1) % logTypes.length];
  systemLogs.push({
    user_id: String(((i - 1) % 10) + 1),
    log_type: logType,
    log_level: logType === "SYSTEM_ERROR" ? "ERROR" : logLevels[(i - 1) % 2],
    message: `测试系统日志 ${i}`,
    action_detail: {
      ip: `198.51.100.${(i % 50) + 1}`,
      operation: `${logType} mock operation`
    },
    timestamp: new Date(Date.UTC(2026, 6, 4 + (i % 5), i % 24, i % 60, 0))
  });
}
hzcuDb.system_logs.insertMany(systemLogs);
