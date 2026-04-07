package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BusDao;
import com.example.ebus.booking.dto.CreateBusRequest;
import com.example.ebus.booking.entity.BusEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusManagementServiceImpl implements BusManagementService {

    private final BusDao busDao;

    @Override
    @Transactional
    public BusEntity createBus(CreateBusRequest request) {
        BusEntity bus = new BusEntity();
        bus.setPlateNumber(request.plateNumber());
        bus.setOperatorName(request.operatorName());
        bus.setTotalSeats(request.totalSeats());
        return busDao.save(bus);
    }
}
