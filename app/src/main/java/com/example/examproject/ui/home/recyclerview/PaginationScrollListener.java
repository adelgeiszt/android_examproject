package com.example.examproject.ui.home.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

  public static final int PAGE_START = 0;
  /** Set scrolling threshold here (for now i'm assuming 10 item in one page) */
  private static final int PAGE_SIZE = 5;

  @NonNull private final LinearLayoutManager layoutManager;
  /**
   * Lehet 3 is de szerintem az a logikus ha mar elore betolt +2 es igy gyorsabb a tekeresnel. 10
   * volt eredetileg. Lehet total rosszul ertelmezem.
   */

  /** Supporting only LinearLayoutManager for now. */
  public PaginationScrollListener(@NonNull LinearLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
  }

  @Override
  public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    int visibleItemCount = layoutManager.getChildCount();
    int totalItemCount = layoutManager.getItemCount();
    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

    if (!isLoading() && !isLastPage()) {
      if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
          && firstVisibleItemPosition >= 0
          && totalItemCount >= PAGE_SIZE) {
        loadMoreItems();
      }
    }
  }

  protected abstract void loadMoreItems();

  public abstract boolean isLastPage();

  public abstract boolean isLoading();
}
