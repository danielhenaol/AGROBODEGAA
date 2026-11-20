package com.agrobodega.infrastructure.repository;

import com.agrobodega.domain.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByEventCodeAndChannelAndLocaleAndActiveTrue(
            String eventCode, String channel, String locale);
}
