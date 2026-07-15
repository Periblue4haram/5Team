package com.example.myeongranghoe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    /**
     * 발신 주소. 비어 있으면 spring.mail.username 을 사용.
     */
    private String from = "";
    private boolean exposeCodeWhenUndelivered = true;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isExposeCodeWhenUndelivered() {
        return exposeCodeWhenUndelivered;
    }

    public void setExposeCodeWhenUndelivered(boolean exposeCodeWhenUndelivered) {
        this.exposeCodeWhenUndelivered = exposeCodeWhenUndelivered;
    }
}
