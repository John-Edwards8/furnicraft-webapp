package com.john.webapp;

import com.john.webapp.dto.ChangeRequestDto;
import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.security.ClientUserDetailsService;
import com.john.webapp.service.ChangeRequestServiceClient;
import com.john.webapp.service.ClientServiceClient;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
 
import java.time.LocalDate;
import java.util.List;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
 
/**
 * Unit Test — ChangeRequestServiceClient та ClientUserDetailsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeRequestServiceClient + ClientUserDetailsService — Unit Tests")
class ChangeRequestAndSecurityTest {
 
    // ── Mocks ─────────────────────────────────────────────────────
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec  getUriSpec;
    @Mock private WebClient.RequestHeadersSpec     headersSpec;
    @Mock private WebClient.ResponseSpec           getResponse;
    @Mock private WebClient.RequestBodyUriSpec     postUriSpec;
    @Mock private WebClient.RequestBodySpec        postBodySpec;
    @Mock private WebClient.ResponseSpec           postResponse;
 
    @Mock private ClientServiceClient clientServiceMock;
 
    private ChangeRequestServiceClient changeService;
    private ClientUserDetailsService userDetailsService;
 
    private ChangeRequestDto sample;
 
    @BeforeEach
    void setUp() {
        changeService      = new ChangeRequestServiceClient(webClient);
        userDetailsService = new ClientUserDetailsService(clientServiceMock);
 
        sample = new ChangeRequestDto();
        sample.setId(1L);
        sample.setEstimateId(2L);
        sample.setClientEmail("lysen@example.com");
        sample.setRequestText("Замінити матеріал на МДФ 16мм");
        sample.setRequestDate(LocalDate.of(2024, 3, 16));
        sample.setStatus("новий");
    }
 
    // ── GET helper ────────────────────────────────────────────────
    private void stubGet() {
        when(webClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString(), anyLong())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(getResponse);
    }
 
    // ── POST helper ───────────────────────────────────────────────
    private void stubPost() {
        when(webClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(postResponse);
    }
 
    // ══ ChangeRequestServiceClient ════════════════════════════════
 
    @Test
    @DisplayName("getByEstimateId — повертає список запитів")
    void getByEstimateId_returnsList() {
        stubGet();
        when(getResponse.bodyToFlux(ChangeRequestDto.class))
                .thenReturn(Flux.just(sample));
 
        List<ChangeRequestDto> result = changeService.getByEstimateId(2L);
 
        assertEquals(1, result.size());
        assertEquals("новий", result.get(0).getStatus());
        assertEquals("lysen@example.com", result.get(0).getClientEmail());
    }
 
    @Test
    @DisplayName("getByEstimateId — порожній список при помилці")
    void getByEstimateId_emptyOnError() {
        stubGet();
        when(getResponse.bodyToFlux(ChangeRequestDto.class))
                .thenReturn(Flux.error(new RuntimeException("timeout")));
 
        assertTrue(changeService.getByEstimateId(999L).isEmpty());
    }
 
    @Test
    @DisplayName("create — повертає збережений DTO з ID")
    void create_returnsSaved() {
        stubPost();
        ChangeRequestDto saved = new ChangeRequestDto();
        saved.setId(5L);
        saved.setStatus("новий");
        when(postResponse.bodyToMono(ChangeRequestDto.class)).thenReturn(Mono.just(saved));
 
        ChangeRequestDto result = changeService.create(sample);
 
        assertNotNull(result);
        assertEquals(5L, result.getId());
    }
 
    // ── ChangeRequestDto ──────────────────────────────────────────
 
    @Test
    @DisplayName("ChangeRequestDto — всі поля коректні")
    void changeRequestDto_fieldsWork() {
        ChangeRequestDto dto = new ChangeRequestDto();
        dto.setId(10L);
        dto.setEstimateId(3L);
        dto.setClientEmail("test@test.com");
        dto.setRequestText("Змінити позицію");
        dto.setRequestDate(LocalDate.of(2024, 1, 1));
        dto.setStatus("розглянутий");
 
        assertAll(
            () -> assertEquals(10L,   dto.getId()),
            () -> assertEquals(3L,    dto.getEstimateId()),
            () -> assertEquals("test@test.com",  dto.getClientEmail()),
            () -> assertEquals("Змінити позицію", dto.getRequestText()),
            () -> assertEquals("розглянутий",     dto.getStatus())
        );
    }
 
    // ══ ClientUserDetailsService ══════════════════════════════════
 
    @Test
    @DisplayName("loadUserByUsername — повертає роль CLIENT для role=0")
    void loadUser_clientRole() {
        ClientResponseDto c = new ClientResponseDto();
        c.setEmail("lysen@example.com");
        c.setPass("pass123");
        c.setRole(0L);
        when(clientServiceMock.getAllClients()).thenReturn(List.of(c));
 
        UserDetails ud = userDetailsService.loadUserByUsername("lysen@example.com");
 
        assertEquals("lysen@example.com", ud.getUsername());
        assertTrue(ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
    }
 
    @Test
    @DisplayName("loadUserByUsername — повертає роль ADMIN для role=1")
    void loadUser_adminRole() {
        ClientResponseDto c = new ClientResponseDto();
        c.setEmail("admin@furnicraft.com");
        c.setPass("admin123");
        c.setRole(1L);
        when(clientServiceMock.getAllClients()).thenReturn(List.of(c));
 
        UserDetails ud = userDetailsService.loadUserByUsername("admin@furnicraft.com");
 
        assertTrue(ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
 
    @Test
    @DisplayName("loadUserByUsername — кидає UsernameNotFoundException для невідомого email")
    void loadUser_throwsForUnknown() {
        when(clientServiceMock.getAllClients()).thenReturn(List.of());
 
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost@ghost.com"));
    }
 
    @Test
    @DisplayName("loadUserByUsername — нечутливий до регістру email")
    void loadUser_caseInsensitive() {
        ClientResponseDto c = new ClientResponseDto();
        c.setEmail("petro@example.com");
        c.setPass("pwd");
        c.setRole(0L);
        when(clientServiceMock.getAllClients()).thenReturn(List.of(c));
 
        UserDetails ud = userDetailsService.loadUserByUsername("PETRO@EXAMPLE.COM");
 
        assertEquals("petro@example.com", ud.getUsername());
    }
}