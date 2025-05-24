package com.example.focusflow_frontend.presentation.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    private GroupViewModel viewModel;
    private List<Group> allGroups = new ArrayList<>();
    private GroupAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group, container, false);

        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        setupRecyclerView(view);
        setupSearch(view);
        setupAddButton(view);

        return view;
    }
// Dua group vao RecycleView
    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerGroupList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        viewModel.getGroupList().observe(getViewLifecycleOwner(), groups -> {
            allGroups = groups;
            adapter.setGroupList(groups);
        });

        viewModel.getFilteredGroups().observe(getViewLifecycleOwner(), filtered -> {
            adapter.setGroupList(filtered);
        });
    }
//Chuyen sang trang AddGroup
    private void setupAddButton(View view) {
        ImageView btnAdd = view.findViewById(R.id.imgAdd);
        btnAdd.setOnClickListener(v -> {
            AddGroupBottomSheet bottomSheet = new AddGroupBottomSheet();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
    }
//Loc group theo ten
    private void setupSearch(View view) {
        EditText edtSearch = view.findViewById(R.id.edtSearch);
        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchGroup(s.toString(), allGroups);
            }
        });
    }
}
