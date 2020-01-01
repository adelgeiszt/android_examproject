package com.example.examproject.ui.home.recyclerview;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.examproject.R;
import com.example.examproject.WebViewActivity;
import com.example.examproject.service.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DocumentRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
  private static final int VIEW_TYPE_LOADING = 0;
  private static final int VIEW_TYPE_NORMAL = 1;
  private static final String TAG = DocumentRecyclerAdapter.class.getName();
  private boolean isLoaderVisible = false;

  private final List<Document> documents; // declare

  public DocumentRecyclerAdapter(List<Document> documents) { // initialize
    if (documents == null) {
      this.documents = Collections.emptyList();
    } else {
      this.documents = new ArrayList<>(documents);
    }
  }

  @NonNull
  @Override
  public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case VIEW_TYPE_NORMAL:
        return new ViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false));
      case VIEW_TYPE_LOADING:
        return new ProgressHolder(
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_loading, parent, false));
      default:
        throw new IllegalArgumentException("Unsupported View type: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.onBind(position);
  }

  @Override
  public int getItemViewType(int position) {
    if (isLoaderVisible) {
      return position == documents.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
    } else {
      return VIEW_TYPE_NORMAL;
    }
  }

  @Override
  public int getItemCount() {
    return documents.size();
  }

  public void addItems(List<Document> docItems) {
    documents.addAll(docItems);
    notifyDataSetChanged();
  }

  public void addLoading() {
    isLoaderVisible = true;
  }

  public void removeLoading() {
    isLoaderVisible = false;
  }

  public void clear() {
    documents.clear();
    notifyDataSetChanged();
  }

  public Document getItem(int position) {
    return documents.get(position);
  }

  public static class ProgressHolder extends BaseViewHolder {
    public ProgressHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public class ViewHolder extends BaseViewHolder {
    @BindView(R.id.row_text)
    TextView textViewTitle;

    @BindView(R.id.home_frame_layout)
    FrameLayout frameLayout;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      frameLayout = itemView.findViewById(R.id.home_frame_layout);
      frameLayout.setOnClickListener(this::onFrameClick);

      TextView rowText = itemView.findViewById(R.id.row_text);
      rowText.setOnClickListener(this::onFrameClick);
    }

    @Override
    public void onBind(int position) {
      super.onBind(position);
      Document item = documents.get(position);
      textViewTitle.setText(item.getTitle());
    }

    public void onFrameClick(View view) {
      Intent intent = new Intent(itemView.getContext(), WebViewActivity.class);
      Document document = documents.get(getCurrentPosition());
      intent.putExtra(WebViewActivity.DOCUMENT_ID, document);
      ActivityOptions options =
          ActivityOptions.makeCustomAnimation(
              itemView.getContext(), R.anim.entry_anim, R.anim.exit_anim);
      // itemView.getContext().startActivity(intent, options.toBundle());
      ((Activity) itemView.getContext()).startActivityForResult(intent, 0, options.toBundle());
    }
  }
}
