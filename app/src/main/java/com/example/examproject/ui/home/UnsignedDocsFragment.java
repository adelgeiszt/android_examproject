package com.example.examproject.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.examproject.MainActivity;
import com.example.examproject.R;
import com.example.examproject.service.Document;
import com.example.examproject.service.Page;
import com.example.examproject.service.RetrofitService;
import com.example.examproject.ui.home.recyclerview.DocumentRecyclerAdapter;
import com.example.examproject.ui.home.recyclerview.PaginationScrollListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnsignedDocsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = UnsignedDocsFragment.class.getName();
  private static final int LIMIT = 5;
  private Instant queryTimestamp = Instant.now();
  private MainActivity mainActivity;

  @BindView(R.id.recyclerUnsigned)
  RecyclerView documentRecyclerView;

  @BindView(R.id.swipeRefresh)
  SwipeRefreshLayout swipeRefresh;

  private DocumentRecyclerAdapter documentRecyclerAdapter;
  private boolean isLastPage = false;
  private boolean isLoading = false;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_unsigned, container, false);
    ButterKnife.bind(this, root);

    // set up the RecyclerView
    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    documentRecyclerView.setLayoutManager(layoutManager);
    documentRecyclerAdapter = new DocumentRecyclerAdapter(new ArrayList<>());
    documentRecyclerView.setAdapter(documentRecyclerAdapter);

    // pagination code
    swipeRefresh.setOnRefreshListener(this);
    documentRecyclerView.setHasFixedSize(true);
    doApiCall();

    documentRecyclerView.addOnScrollListener(
        new PaginationScrollListener(layoutManager) {
          @Override
          protected void loadMoreItems() {
            isLoading = true;
            queryTimestamp =
                documentRecyclerAdapter
                    .getItem(documentRecyclerAdapter.getItemCount() - 1)
                    .getPublicationTimestamp();
            doApiCall();
          }

          @Override
          public boolean isLastPage() {
            return isLastPage;
          }

          @Override
          public boolean isLoading() {
            return isLoading;
          }
        });

    return root;
  }

  @Override
  public void onRefresh() {
    queryTimestamp = Instant.now();
    isLastPage = false;
    documentRecyclerAdapter.clear();
    doApiCall();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    mainActivity = (MainActivity) context;
    mainActivity.setOnRefresh(() -> swipeRefresh.post(() -> swipeRefresh.setRefreshing(true)));
  }

  private void doApiCall() {
    documentRecyclerAdapter.addLoading();
    mainActivity.performActionWithFreshTokens(
        (accessToken, idToken, ex) ->
            RetrofitService.getInstance()
                .getUnsignedDocuments(UnsignedDocsFragment.LIMIT, queryTimestamp, accessToken)
                .enqueue(
                    new Callback<Page>() {
                      private void removeLoading() {
                        // siker es hiba eseten is meghivodik:
                        swipeRefresh.setRefreshing(false);
                        isLoading = false;
                      }

                      @Override
                      public void onResponse(Call<Page> call, Response<Page> response) {
                        if (response.isSuccessful()) {
                          List<Document> documents = response.body().getResults();
                          documentRecyclerAdapter.addItems(documents);
                          if (documents.size() < UnsignedDocsFragment.LIMIT) {
                            isLastPage = true;
                            documentRecyclerAdapter.removeLoading();
                          } else {
                            documentRecyclerAdapter.addLoading();
                          }
                        } else {
                          Log.e(
                              UnsignedDocsFragment.TAG,
                              String.format("API error response received: [%s]", response));
                        }

                        removeLoading();
                      }

                      @Override
                      public void onFailure(Call<Page> call, Throwable t) {
                        Log.e(UnsignedDocsFragment.TAG, "API call failed", t);
                        removeLoading();
                      }
                    }));
  }
}
