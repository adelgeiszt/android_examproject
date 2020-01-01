package com.example.examproject.ui.home.recyclerview;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

  private int currentPosition;

  public BaseViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public void onBind(int position) {
    currentPosition = position;
  }

  public int getCurrentPosition() {
    return currentPosition;
  }
}
