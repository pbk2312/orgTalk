// resources/mongo-init.js

// 1. 컬렉션 생성 및 스키마 검증
db.createCollection("chat_messages", {
  capped: false,
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["roomId", "senderName", "senderId", "message", "type",
        "language", "createdAt"],
      properties: {
        roomId: {bsonType: "long"},
        senderName: {bsonType: "string"},
        senderId: {bsonType: "long"},
        type: {enum: ["TEXT", "CODE", "IMAGE", "SYSTEM"]},
        message: {bsonType: "string"},
        codeContent: {bsonType: ["string", "null"]},
        language: {enum: ["JAVA", "JS", "PYTHON", "NONE"]},
        createdAt: {bsonType: "date"},
        updatedAt: {bsonType: "date"}
      }
    }
  },
  validationLevel: "strict"
});

// 2. 인덱스 생성
// 방별 최신 메시지 조회 최적화
db.chat_messages.createIndex(
    {roomId: 1, createdAt: -1},
    {name: "idx_chatmsg_room_created"}
);

// 방+사용자별 메시지 조회 최적화
db.chat_messages.createIndex(
    {roomId: 1, senderId: 1, createdAt: -1},
    {name: "idx_chatmsg_room_sender"}
);

// 전문 텍스트 검색용 (message + codeContent)
db.chat_messages.createIndex(
    {message: "text", codeContent: "text"},
    {
      name: "idx_chatmsg_text_search",
      default_language: "none",
      language_override: "language"
    }
);

// 3. TTL 인덱스 (예: 30일 후 자동 삭제)
// db.chat_messages.createIndex(
//   { createdAt: 1 },
//   { expireAfterSeconds: 60*60*24*30, name: "ttl_chatmsg_30days" }
// );
