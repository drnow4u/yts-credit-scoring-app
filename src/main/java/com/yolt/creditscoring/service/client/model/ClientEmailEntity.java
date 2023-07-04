package com.yolt.creditscoring.service.client.model;

import com.yolt.creditscoring.configuration.validation.constraints.EmailAddress;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Entity with client-specific data to fill invitation e-mail template placeholders with. Below you'll find a
 * visualisation of how these different placeholders are used in the invitation e-mail.
 *
 * ----------------------------------------------------------
 * |                 `title`  [`client.logo`](`websiteUrl`) |
 * |                                                        |
 * |                `subtitle`                              |
 * |                                                        |
 * | Dear name of user,                                     |
 * | `welcomebox`                                           |
 * |                                                        |
 * | -------------------------------------------------------|
 * |  [`buttonText`](`client.redirectUrl`)                  |
 * | -------------------------------------------------------|
 * |                                                        |
 * | `summaryBox`                                           |
 * |                                                        |
 * |--------------------------------------------------------|
 * |  Yolt Footer                                           |
 * |________________________________________________________|
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = ClientEmailEntity.TABLE_NAME)
@Entity
public class ClientEmailEntity {

    public static final String TABLE_NAME = "client_email";

    @Id
    private UUID id;

    @NotNull
    private String subject;

    @NotNull
    private String template;

    private String title;

    private String subtitle;

    @Column(name = "welcome_box")
    private String welcomeBox;

    @Column(name = "button_text")
    private String buttonText;

    @Column(name = "summary_box")
    private String summaryBox;

    @Column(name = "website_url")
    private String websiteUrl;

    @NotNull
    @EmailAddress
    private String sender;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientEntity client;
}
