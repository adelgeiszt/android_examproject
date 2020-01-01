package com.example.examproject.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Page implements Serializable {

  private static final long serialVersionUID = -33772085220810360L;
  private final int page;
  private final int limit;
  private final Instant queryTimestamp;
  private final List<Document> results;

  @JsonCreator
  public Page(
      @JsonProperty("page") int page,
      @JsonProperty("limit") int limit,
      @JsonProperty("queryTimestamp") Instant queryTimestamp,
      @JsonProperty("results") List<Document> results) {
    this.page = page;
    this.limit = limit;
    this.queryTimestamp = queryTimestamp;

    if (results == null) {
      this.results = Collections.emptyList();
    } else {
      this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }
  }

  public int getPage() {
    return page;
  }

  public int getLimit() {
    return limit;
  }

  public Instant getQueryTimestamp() {
    return queryTimestamp;
  }

  public List<Document> getResults() {
    return results;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Page page1 = (Page) o;
    return page == page1.page
        && limit == page1.limit
        && Objects.equals(queryTimestamp, page1.queryTimestamp)
        && Objects.equals(results, page1.results);
  }

  @Override
  public int hashCode() {
    return Objects.hash(page, limit, queryTimestamp, results);
  }

  @Override
  public String toString() {
    return "Page{"
        + "page="
        + page
        + ", limit="
        + limit
        + ", queryTimestamp="
        + queryTimestamp
        + ", results="
        + results
        + '}';
  }
}
