package com.example.educationappsysproject.examsection.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.example.educationappsysproject.R;
import com.example.educationappsysproject.examsection.views.Adapter.ExamListAdapter;
import com.example.educationappsysproject.examsection.views.Model.ExamListModel;
import com.example.educationappsysproject.examsection.views.viewmodel.ExamListViewModel;

import java.util.List;

public class ListFragment extends Fragment implements ExamListAdapter.OnItemClickedListner {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NavController navController;
    private ExamListViewModel viewModel;
    private ExamListAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel= new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(ExamListViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView=view.findViewById(R.id.listRecycledView);
        progressBar=view.findViewById(R.id.examListprogressBar);
        navController= Navigation.findNavController(view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter= new ExamListAdapter(this);
        recyclerView.setAdapter(adapter);
        viewModel.getExamListLiveData().observe(getViewLifecycleOwner(), new Observer<List<ExamListModel>>() {
            @Override
            public void onChanged(List<ExamListModel> examListModels) {
                progressBar.setVisibility(View.GONE);
                adapter.setExamListModels(examListModels);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
    ListFragmentDirections.ActionListFragmentToDetailFragment action=
            ListFragmentDirections.actionListFragmentToDetailFragment();
        action.setPosition(position);
            navController.navigate(action);
    }
}