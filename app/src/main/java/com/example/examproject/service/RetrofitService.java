package com.example.examproject.service;

import com.example.examproject.ContractApp;
import com.example.examproject.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public final class RetrofitService implements DocumentService {
  private static final RetrofitService OUR_INSTANCE = new RetrofitService();
  private final DocumentService documentService;
  private static final String AUTH_HEADER_PREFIX = "Bearer ";

  private RetrofitService() {

    // CREATING JAVA OBJECTS FROM THE JSON
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    JacksonConverterFactory jacksonConverterFactory = JacksonConverterFactory.create(objectMapper);

    String baseUrl = ContractApp.getAppContext().getString(R.string.document_service_base_url);

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jacksonConverterFactory)
            .build();
    documentService = retrofit.create(DocumentService.class);
  }

  public static RetrofitService getInstance() {
    return OUR_INSTANCE;
  }

  @Override
  public Call<Page> getUnsignedDocuments(int limit, Instant queryTimestamp, String authToken) {
    return documentService.getUnsignedDocuments(
        limit, queryTimestamp, AUTH_HEADER_PREFIX + authToken);
  }

  @Override
  public Call<Page> getSignedDocuments(int limit, Instant queryTimestamp, String authToken) {
    return documentService.getSignedDocuments(
        limit, queryTimestamp, AUTH_HEADER_PREFIX + authToken);
  }

  @Override
  public Call<ResponseBody> getDocument(String documentId, String authToken) {
    return documentService.getDocument(documentId, AUTH_HEADER_PREFIX + authToken);
  }

  @Override
  public Call<ResponseBody> postSignature(
      String documentId, SignatureModel signatureModel, String authToken) {
    return documentService.postSignature(
        documentId, signatureModel, AUTH_HEADER_PREFIX + authToken);
  }
}
