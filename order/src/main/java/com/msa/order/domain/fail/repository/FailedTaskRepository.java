package com.msa.order.domain.fail.repository;

import com.msa.order.domain.fail.entity.FailedTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedTaskRepository extends JpaRepository<FailedTask, Long> {

    List<FailedTask> findByStatusAndRetryCountLessThan(FailedTask.TaskStatus status, Integer maxRetry);

}
