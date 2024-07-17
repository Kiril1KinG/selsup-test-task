package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApiImpl(10, 50);
        String json = "{\"description\":\n" +
                "{ \"participantInn\": \"string\" }, \"doc_id\": \"string\", \"doc_status\": \"string\",\n" +
                "\"doc_type\": \"LP_INTRODUCE_GOODS\", \"importRequest\": true,\n" +
                "\"owner_inn\": \"string\", \"participant_inn\": \"string\", \"producer_inn\":\n" +
                "\"string\", \"production_date\": \"2020-01-23\", \"production_type\": \"string\",\n" +
                "\"products\": [ { \"certificate_document\": \"string\",\n" +
                "\"certificate_document_date\": \"2020-01-23\",\n" +
                "\"certificate_document_number\": \"string\", \"owner_inn\": \"string\",\n" +
                "\"producer_inn\": \"string\", \"production_date\": \"2020-01-23\",\n" +
                "\"tnved_code\": \"string\", \"uit_code\": \"string\", \"uitu_code\": \"string\" } ],\n" +
                "\"reg_date\": \"2020-01-23\", \"reg_number\": \"string\"}";
        String signature = "ADC343c35r3c67p0";

        System.out.println(crptApi.addDocument(json, signature));
    }
}
class CrptApiImpl implements CrptApi {

    private final long timeIntervalInSeconds;
    private final long requestsLimit;
    private long requestsCount;
    private LocalDateTime end;
    private final ObjectMapper jsonMapper;
    private final CrptService crptService = new CrptServiceImpl();

    public CrptApiImpl(long timeIntervalInSeconds, long requestLimit) {
        jsonMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        this.timeIntervalInSeconds = timeIntervalInSeconds;
        this.requestsLimit = requestLimit;
        this.requestsCount = 0;
        this.end = LocalDateTime.now().plusSeconds(timeIntervalInSeconds);
    }

    @Override
    public synchronized Document addDocument(String document, String signature) {
        if (!isRequestsLimitReached()) {
            try {
                return crptService.addDocument(jsonMapper.readValue(document, Document.class), signature);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private boolean isRequestsLimitReached() {
        if (requestsCount == requestsLimit) {
            if (end.isAfter(LocalDateTime.now())) {
                return true;
            } else {
                requestsCount = 1;
                end = LocalDateTime.now().plusSeconds(timeIntervalInSeconds);
                return false;
            }
        } else {
            if (end.isAfter(LocalDateTime.now())) {
                requestsCount++;
            } else {
                requestsCount = 1;
                end = LocalDateTime.now().plusSeconds(timeIntervalInSeconds);
            }
            return false;
        }
    }

}

class CrptServiceImpl implements CrptService {

    @Override
    public Document addDocument(Document document, String signature) {
        document.setSignature(signature);
        return document;
    }
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Document {

    @JsonProperty("description")
    private ParticipantInn description;

    @JsonProperty("doc_id")
    private String docId;

    @JsonProperty("doc_status")
    private String docStatus;

    @JsonProperty("doc_type")
    private String docType = "LP_INTRODUCE_GOODS";

    @JsonProperty("importRequest")
    private boolean importRequest;

    @JsonProperty("owner_inn")
    private String ownerInn;

    @JsonProperty("participant_inn")
    private String participantInn;

    @JsonProperty("producer_inn")
    private String producerInn;

    @JsonProperty("production_date")
    private LocalDate productionDate;

    @JsonProperty("products")
    private List<Product> products;

    @JsonProperty("production_type")
    private String productionType;

    @JsonProperty("reg_date")
    private LocalDate regDate;

    @JsonProperty("reg_number")
    private String regNumber;

    @JsonIgnore
    private String signature;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class ParticipantInn {

    @JsonProperty("participantInn")
    private String participantInn;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Product {
    @JsonProperty("certificate_document")
    private String certificateDocument;

    @JsonProperty("certificate_document_date")
    private LocalDate certificateDocumentDate;

    @JsonProperty("certificate_document_number")
    private String certificateDocumentNumber;

    @JsonProperty("owner_inn")
    private String ownerInn;

    @JsonProperty("producer_inn")
    private String producerInn;

    @JsonProperty("production_date")
    private LocalDate productionDate;

    @JsonProperty("tnved_code")
    private String tnvedCode;

    @JsonProperty("uit_code")
    private String uitCode;

    @JsonProperty("uitu_code")
    private String uituCode;
}

interface CrptApi {

    Document addDocument(String document, String signature);
}

interface CrptService {

    Document addDocument(Document document, String signature);
}

