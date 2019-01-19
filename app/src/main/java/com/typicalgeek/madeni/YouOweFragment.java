package com.typicalgeek.madeni;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class YouOweFragment extends Fragment implements RefreshInterface{
    DebtsActivity debtsActivity = new DebtsActivity();
    RecyclerView recyclerView;
    public YouOweFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_you_owe, container, false);
        recyclerView  = v.findViewById(R.id.rvYouOwe);
        debtsActivity.swipeYouOwe = v.findViewById(R.id.swipeYouOwe);
        debtsActivity.swipeYouOwe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        if(!debtsActivity.swipeYouOwe.isRefreshing()){
            if (shouldRefresh){
                debtsActivity.swipeYouOwe.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        debtsActivity.swipeYouOwe.setRefreshing(false);
                    }
                }, debtsActivity.REFRESH_LENGTH);
            }
        } else new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                debtsActivity.swipeYouOwe.setRefreshing(false);
            }
        }, debtsActivity.REFRESH_LENGTH);
        recyclerView.setHasFixedSize(true);
        YouOweAdapter adapter = new YouOweAdapter(this, DebtsActivity.dbGetFilteredDebts
                (DatabaseHelper.DEBTS_COL_5, DatabaseHelper.DEBT_OWE),
                DebtsActivity.dbGetAllPayments());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
    }
}