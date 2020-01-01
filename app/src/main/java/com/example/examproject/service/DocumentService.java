package com.example.examproject.service;

import java.time.Instant;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DocumentService {
  @GET("unsigned") // get utan a vegpont van
  Call<Page> getUnsignedDocuments(
      @Query("limit") int limit,
      @Query("queryTimestamp") Instant queryTimestamp,
      @Header("Authorization") String authHeader);

  @GET("signed")
  Call<Page> getSignedDocuments(
      @Query("limit") int limit,
      @Query("queryTimestamp") Instant queryTimestamp,
      @Header("Authorization") String authHeader);

  @GET("{documentId}")
  Call<ResponseBody> getDocument(
      @Path("documentId") String documentId, @Header("Authorization") String authHeader);

  @POST("{documentId}/signature")
  Call<ResponseBody> postSignature(
      @Path("documentId") String documentId,
      @Body SignatureModel signatureModel,
      @Header("Authorization") String authHeader);
}
