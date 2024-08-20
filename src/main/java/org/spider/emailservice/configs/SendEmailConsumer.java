package org.spider.emailservice.configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.spider.emailservice.dtos.SendEmailDto;
import org.spider.emailservice.utils.EmailUtil;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Authenticator;
import java.util.Properties;

@Service
@Getter
@Setter
public class SendEmailConsumer {
//    this method should be triggered if any sendEmail event happens
//    here group id is mentioned because in prod there will be multiple instance of email service will be running so one service should handle this and other should ignore
    private ObjectMapper objectMapper;
    private EmailUtil emailUtil;
    private  SendEmailDto sendEmailDto = null;
    private String emailPassword;
    public SendEmailConsumer(ObjectMapper objectMapper, EmailUtil emailUtil) {
        this.objectMapper = objectMapper;
        this.emailUtil = emailUtil;
    }
    @KafkaListener(topics = "sendEmail", groupId = "emailService")
    public void sendEmailMessage(String message){
//        code to send an email

        try {
            sendEmailDto = objectMapper.readValue(message, SendEmailDto.class);
        } catch (JsonProcessingException e) {
            System.out.println("serialization failed");
            throw new RuntimeException(e);
        }
//        send email
//        uses SMTP - simple mail transfer protocol
//        creating SMTP session for sending email
        System.out.println("Sending email starts");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sendEmailDto.getFrom(), "${EMAIL_PWD}");
            }
        };
        Session session = Session.getInstance(props, auth);
        emailUtil.sendEmail(session,sendEmailDto.getTo(),sendEmailDto.getSubject(),sendEmailDto.getBody());

    }
}
