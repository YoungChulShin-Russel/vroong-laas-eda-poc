// MongoDB 초기화 스크립트
// projection 데이터베이스와 컬렉션 설정

// projection 데이터베이스로 전환
db = db.getSiblingDB('projection');

// order_projections 컬렉션 생성 및 인덱스 설정
db.createCollection('order_projections');

// 인덱스 생성
db.order_projections.createIndex({ "orderId": 1 }, { unique: true });
db.order_projections.createIndex({ "agentId": 1 });
db.order_projections.createIndex({ "deliveryStatus": 1 });
db.order_projections.createIndex({ "orderedAt": 1 });
db.order_projections.createIndex({ "createdAt": 1 });
db.order_projections.createIndex({ "updatedAt": 1 });

// 복합 인덱스
db.order_projections.createIndex({ "agentId": 1, "deliveryStatus": 1 });
db.order_projections.createIndex({ "orderedAt": 1, "deliveryStatus": 1 });

print('MongoDB initialized successfully for projection service');
print('Database: projection');
print('Collection: order_projections');
print('Indexes created for: orderId(unique), agentId, deliveryStatus, orderedAt, createdAt, updatedAt');