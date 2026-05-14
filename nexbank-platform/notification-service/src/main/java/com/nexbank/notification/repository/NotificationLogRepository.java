package com.nexbank.notification.repository;

import com.nexbank.notification.document.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<NotificationLog> findByTypeOrderByCreatedAtDesc(String type);

    List<NotificationLog> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
}
