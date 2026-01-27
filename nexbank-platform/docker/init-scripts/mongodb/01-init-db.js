// ============================================
//       INICIALIZAR BASE DE DATOS MONGODB
// ============================================

db = db.getSiblingDB('nexbank_notifications');

// Crear colección para logs de notificaciones
db.createCollection('notification_logs');

// Crear índices
db.notification_logs.createIndex({ "createdAt": 1 }, { expireAfterSeconds: 2592000 }); // 30 días
db.notification_logs.createIndex({ "userId": 1 });
db.notification_logs.createIndex({ "type": 1 });

print('MongoDB inicializado correctamente');