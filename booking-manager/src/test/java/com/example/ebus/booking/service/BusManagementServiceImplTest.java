package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BusDao;
import com.example.ebus.booking.dto.CreateBusRequest;
import com.example.ebus.booking.entity.BusEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusManagementServiceImplTest {

    @Mock
    private BusDao busDao;

    @InjectMocks
    private BusManagementServiceImpl busManagementService;

    @Test
    void createBus_Success() {
        CreateBusRequest request = new CreateBusRequest("ABC-1234", "Test Bus Co", 40);
        BusEntity bus = new BusEntity();
        bus.setId(1L);
        bus.setPlateNumber("ABC-1234");

        when(busDao.save(any(BusEntity.class))).thenReturn(bus);

        BusEntity result = busManagementService.createBus(request);

        assertThat(result).isNotNull();
        assertThat(result.getPlateNumber()).isEqualTo("ABC-1234");
        verify(busDao).save(any(BusEntity.class));
    }
}
