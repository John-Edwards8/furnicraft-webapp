package com.john.webapp;

import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.dto.OrderResponseDto;
import com.john.webapp.service.ClientServiceClient;
import com.john.webapp.service.OrderServiceClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests — ClientServiceClient + OrderServiceClient + всі webapp DTO
 * Платформа: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
@DisplayName("ClientServiceClient + OrderServiceClient + DTOs — Unit Tests")
class ClientAndOrderServiceClientTest {
	 
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec  getUriSpec;
    @Mock private WebClient.RequestHeadersSpec     getHeadersSpec;
    @Mock private WebClient.ResponseSpec           getResponse;
    @Mock private WebClient.RequestBodyUriSpec     bodyUriSpec;
    @Mock private WebClient.RequestBodySpec        bodySpec;
    @Mock private WebClient.ResponseSpec           bodyResponse;
 
    private ClientServiceClient clientService;
    private OrderServiceClient  orderService;
 
    private ClientResponseDto client1;
    private ClientResponseDto client2;
    private OrderResponseDto  order1;
    private OrderResponseDto  order2;
 
    @BeforeEach
    void setUp() {
        clientService = new ClientServiceClient(webClient);
        orderService  = new OrderServiceClient(webClient);
 
        client1 = new ClientResponseDto();
        client1.setId(1L); client1.setName("Петро"); client1.setSurname("Савченко");
        client1.setEmail("petro@example.com"); client1.setPass("hashed_pwd_1"); client1.setRole(0L);
 
        client2 = new ClientResponseDto();
        client2.setId(2L); client2.setName("Дмитро"); client2.setSurname("Лисенко");
        client2.setEmail("lysen@example.com"); client2.setPass("pass123"); client2.setRole(0L);
 
        order1 = new OrderResponseDto();
        order1.setId(1L); order1.setClientId(1L);
        order1.setStatus("виконується"); order1.setOrderDate(new Date());
 
        order2 = new OrderResponseDto();
        order2.setId(2L); order2.setClientId(1L);
        order2.setStatus("виконано"); order2.setOrderDate(new Date());
    }
 
    // ── helpers ───────────────────────────────────────────────────
 
