package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.CreateBusRequest;
import com.example.ebus.booking.entity.BusEntity;

public interface BusManagementService {

    BusEntity createBus(CreateBusRequest request);
}
