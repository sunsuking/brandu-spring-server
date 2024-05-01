package shop.brandu.server.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;


    public void sendSignUpEmail(String to, String nickname, String code) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setSubject("BrandU 회원가입 인증 메일입니다.");
        helper.setTo(to);

        // TODO: 변수로 바꾸기
        String confirmURI = "http://localhost:8080/api/v1/auth/confirm?code=" + code + "&email=" + to;

        Context context = new Context();
        context.setVariable("confirmURI", confirmURI);
        context.setVariable("nickname", nickname);

        String html = templateEngine.process("mail/confirm_member_account_mail", context);
        helper.setText(html, true);

        javaMailSender.send(message);
    }
}
