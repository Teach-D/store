package com.msa.order.domain.fail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class FailedTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskType;

    @Column(columnDefinition = "TEXT")
    private String taskData;
    private LocalDateTime failedAt;
    private Integer retryCount = 0;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    private String errorMessage;
    private LocalDateTime lastRetryAt;

    public FailedTask(String clearCart, String taskData, String message) {
        this.taskType = clearCart;
        this.taskData = taskData;
        this.failedAt = LocalDateTime.now();
        this.errorMessage = message;
    }

    public void markAsPending() {
        this.status = TaskStatus.PENDING;
    }

    public enum TaskStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED
    }

    public FailedTask(String taskType, String taskData, LocalDateTime failedAt, Integer retryCount, TaskStatus status, String errorMessage, LocalDateTime lastRetryAt) {
        this.taskType = taskType;
        this.taskData = taskData;
        this.errorMessage = errorMessage;
        this.failedAt = failedAt;
        this.retryCount = 0;
        this.status = status;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();

        if (retryCount >= 5) {
            this.status = TaskStatus.FAILED;
        }
    }

    public void markAsProcessing() {
        this.status = TaskStatus.PROCESSING;
    }

    public void markAsSuccess() {
        this.status = TaskStatus.SUCCESS;
    }

    public void markAsFailed() {
        this.status = TaskStatus.FAILED;
    }
}
