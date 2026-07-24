package org.assessment.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private HeaderAuthFilter headerAuthFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void securityFilterChainBeanCreated() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);

        DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(chain);

        SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity);

        assertThat(result).isEqualTo(chain);
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).sessionManagement(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).addFilterBefore(eq(headerAuthFilter), any());
    }
}
