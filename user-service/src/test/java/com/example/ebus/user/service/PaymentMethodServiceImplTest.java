package com.example.ebus.user.service;

import com.example.ebus.user.dao.PaymentMethodDao;
import com.example.ebus.user.dao.UserDao;
import com.example.ebus.user.dto.PaymentMethodRequest;
import com.example.ebus.user.dto.PaymentMethodResponse;
import com.example.ebus.user.entity.PaymentMethodEntity;
import com.example.ebus.user.entity.PaymentMethodType;
import com.example.ebus.user.entity.UserEntity;
import com.example.ebus.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PaymentMethodDao paymentMethodDao;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    private UserEntity sampleUser;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        sampleUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void addPaymentMethod_Success() {
        PaymentMethodRequest request = new PaymentMethodRequest(
                PaymentMethodType.CARD,
                "Stripe",
                "tok_123",
                true
        );

        PaymentMethodEntity pm = PaymentMethodEntity.builder()
                .id(1L)
                .type(PaymentMethodType.CARD)
                .provider("Stripe")
                .token("tok_123")
                .defaultMethod(true)
                .user(sampleUser)
                .build();

        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(paymentMethodDao.save(any(PaymentMethodEntity.class))).thenReturn(pm);

        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(PaymentMethodType.CARD);
        assertThat(response.getProvider()).isEqualTo("Stripe");
        verify(paymentMethodDao).save(any(PaymentMethodEntity.class));
    }

    @Test
    void addPaymentMethod_UserNotFound() {
        PaymentMethodRequest request = new PaymentMethodRequest(
                PaymentMethodType.CARD,
                "Stripe",
                "tok_123",
                false
        );

        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.addPaymentMethod(99L, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getPaymentMethods_Success() {
        PaymentMethodEntity pm = PaymentMethodEntity.builder()
                .id(1L)
                .type(PaymentMethodType.CARD)
                .provider("Stripe")
                .token("tok_123")
                .defaultMethod(true)
                .user(sampleUser)
                .build();

        when(userDao.existsById(1L)).thenReturn(true);
        when(paymentMethodDao.findByUserId(1L)).thenReturn(List.of(pm));

        List<PaymentMethodResponse> responses = paymentMethodService.getPaymentMethods(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getType()).isEqualTo(PaymentMethodType.CARD);
    }

    @Test
    void getPaymentMethods_UserNotFound() {
        when(userDao.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> paymentMethodService.getPaymentMethods(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getPaymentMethods_EmptyList() {
        when(userDao.existsById(1L)).thenReturn(true);
        when(paymentMethodDao.findByUserId(1L)).thenReturn(List.of());

        List<PaymentMethodResponse> responses = paymentMethodService.getPaymentMethods(1L);

        assertThat(responses).isEmpty();
    }
}