    private void stubGet() {
        when(webClient.get()).thenReturn(getUriSpec);
        lenient().when(getUriSpec.uri(anyString())).thenReturn(getHeadersSpec);
        lenient().when(getUriSpec.uri(anyString(), anyLong())).thenReturn(getHeadersSpec);
        lenient().when(getUriSpec.uri(anyString(), any(Object[].class))).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponse);
    }
 
    private void stubPost() {
        when(webClient.post()).thenReturn(bodyUriSpec);
        lenient().when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(bodyResponse);
    }
 
    private void stubPut() {
        when(webClient.put()).thenReturn(bodyUriSpec);
        lenient().when(bodyUriSpec.uri(anyString(), anyLong())).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(bodyResponse);
        lenient().when(bodyResponse.toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.ok().build()));
        lenient().when(bodyResponse.bodyToMono(ClientResponseDto.class))
                .thenReturn(Mono.just(client1));
    }
 
    private void stubDelete() {
        WebClient.RequestHeadersUriSpec deleteSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(webClient.delete()).thenReturn(deleteSpec);
        lenient().when(deleteSpec.uri(anyString(), anyLong())).thenReturn(getHeadersSpec);
        lenient().when(deleteSpec.uri(anyString(), any(Object[].class))).thenReturn(getHeadersSpec);
        lenient().when(deleteSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponse);
        when(getResponse.toBodilessEntity())
                .thenReturn(Mono.just(ResponseEntity.ok().build()));
    }
 
    // ══ ClientServiceClient ═══════════════════════════════════════
 
    @Test
    @DisplayName("getAllClients — повертає список всіх клієнтів")
    void getAllClients_returnsList() {
        stubGet();
        when(getResponse.bodyToFlux(ClientResponseDto.class))
                .thenReturn(Flux.just(client1, client2));
 
        List<ClientResponseDto> result = clientService.getAllClients();
 
        assertEquals(2, result.size());
        assertEquals("petro@example.com", result.get(0).getEmail());
        assertEquals("lysen@example.com", result.get(1).getEmail());
    }
 
    @Test
    @DisplayName("getAllClients — порожній список при помилці сервісу")
    void getAllClients_emptyOnError() {
        stubGet();
        when(getResponse.bodyToFlux(ClientResponseDto.class))
                .thenReturn(Flux.error(new RuntimeException("service down")));
 
        assertTrue(clientService.getAllClients().isEmpty());
    }
 
    @Test
    @DisplayName("getClientById — повертає Optional з клієнтом")
    void getClientById_returnsOptional() {
        stubGet();
        when(getResponse.bodyToMono(ClientResponseDto.class))
                .thenReturn(Mono.just(client1));
 
        Optional<ClientResponseDto> result = clientService.getClientById(1L);
 
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Петро", result.get().getName());
    }
 
    @Test
    @DisplayName("getClientById — порожній Optional для неіснуючого клієнта")
    void getClientById_emptyForMissing() {
        stubGet();
        when(getResponse.bodyToMono(ClientResponseDto.class))
                .thenReturn(Mono.empty());
 
        assertFalse(clientService.getClientById(999L).isPresent());
    }
 
    @Test
    @DisplayName("createClient — повертає збереженого клієнта з ID")
    void createClient_returnsSaved() {
        stubPost();
        when(bodyResponse.bodyToMono(ClientResponseDto.class))
                .thenReturn(Mono.just(client1));
 
        ClientResponseDto result = clientService.createClient(client1);
 
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("petro@example.com", result.getEmail());
    }
 
    @Test
    @DisplayName("updateClient — завершується без виняткiв")
    void updateClient_callsPut() {
        stubPut();
        assertDoesNotThrow(() -> clientService.updateClient(client1));
        verify(webClient).put();
    }
 
    @Test
    @DisplayName("deleteClient — завершується без винятків")
    void deleteClient_callsDelete() {
        stubDelete();
        assertDoesNotThrow(() -> clientService.deleteClient(1L));
        verify(webClient).delete();
    }
 
    // ══ OrderServiceClient ════════════════════════════════════════
 
    @Test
    @DisplayName("getAllOrders — повертає список всіх замовлень")
    void getAllOrders_returnsList() {
        stubGet();
        when(getResponse.bodyToFlux(OrderResponseDto.class))
                .thenReturn(Flux.just(order1, order2));
 
        List<OrderResponseDto> result = orderService.getAllOrders();
 
        assertEquals(2, result.size());
        assertEquals("виконується", result.get(0).getStatus());
        assertEquals("виконано", result.get(1).getStatus());
    }
 
    @Test
    @DisplayName("getAllOrders — порожній список при помилці")
    void getAllOrders_emptyOnError() {
        stubGet();
        when(getResponse.bodyToFlux(OrderResponseDto.class))
                .thenReturn(Flux.error(new RuntimeException("timeout")));
 
        assertTrue(orderService.getAllOrders().isEmpty());
    }
 
    @Test
    @DisplayName("getOrdersByClientId — повертає замовлення клієнта")
    void getOrdersByClientId_returnsFiltered() {
        stubGet();
        when(getResponse.bodyToFlux(OrderResponseDto.class))
                .thenReturn(Flux.just(order1, order2));
 
        List<OrderResponseDto> result = orderService.getOrdersByClientId(1L);
 
        assertEquals(2, result.size());
    }
 
    @Test
    @DisplayName("getOrdersByClientId — порожній список при помилці")
    void getOrdersByClientId_emptyOnError() {
        stubGet();
        when(getResponse.bodyToFlux(OrderResponseDto.class))
                .thenReturn(Flux.error(new RuntimeException("error")));
 
        assertTrue(orderService.getOrdersByClientId(1L).isEmpty());
    }
 
    @Test
    @DisplayName("deleteOrder — завершується без винятків")
    void deleteOrder_callsDelete() {
        stubDelete();
        assertDoesNotThrow(() -> orderService.deleteOrder(1L, 1L));
        verify(webClient).delete();
    }
 
    // ══ ClientResponseDto ═════════════════════════════════════════
 
    @Test
    @DisplayName("ClientResponseDto — всі поля геттерів/сеттерів")
    void clientResponseDto_fieldsWork() {
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId(5L); dto.setName("Іван"); dto.setSurname("Франко");
        dto.setPatronymic("Якович"); dto.setPhoneNumber("0501234567");
        dto.setEmail("ivan@example.com"); dto.setPass("secret"); dto.setRole(1L);
 
        assertAll(
            () -> assertEquals(5L,                 dto.getId()),
            () -> assertEquals("Іван",             dto.getName()),
            () -> assertEquals("Франко",           dto.getSurname()),
            () -> assertEquals("Якович",           dto.getPatronymic()),
            () -> assertEquals("0501234567",       dto.getPhoneNumber()),
            () -> assertEquals("ivan@example.com", dto.getEmail()),
            () -> assertEquals("secret",           dto.getPass()),
            () -> assertEquals(1L,                 dto.getRole())
        );
    }
 
    @Test
    @DisplayName("ClientResponseDto — новий об'єкт має null поля")
    void clientResponseDto_defaultNulls() {
        ClientResponseDto dto = new ClientResponseDto();
        assertNull(dto.getId());
        assertNull(dto.getEmail());
        assertNull(dto.getRole());
    }
 
    // ══ OrderResponseDto ══════════════════════════════════════════
 
    @Test
    @DisplayName("OrderResponseDto — всі поля геттерів/сеттерів")
    void orderResponseDto_fieldsWork() {
        Date now = new Date();
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(10L); dto.setClientId(3L);
        dto.setOrderDate(now); dto.setStatus("виконується");
 
        assertAll(
            () -> assertEquals(10L,           dto.getId()),
            () -> assertEquals(3L,            dto.getClientId()),
            () -> assertEquals(now,           dto.getOrderDate()),
            () -> assertEquals("виконується", dto.getStatus())
        );
    }
 
    @Test
    @DisplayName("OrderResponseDto — новий об'єкт має null поля")
    void orderResponseDto_defaultNulls() {
        OrderResponseDto dto = new OrderResponseDto();
        assertNull(dto.getId());
        assertNull(dto.getStatus());
        assertNull(dto.getClientId());
    }
 
    @Test
    @DisplayName("OrderResponseDto — поле id встановлюється через setId")
    void orderResponseDto_setIdWorks() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(7L);
        assertEquals(7L, dto.getId());
    }
}