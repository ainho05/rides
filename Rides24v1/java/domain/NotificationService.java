package domain;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import exceptions.NotificationException;

public class NotificationService {
    private static final String FROM_EMAIL = "tuemail@gmail.com"; // Cambiar por tu email real
    private static final String SMTP_USERNAME = "tuemail@gmail.com"; // Usuario SMTP
    private static final String SMTP_PASSWORD = "tupassword"; // Contraseña de aplicación
    private static final String SMTP_HOST = "smtp.gmail.com"; // Ejemplo para Gmail
    private static final int SMTP_PORT = 587; // Puerto para TLS

    public void sendRideAcceptedEmail(String toEmail, String driverEmail, String subject, String body) 
        throws NotificationException {
        
        try {
            // 1. Configuración SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            
            // 2. Autenticación
            Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                    }
                });

            // 3. Creación del mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            // 4. Envío
            Transport.send(message);
            
        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email notification: " + e.getMessage(), e);
        }
    }
}