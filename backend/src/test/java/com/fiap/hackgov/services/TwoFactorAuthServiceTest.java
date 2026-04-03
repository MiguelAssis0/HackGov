package com.fiap.hackgov.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TwoFactorAuthServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    @Test
    void generateCode_ReturnsSixDigits() {
        String code = twoFactorAuthService.generateCode();

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void sendTwoFactorCode_Success() {
        String email = "test@example.com";
        String name = "Test User";

        assertDoesNotThrow(() -> twoFactorAuthService.sendTwoFactorCode(email, name));
        
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendTwoFactorCode_EmailFailure_ThrowsException() {
        String email = "test@example.com";
        String name = "Test User";

        doThrow(new RuntimeException("Mail service failed")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> twoFactorAuthService.sendTwoFactorCode(email, name));
        
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void verifyCode_Success() {
        String email = "test@example.com";
        String name = "Test User";

        twoFactorAuthService.sendTwoFactorCode(email, name);

        String code = "123456";
        
        // Create TwoFactorCode instance using reflection
        try {
            Class<?> twoFactorCodeClass = Class.forName("com.fiap.hackgov.services.TwoFactorAuthService$TwoFactorCode");
            java.lang.reflect.Constructor<?> constructor = twoFactorCodeClass.getDeclaredConstructor(String.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object twoFactorCode = constructor.newInstance(code, LocalDateTime.now().plusMinutes(10));
            
            // Set up code storage with test data
            ConcurrentMap<String, Object> testStorage = new ConcurrentHashMap<>();
            testStorage.put(email, twoFactorCode);
            ReflectionTestUtils.setField(twoFactorAuthService, "codeStorage", testStorage);
        } catch (Exception e) {
            fail("Failed to set up test data: " + e.getMessage());
        }

        boolean result = twoFactorAuthService.verifyCode(email, code);

        assertTrue(result);
    }

    @Test
    void verifyCode_InvalidCode_ReturnsFalse() {
        String email = "test@example.com";
        String name = "Test User";

        twoFactorAuthService.sendTwoFactorCode(email, name);

        boolean result = twoFactorAuthService.verifyCode(email, "999999");

        assertFalse(result);
    }

    @Test
    void verifyCode_EmailNotFound_ReturnsFalse() {
        String email = "nonexistent@example.com";
        String code = "123456";

        boolean result = twoFactorAuthService.verifyCode(email, code);

        assertFalse(result);
    }

    @Test
    void verifyCode_ExpiredCode_ReturnsFalse() {
        String email = "test@example.com";
        String code = "123456";
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(1);

        // Create expired TwoFactorCode instance using reflection
        try {
            Class<?> twoFactorCodeClass = Class.forName("com.fiap.hackgov.services.TwoFactorAuthService$TwoFactorCode");
            java.lang.reflect.Constructor<?> constructor = twoFactorCodeClass.getDeclaredConstructor(String.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object twoFactorCode = constructor.newInstance(code, expiredTime);
            
            // Set up code storage with test data
            ConcurrentMap<String, Object> testStorage = new ConcurrentHashMap<>();
            testStorage.put(email, twoFactorCode);
            ReflectionTestUtils.setField(twoFactorAuthService, "codeStorage", testStorage);
        } catch (Exception e) {
            fail("Failed to set up test data: " + e.getMessage());
        }

        boolean result = twoFactorAuthService.verifyCode(email, code);

        assertFalse(result);
    }

    @Test
    void verifyCode_SuccessfulVerification_RemovesCode() {
        String email = "test@example.com";
        String code = "123456";
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(10);

        // Create TwoFactorCode instance using reflection
        try {
            Class<?> twoFactorCodeClass = Class.forName("com.fiap.hackgov.services.TwoFactorAuthService$TwoFactorCode");
            java.lang.reflect.Constructor<?> constructor = twoFactorCodeClass.getDeclaredConstructor(String.class, LocalDateTime.class);
            constructor.setAccessible(true);
            Object twoFactorCode = constructor.newInstance(code, futureTime);
            
            // Set up code storage with test data
            ConcurrentMap<String, Object> testStorage = new ConcurrentHashMap<>();
            testStorage.put(email, twoFactorCode);
            ReflectionTestUtils.setField(twoFactorAuthService, "codeStorage", testStorage);
        } catch (Exception e) {
            fail("Failed to set up test data: " + e.getMessage());
        }

        twoFactorAuthService.verifyCode(email, code);

        boolean result = twoFactorAuthService.verifyCode(email, code);

        assertFalse(result);
    }
}
