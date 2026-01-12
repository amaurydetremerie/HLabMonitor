package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetEntityRepository extends JpaRepository<TargetEntity, Long> {
    TargetEntity findByTargetId(String id);

    boolean existsByTargetId(String targetId);
}
