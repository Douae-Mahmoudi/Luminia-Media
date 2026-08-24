package com.mediatheque.auth_svc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@mediatheque.com");
    }



    @Test
    void sendPasswordResetCode_callsMailSenderOnce() {
        emailService.sendPasswordResetCode("user@example.com", "394601");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetCode_setsCorrectFrom() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordResetCode("user@example.com", "394601");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("noreply@mediatheque.com");
    }

    @Test
    void sendPasswordResetCode_setsCorrectTo() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordResetCode("user@example.com", "394601");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).contains("user@example.com");
    }

    @Test
    void sendPasswordResetCode_setsCorrectSubject() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordResetCode("user@example.com", "394601");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Votre code de réinitialisation");
    }

    @Test
    void sendPasswordResetCode_bodyContainsCode() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordResetCode("user@example.com", "394601");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("394601");
    }

    @Test
    void sendPasswordResetCode_bodyContainsExpectedSentences() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordResetCode("user@example.com", "394601");

        verify(mailSender).send(captor.capture());
        String body = captor.getValue().getText();
        assertThat(body).contains("Vous avez demandé la réinitialisation de votre mot de passe");
        assertThat(body).contains("valable 15 minutes");
        assertThat(body).contains("L'équipe Médiathèque");
    }

    @Test
    void sendPasswordResetCode_differentCodesAreIncludedCorrectly() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPasswordResetCode("autre@example.com", "000001");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("000001");
        assertThat(captor.getValue().getTo()).contains("autre@example.com");
    }

    @Test
    void sendPasswordResetCode_mailSenderThrows_propagatesException() {
        doThrow(new RuntimeException("SMTP indisponible"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> emailService.sendPasswordResetCode("user@example.com", "394601")
        );
    }
}