package com.example.educationappsysproject.examsection.views.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.examsection.views.Model.ExamListModel;


import java.util.List;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.ExamListViewHolder> {

    private List<ExamListModel> examListModels;
    private OnItemClickedListner onItemClickedListner;

    public void setExamListModels(List<ExamListModel> examListModels) {
        this.examListModels = examListModels;
    }

    public void setOnItemClickedListner(OnItemClickedListner onItemClickedListner) {
        this.onItemClickedListner = onItemClickedListner;
    }
    public ExamListAdapter(OnItemClickedListner onItemClickedListner)
    {
        this.onItemClickedListner=onItemClickedListner;
    }

    @NonNull
    @Override
    public ExamListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_exam, parent, false);
        return new ExamListViewHolder(view, onItemClickedListner);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamListViewHolder holder, int position) {
        ExamListModel model = examListModels.get(position);
        holder.title.setText(model.getTitle());
        Glide.with(holder.itemView).load(model.getImage()).into(holder.examImage);
    }

    @Override
    public int getItemCount() {
        if (examListModels == null) {
            return 0;
        } else {
            return examListModels.size();
        }
    }

    public class ExamListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private ImageView examImage;
        private ConstraintLayout constraintLayout;
        private OnItemClickedListner onItemClickedListner;

        public ExamListViewHolder(@NonNull View itemView, OnItemClickedListner onItemClickedListner) {
            super(itemView);
            title = itemView.findViewById(R.id.quizTitleList);
            examImage = itemView.findViewById(R.id.quizImageList);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            this.onItemClickedListner = onItemClickedListner;
            constraintLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickedListner != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickedListner.onItemClick(position);
                }
            }
        }
    }

    public interface OnItemClickedListner {
        void onItemClick(int position);
    }
}
