const hzcuDb = db.getSiblingDB("hzcu_mongo");

[
  "item_detail_view",
  "item_comment_list",
  "agg_user_activity_report",
  "agg_item_attention_report",
  "agg_comment_rating_report",
  "agg_system_audit_report",
  "agg_login_risk_report",
  "action_logs",
  "comments",
  "item_details",
  "system_logs"
].forEach((name) => hzcuDb.getCollection(name).drop());
