package com.quochuy.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	@Value("${app.mail.enabled:false}")
	private boolean mailEnabled;

	@Value("${app.mail.from:no-reply@localhost}")
	private String from;

	public void sendActivationEmail(String to, String fullName, String activationUrl) {
		if (!mailEnabled) {
			log.info("Email sending is disabled. Activation link for {}: {}", to, activationUrl);
			return;
		}
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null) {
			throw new IllegalStateException("Email sending is enabled but JavaMailSender is not configured");
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject("Activate your Quoc Huy Paint account");
		message.setText("""
				Hi %s,

				Thanks for registering. Please activate your account using this link:

				%s

				This link expires in 24 hours.
				""".formatted(fullName, activationUrl));

		mailSender.send(message);
	}
}
