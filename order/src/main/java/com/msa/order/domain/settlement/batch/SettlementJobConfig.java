package com.msa.order.domain.settlement.batch;

import com.msa.order.domain.order.entity.Order;
import com.msa.order.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository,
                                  Step settlementStep,
                                  SettlementJobListener jobListener) {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .listener(jobListener)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemReader<Long> sellerIdReader,
                               SettlementItemProcessor processor,
                               SettlementItemWriter writer,
                               SettlementSkipListener skipListener,
                               SettlementStepListener stepListener) {
        return new StepBuilder("settlementStep", jobRepository)
                .<Long, SettlementData>chunk(10, transactionManager)
                .reader(sellerIdReader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(DataIntegrityViolationException.class)
                .skip(IllegalArgumentException.class)
                .skipLimit(10)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(skipListener)
                .listener(stepListener)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Long> sellerIdReader(
            @Value("#{jobParameters['date']}") String date,
            OrderRepository orderRepository) {
        String dateStr = LocalDate.parse(date).format(DATE_FORMATTER);
        List<Long> sellerIds = orderRepository.findDistinctSellerIdsByStatusAndDate(
                Order.OrderStatus.CONFIRMED, dateStr);
        return new ListItemReader<>(sellerIds);
    }
}
