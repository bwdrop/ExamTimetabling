package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Created by Héliane Ly on 17/08/2016.
 */
public class Mail {
    public static final String DATE_PLACEHOLDER = "[DATE]";
    public static final String START_TIME_PLACEHOLDER = "[START_TIME]";
    public static final String END_TIME_PLACEHOLDER = "[END_TIME]";
    public static final String GROUP_PLACEHOLDER = "[GROUP]";

    private final StringProperty smtpAuth = new SimpleStringProperty();
    private final StringProperty smtpStarttls = new SimpleStringProperty();
    private final StringProperty smtpServer = new SimpleStringProperty();
    private final StringProperty smtpPort = new SimpleStringProperty();
    private final StringProperty sender = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();

    private final StringProperty subject = new SimpleStringProperty();
    private final StringProperty body = new SimpleStringProperty();

    private Session session;

    public Mail() {}

    public void init() {
        Properties config = System.getProperties();
        config.setProperty("mail.smtp.host", smtpServer.getValue());
        config.setProperty("mail.smtp.port", smtpPort.getValue());
        config.setProperty("mail.smtp.auth", smtpAuth.getValue());
        config.setProperty("mail.smtp.starttls.enable", smtpStarttls.getValue());
        session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender.getValue(), password.getValue());
            }
        });
    }

    public int send(List<String> recipients, String date, String start, String end, int group) {
        try {
            final Message message = new MimeMessage(session);
            for (String to : recipients)
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setFrom(new InternetAddress(sender.getValue()));
            String sub = subject.getValue().replace(DATE_PLACEHOLDER, date)
                    .replace(START_TIME_PLACEHOLDER, start)
                    .replace(END_TIME_PLACEHOLDER, end)
                    .replace(GROUP_PLACEHOLDER, Integer.toString(group));
            String text = body.getValue().replace(DATE_PLACEHOLDER, date)
                    .replace(START_TIME_PLACEHOLDER, start)
                    .replace(END_TIME_PLACEHOLDER, end)
                    .replace(GROUP_PLACEHOLDER, Integer.toString(group));
            message.setSubject(sub);
            message.setText(text);
            Transport.send(message);
            System.out.println("Sent message successfully to.... " + recipients);
        } catch (MessagingException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    public String getSmtpAuth() {
        return smtpAuth.get();
    }

    public StringProperty smtpAuthProperty() {
        return smtpAuth;
    }

    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth.set(smtpAuth);
    }

    public String getSmtpStarttls() {
        return smtpStarttls.get();
    }

    public StringProperty smtpStarttlsProperty() {
        return smtpStarttls;
    }

    public void setSmtpStarttls(String smtpStarttls) {
        this.smtpStarttls.set(smtpStarttls);
    }

    public String getSmtpServer() {
        return smtpServer.get();
    }

    public StringProperty smtpServerProperty() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer.set(smtpServer);
    }

    public String getSmtpPort() {
        return smtpPort.get();
    }

    public StringProperty smtpPortProperty() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort.set(smtpPort);
    }

    public String getSender() {
        return sender.get();
    }

    public StringProperty senderProperty() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender.set(sender);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public String getBody() {
        return body.get();
    }

    public StringProperty bodyProperty() {
        return body;
    }

    public void setBody(String body) {
        this.body.set(body);
    }
}