package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultEntityRepository extends JpaRepository<ResultEntity, Long>, JpaSpecificationExecutor<ResultEntity> {
    List<ResultEntity> findAllByTargetId(String id);
}
