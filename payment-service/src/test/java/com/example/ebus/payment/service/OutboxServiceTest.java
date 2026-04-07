package com.example.ebus.payment.service;

import com.example.ebus.payment.dao.OutboxEventDao;
import com.example.ebus.payment.entity.OutboxEventEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventDao outboxEventDao;

    @InjectMocks
    private OutboxService outboxService;

    @Test
    void saveEvent_Success() {
        outboxService.saveEvent("Payment", "1", "payment.completed", "{\"id\":1}");

        verify(outboxEventDao).save(any(OutboxEventEntity.class));
    }
}
