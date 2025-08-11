package com.example.educationappsysproject.examsection.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.examsection.views.Model.ExamListModel;
import com.example.educationappsysproject.examsection.views.viewmodel.ExamListViewModel;

import java.util.List;


public class detailFragment extends Fragment {
    private TextView title;
    private TextView totalQuestions;
    private Button startExamBtn;

    private NavController navController;
    private int position;
    private ProgressBar progressBar;
    private ExamListViewModel viewModel;
    private ImageView topicImage;
    private String ExamId;
    private Long totalQuescount;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel= new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(ExamListViewModel.class);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.detailFragmentTitle);
        totalQuestions = view.findViewById(R.id.detailFragmentQuestions);
        startExamBtn = view.findViewById(R.id.startQuizBtn);
        progressBar = view.findViewById(R.id.detailProgressBar);
        topicImage =view.findViewById(R.id.detailFragmentImage);
        navController = Navigation.findNavController(view);


        position=detailFragmentArgs.fromBundle(getArguments()).getPosition();
        viewModel.getExamListLiveData().observe(getViewLifecycleOwner(), new Observer<List<ExamListModel>>() {
            @Override
            public void onChanged(List<ExamListModel> examListModels) {
                ExamListModel exam= examListModels.get(position);
                title.setText(exam.getTitle());
                totalQuestions.setText(String.valueOf(exam.getQuestions()));
                Glide.with(view).load(exam.getImage()).into(topicImage);
                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                },2000);
                totalQuescount=exam.getQuestions();
                ExamId=exam.getExamId();
            }
        });
        startExamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailFragmentDirections.ActionDetailFragmentToQuizFragment action= detailFragmentDirections.actionDetailFragmentToQuizFragment();
                action.setExamId(ExamId);
                action.setTotalQueCount(totalQuescount);
                navController.navigate(action);
            }
        });
    }
}