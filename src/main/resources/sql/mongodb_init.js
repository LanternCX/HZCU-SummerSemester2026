const hzcuDb = db.getSiblingDB("hzcu_mongo");

hzcuDb.createCollection("action_logs");
hzcuDb.createCollection("comments");
hzcuDb.createCollection("item_details");
hzcuDb.createCollection("system_logs");

hzcuDb.action_logs.createIndex({ user_id: 1, created_at: -1 }, { name: "idx_action_logs_user_created_at" });
hzcuDb.action_logs.createIndex({ item_id: 1, created_at: -1 }, { name: "idx_action_logs_item_created_at" });
hzcuDb.action_logs.createIndex({ action_type: 1, created_at: -1 }, { name: "idx_action_logs_type_created_at" });

hzcuDb.comments.createIndex({ item_id: 1, created_at: -1 }, { name: "idx_comments_item_created_at" });
hzcuDb.comments.createIndex({ user_id: 1, created_at: -1 }, { name: "idx_comments_user_created_at" });
hzcuDb.comments.createIndex({ rating: 1 }, { name: "idx_comments_rating" });

hzcuDb.item_details.createIndex({ item_id: 1 }, { name: "uk_item_details_item_id", unique: true });
hzcuDb.item_details.createIndex({ "metadata.language": 1 }, { name: "idx_item_details_language" });
hzcuDb.item_details.createIndex({ updated_at: -1 }, { name: "idx_item_details_updated_at" });

hzcuDb.system_logs.createIndex({ user_id: 1, timestamp: -1 }, { name: "idx_system_logs_user_timestamp" });
hzcuDb.system_logs.createIndex({ log_type: 1, timestamp: -1 }, { name: "idx_system_logs_type_timestamp" });
hzcuDb.system_logs.createIndex({ log_level: 1, timestamp: -1 }, { name: "idx_system_logs_level_timestamp" });

hzcuDb.createView("item_detail_view", "item_details", [
  { $project: { _id: 0, item_id: 1, description: 1, images: 1, metadata: 1, updated_at: 1 } }
]);

hzcuDb.createView("item_comment_list", "comments", [
  { $sort: { created_at: -1 } },
  { $project: { _id: 0, user_id: 1, item_id: 1, content: 1, rating: 1, tags: 1, created_at: 1 } }
]);

hzcuDb.createView("agg_user_activity_report", "action_logs", [
  {
    $group: {
      _id: { user_id: "$user_id", action_type: "$action_type" },
      action_count: { $sum: 1 },
      total_duration_seconds: { $sum: "$duration_seconds" },
      last_action_at: { $max: "$created_at" }
    }
  },
  {
    $project: {
      _id: 0,
      user_id: "$_id.user_id",
      action_type: "$_id.action_type",
      action_count: 1,
      total_duration_seconds: 1,
      last_action_at: 1
    }
  }
]);

hzcuDb.createView("agg_item_attention_report", "action_logs", [
  {
    $group: {
      _id: "$item_id",
      view_count: { $sum: { $cond: [{ $eq: ["$action_type", "VIEW"] }, 1, 0] } },
      action_count: { $sum: 1 }
    }
  },
  {
    $lookup: {
      from: "comments",
      localField: "_id",
      foreignField: "item_id",
      as: "comments"
    }
  },
  {
    $project: {
      _id: 0,
      item_id: "$_id",
      view_count: 1,
      action_count: 1,
      comment_count: { $size: "$comments" },
      average_rating: { $ifNull: [{ $avg: "$comments.rating" }, 0] }
    }
  },
  { $sort: { action_count: -1, item_id: 1 } }
]);

hzcuDb.createView("agg_comment_rating_report", "comments", [
  {
    $group: {
      _id: "$item_id",
      comment_count: { $sum: 1 },
      average_rating: { $avg: "$rating" },
      tags: { $addToSet: "$tags" }
    }
  },
  {
    $project: {
      _id: 0,
      item_id: "$_id",
      comment_count: 1,
      average_rating: 1,
      tags: {
        $reduce: {
          input: "$tags",
          initialValue: [],
          in: { $setUnion: ["$$value", "$$this"] }
        }
      }
    }
  },
  { $sort: { average_rating: -1, item_id: 1 } }
]);

hzcuDb.createView("agg_system_audit_report", "system_logs", [
  {
    $group: {
      _id: {
        log_type: "$log_type",
        log_level: "$log_level",
        day: { $dateToString: { format: "%Y-%m-%d", date: "$timestamp" } }
      },
      log_count: { $sum: 1 },
      last_log_at: { $max: "$timestamp" }
    }
  },
  {
    $project: {
      _id: 0,
      log_type: "$_id.log_type",
      log_level: "$_id.log_level",
      day: "$_id.day",
      log_count: 1,
      last_log_at: 1
    }
  }
]);

hzcuDb.createView("agg_login_risk_report", "system_logs", [
  { $match: { log_type: { $in: ["LOGIN", "LOGIN_FAILED"] } } },
  {
    $group: {
      _id: { user_id: "$user_id", ip: "$action_detail.ip" },
      login_count: { $sum: { $cond: [{ $eq: ["$log_type", "LOGIN"] }, 1, 0] } },
      failed_count: { $sum: { $cond: [{ $eq: ["$log_type", "LOGIN_FAILED"] }, 1, 0] } },
      last_login_at: { $max: "$timestamp" }
    }
  },
  {
    $project: {
      _id: 0,
      user_id: "$_id.user_id",
      ip: "$_id.ip",
      login_count: 1,
      failed_count: 1,
      last_login_at: 1
    }
  },
  { $sort: { failed_count: -1, login_count: -1 } }
]);
