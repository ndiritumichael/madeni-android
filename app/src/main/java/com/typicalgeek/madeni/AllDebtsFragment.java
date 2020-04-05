package com.typicalgeek.madeni;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllDebtsFragment extends Fragment implements RefreshInterface{
    DebtsActivity debtsActivity = new DebtsActivity();
    RecyclerView recyclerView;

    public AllDebtsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_all_debts, container, false);
        debtsActivity.swipeAllDebts = v.findViewById(R.id.swipeAllDebts);
        recyclerView = v.findViewById(R.id.rvAllDebts);
        debtsActivity.swipeAllDebts.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAll();
            }
        });
        final FloatingActionButton fab = this.getActivity().findViewById(R.id.fab);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fab.hide();
                else if (dy < 0) fab.show();
            }
        });
        refreshAll();
        return v;
    }

    @Override
    public void refreshAll() {
        refreshAll(false);
    }

    void refreshAll(boolean shouldRefresh) {
        if(!debtsActivity.swipeAllDebts.isRefreshing()){
            if (shouldRefresh){
                debtsActivity.swipeAllDebts.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            debtsActivity.swipeAllDebts.setRefreshing(false);
                        } catch (Exception e){
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, debtsActivity.REFRESH_LENGTH);
            }
        } else new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                debtsActivity.swipeAllDebts.setRefreshing(false);
            }
        }, debtsActivity.REFRESH_LENGTH);
        recyclerView.setHasFixedSize(true);
        AllDebtsAdapter adapter = new AllDebtsAdapter(this, DebtsActivity.dbGetAllDebts(), DebtsActivity.dbGetAllPayments());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(llm);
    }
}