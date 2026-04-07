package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BusDao;
import com.example.ebus.booking.dao.RouteDao;
import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.document.SeatLayoutDocument;
import com.example.ebus.booking.dto.*;
import com.example.ebus.booking.entity.BusEntity;
import com.example.ebus.booking.entity.RouteEntity;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import com.example.ebus.booking.repository.SeatLayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripDao tripDao;

    @Mock
    private RouteDao routeDao;

    @Mock
    private BusDao busDao;

    @Mock
    private SeatLayoutRepository seatLayoutRepository;

    @Mock
    private SeatLockService seatLockService;

    @InjectMocks
    private TripService tripService;

    private TripEntity sampleTrip;
    private SeatLayoutDocument sampleLayout;

    @BeforeEach
    void setUp() {
        sampleTrip = new TripEntity();
        sampleTrip.setId(1L);
        sampleTrip.setRouteId(10L);
        sampleTrip.setBusId(20L);
        sampleTrip.setDepartureTime(LocalDateTime.of(2026, 4, 10, 8, 0));
        sampleTrip.setArrivalTime(LocalDateTime.of(2026, 4, 10, 12, 0));
        sampleTrip.setPrice(BigDecimal.valueOf(50));
        sampleTrip.setCurrency("USD");
        sampleTrip.setTotalSeats(40);
        sampleTrip.setOperatorName("Test Bus Co");

        sampleLayout = new SeatLayoutDocument();
        sampleLayout.setId("layout-1");
        sampleLayout.setBusId(20L);
        sampleLayout.setSeatMap(Map.of("1A", "window", "1B", "aisle"));
        sampleLayout.setRows(10);
        sampleLayout.setSeatsPerRow(4);
    }

    @Test
    void findTrips_WithAllParams() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        when(tripDao.findTrips("NYC", "Boston", date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(sampleTrip));

        List<TripResponse> responses = tripService.findTrips("NYC", "Boston", date);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
    }

    @Test
    void findTrips_WithNullDate() {
        when(tripDao.findTrips("NYC", "Boston", null, null))
                .thenReturn(List.of(sampleTrip));

        List<TripResponse> responses = tripService.findTrips("NYC", "Boston", null);

        assertThat(responses).hasSize(1);
        verify(tripDao).findTrips("NYC", "Boston", null, null);
    }

    @Test
    void findTrips_EmptyResult() {
        when(tripDao.findTrips(any(), any(), any(), any())).thenReturn(List.of());

        List<TripResponse> responses = tripService.findTrips("NYC", "Boston", LocalDate.now());

        assertThat(responses).isEmpty();
    }

    @Test
    void getTrip_Success() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));

        TripResponse response = tripService.getTrip(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.operatorName()).isEqualTo("Test Bus Co");
    }

    @Test
    void getTrip_NotFound() {
        when(tripDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getTrip(1L))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getSeatAvailability_WithLayout() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(seatLayoutRepository.findByBusId(20L)).thenReturn(Optional.of(sampleLayout));
        when(seatLockService.isSeatLocked(1L, "1A")).thenReturn(false);
        when(seatLockService.isSeatLocked(1L, "1B")).thenReturn(true);

        SeatAvailabilityResponse response = tripService.getSeatAvailability(1L);

        assertThat(response).isNotNull();
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.rows()).isEqualTo(10);
        assertThat(response.seatsPerRow()).isEqualTo(4);
        assertThat(response.availability()).containsEntry("1A", true);
        assertThat(response.availability()).containsEntry("1B", false);
    }

    @Test
    void getSeatAvailability_WithoutLayout() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(seatLayoutRepository.findByBusId(20L)).thenReturn(Optional.empty());

        SeatAvailabilityResponse response = tripService.getSeatAvailability(1L);

        assertThat(response).isNotNull();
        assertThat(response.rows()).isEqualTo(0);
        assertThat(response.seatMap()).isEmpty();
    }

    @Test
    void getSeatAvailability_TripNotFound() {
        when(tripDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getSeatAvailability(1L))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createRoute_Success() {
        CreateRouteRequest request = new CreateRouteRequest("NYC", "Boston", 350, 240);
        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setOrigin("NYC");
        route.setDestination("Boston");

        when(routeDao.save(any(RouteEntity.class))).thenReturn(route);

        RouteEntity result = tripService.createRoute(request);

        assertThat(result).isNotNull();
        assertThat(result.getOrigin()).isEqualTo("NYC");
        verify(routeDao).save(any(RouteEntity.class));
    }

    @Test
    void createTrip_Success() {
        CreateTripRequest request = new CreateTripRequest(
                10L, 20L, LocalDateTime.of(2026, 4, 10, 8, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0), BigDecimal.valueOf(50),
                "USD", 40, "Test Bus Co");

        when(tripDao.save(any(TripEntity.class))).thenAnswer(invocation -> {
            TripEntity trip = invocation.getArgument(0);
            trip.setId(1L);
            return trip;
        });

        TripResponse response = tripService.createTrip(request);

        assertThat(response).isNotNull();
        assertThat(response.operatorName()).isEqualTo("Test Bus Co");
        verify(seatLockService).initAvailability(1L, 40);
    }

    @Test
    void createBus_Success() {
        CreateBusRequest request = new CreateBusRequest("ABC-1234", "Test Bus Co", 40);
        BusEntity bus = new BusEntity();
        bus.setId(1L);
        bus.setPlateNumber("ABC-1234");

        when(busDao.save(any(BusEntity.class))).thenReturn(bus);

        BusEntity result = tripService.createBus(request);

        assertThat(result).isNotNull();
        assertThat(result.getPlateNumber()).isEqualTo("ABC-1234");
        verify(busDao).save(any(BusEntity.class));
    }
}
