package com.example.examproject.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//

public final class Document implements Serializable {

  private static final long serialVersionUID = 2934779488095936139L;
  private final String title;
  private final Instant publicationTimestamp;
  private final String documentId;
  private final List<Partner> parties;

  @JsonCreator
  public Document(
      @JsonProperty("title") String title,
      @JsonProperty("publicationTimestamp") Instant publicationTimestamp,
      @JsonProperty("documentId") String documentId,
      @JsonProperty("parties") List<Partner> parties) {
    this.title = title;
    this.publicationTimestamp = publicationTimestamp;
    this.documentId = documentId;

    if (parties == null) {
      this.parties = Collections.emptyList();
    } else {
      this.parties = Collections.unmodifiableList(new ArrayList<>(parties));
    }
  }

  public String getTitle() {
    return title;
  }

  public Instant getPublicationTimestamp() {
    return publicationTimestamp;
  }

  public String getDocumentId() {
    return documentId;
  }

  public List<Partner> getParties() {
    return parties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Document document = (Document) o;
    return Objects.equals(title, document.title)
        && Objects.equals(publicationTimestamp, document.publicationTimestamp)
        && Objects.equals(documentId, document.documentId)
        && Objects.equals(parties, document.parties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, publicationTimestamp, documentId, parties);
  }

  @Override
  public String toString() {
    return "Document{"
        + "title='"
        + title
        + '\''
        + ", publicationTimestamp="
        + publicationTimestamp
        + ", documentId='"
        + documentId
        + '\''
        + ", parties="
        + parties
        + '}';
  }

  public static final class Partner implements Serializable {

    private static final long serialVersionUID = 672529050000538408L;
    private final String partnerId;
    private final Instant signatureTimestamp;
    private final String firstName;
    private final String lastName;
    private final String organization;

    @JsonCreator
    public Partner(
        @JsonProperty("partnerId") String partnerId,
        @JsonProperty("signatureTimestamp") Instant signatureTimestamp,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("organization") String organization) {

      this.partnerId = partnerId;
      this.signatureTimestamp = signatureTimestamp;
      this.firstName = firstName;
      this.lastName = lastName;
      this.organization = organization;
    }

    public String getPartnerId() {
      return partnerId;
    }

    public Instant getSignatureTimestamp() {
      return signatureTimestamp;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String getOrganization() {
      return organization;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Partner partner = (Partner) o;
      return Objects.equals(partnerId, partner.partnerId)
          && Objects.equals(signatureTimestamp, partner.signatureTimestamp)
          && Objects.equals(firstName, partner.firstName)
          && Objects.equals(lastName, partner.lastName)
          && Objects.equals(organization, partner.organization);
    }

    @Override
    public int hashCode() {
      return Objects.hash(partnerId, signatureTimestamp, firstName, lastName, organization);
    }

    @Override
    public String toString() {
      return "Partner{"
          + "partnerId='"
          + partnerId
          + '\''
          + ", signatureTimestamp="
          + signatureTimestamp
          + ", firstName='"
          + firstName
          + '\''
          + ", lastName='"
          + lastName
          + '\''
          + ", organization='"
          + organization
          + '\''
          + '}';
    }
  }
}
