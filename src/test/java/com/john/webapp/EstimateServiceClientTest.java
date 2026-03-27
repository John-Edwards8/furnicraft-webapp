package com.john.webapp;

import com.john.webapp.dto.EstimateResponseDto;
import com.john.webapp.service.EstimateServiceClient;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
 
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
 
/**
 * Unit Test — EstimateServiceClient
 * Покриває всі публічні методи сервісного клієнта кошторисів.
 * Платформа: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
@DisplayName("EstimateServiceClient — Unit Tests")
class EstimateServiceClientTest {
 
    // ── Mocks для WebClient fluent API ────────────────────────────
    @Mock private WebClient webClient;
    // GET chain mocks
    @Mock private WebClient.RequestHeadersUriSpec  getUriSpec;
    @Mock private WebClient.RequestHeadersSpec     getHeadersSpec;
    @Mock private WebClient.ResponseSpec           getResponse;
 
    // POST/PUT/PATCH/DELETE chain mocks
    @Mock private WebClient.RequestBodyUriSpec     bodyUriSpec;
    @Mock private WebClient.RequestBodySpec        bodySpec;
    @Mock private WebClient.ResponseSpec           bodyResponse;
 
    private EstimateServiceClient service;
 
    // ── Тестові дані ──────────────────────────────────────────────
    private EstimateResponseDto draft;
    private EstimateResponseDto finalized;
 
    @BeforeEach
    void setUp() {
        service = new EstimateServiceClient(webClient);
 
        draft = new EstimateResponseDto();
        draft.setId(1L);
        draft.setName("Попередній кошторис");
        draft.setDate(LocalDate.of(2024, 1, 15));
        draft.setIsFinal(false);
        draft.setOrderId(1L);
 
        finalized = new EstimateResponseDto();
        finalized.setId(2L);
        finalized.setName("Фінальний кошторис");
        finalized.setDate(LocalDate.of(2024, 1, 18));
        finalized.setIsFinal(true);
        finalized.setOrderId(1L);
    }
 
    // ── GET chain helper ──────────────────────────────────────────
    private void stubGet() {
        when(webClient.get()).thenReturn(getUriSpec);
        // LENIENT: всі варіанти uri() — кожен тест використовує свій
        lenient().when(getUriSpec.uri(anyString(), any(Object[].class))).thenReturn(getHeadersSpec);
        lenient().when(getUriSpec.uri(anyString(), anyLong())).thenReturn(getHeadersSpec);
        lenient().when(getUriSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponse);
    }
 
    // ── POST/PATCH chain helper ───────────────────────────────────
    private void stubPost() {
        when(webClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(getHeadersSpec); // bodyValue → RequestHeadersSpec
        when(getHeadersSpec.retrieve()).thenReturn(bodyResponse);
    }
 
    private void stubPatch() {
        when(webClient.patch()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(anyString(), anyLong())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(bodyResponse);
    }
 
    // ── getByOrderId ──────────────────────────────────────────────
 
    @Test
    @DisplayName("getByOrderId — повертає список кошторисів замовлення")
    void getByOrderId_returnsList() {
        stubGet();
        when(getResponse.bodyToFlux(EstimateResponseDto.class))
                .thenReturn(Flux.just(draft, finalized));
 
        List<EstimateResponseDto> result = service.getByOrderId(1L);
 
        assertEquals(2, result.size());
        assertFalse(result.get(0).getIsFinal());
        assertTrue(result.get(1).getIsFinal());
    }
 
    @Test
    @DisplayName("getByOrderId — повертає порожній список при помилці сервісу")
    void getByOrderId_returnsEmptyOnError() {
    	stubGet();
        when(getResponse.bodyToFlux(EstimateResponseDto.class))
                .thenReturn(Flux.error(new RuntimeException("unavailable")));
 
        assertTrue(service.getByOrderId(99L).isEmpty());
    }
 
    // ── getById ───────────────────────────────────────────────────
 
    @Test
    @DisplayName("getById — повертає Optional з кошторисом")
    void getById_returnsOptional() {
        stubGet();
        when(getResponse.bodyToMono(EstimateResponseDto.class)).thenReturn(Mono.just(draft));
 
        Optional<EstimateResponseDto> result = service.getById(1L);
 
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
 
    @Test
    @DisplayName("getById — порожній Optional для відсутнього ID")
    void getById_emptyOptionalForMissing() {
        stubGet();
        when(getResponse.bodyToMono(EstimateResponseDto.class)).thenReturn(Mono.empty());
 
        assertFalse(service.getById(999L).isPresent());
    }
 
    // ── create ────────────────────────────────────────────────────
 
    @Test
    @DisplayName("create — повертає збережений кошторис з ID")
    void create_returnsSavedWithId() {
    	stubPost();
        EstimateResponseDto saved = new EstimateResponseDto();
        saved.setId(3L);
        saved.setIsFinal(false);
        when(bodyResponse.bodyToMono(EstimateResponseDto.class)).thenReturn(Mono.just(saved));
 
        EstimateResponseDto result = service.create(draft);
 
        assertNotNull(result);
        assertEquals(3L, result.getId());
    }
 
    // ── finalize ──────────────────────────────────────────────────
 
    @Test
    @DisplayName("finalize — повертає кошторис з isFinal=true")
    void finalize_returnsEstimateWithFinalTrue() {
    	stubPatch();
        when(bodyResponse.bodyToMono(EstimateResponseDto.class)).thenReturn(Mono.just(finalized));
 
        EstimateResponseDto result = service.finalize(1L);
 
        assertNotNull(result);
        assertTrue(result.getIsFinal());
    }
 
    // ── EstimateDto ───────────────────────────────────────────────
 
    @Test
    @DisplayName("EstimateDto — всі поля працюють коректно")
    void estimateDto_fieldsWork() {
    	EstimateResponseDto dto = new EstimateResponseDto();
        dto.setId(10L);
        dto.setName("Тест");
        dto.setDescription("Опис");
        dto.setDate(LocalDate.of(2024, 3, 1));
        dto.setIsFinal(true);
        dto.setOrderId(5L);
 
        assertAll(
            () -> assertEquals(10L, dto.getId()),
            () -> assertEquals("Тест", dto.getName()),
            () -> assertEquals("Опис", dto.getDescription()),
            () -> assertTrue(dto.getIsFinal()),
            () -> assertEquals(5L, dto.getOrderId())
        );
    }
 
    @Test
    @DisplayName("EstimateDto — новий об'єкт має isFinal=null")
    void estimateDto_defaultIsFinalIsNull() {
        assertNull(new EstimateResponseDto().getIsFinal());
    }
}