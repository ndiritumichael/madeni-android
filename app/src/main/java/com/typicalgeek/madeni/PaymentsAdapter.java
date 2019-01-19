package com.typicalgeek.madeni;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.MyViewHolder>{
    private static Payment[] mPay;
    private Context mCtx;
    private static DatabaseHelper databaseHelper;
    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mAmountPaid, mDatePaid;
        ImageButton ibDeletePay;
        MyViewHolder(View v) {
            super(v);
            mAmountPaid = v.findViewById(R.id.tvPaymentAmount);
            mDatePaid= v.findViewById(R.id.tvPaymentDate);
            ibDeletePay = v.findViewById(R.id.ibDelete);
            ibDeletePay.setOnClickListener(this);
        }
        @Override
        public void onClick(final View v) {
            databaseHelper = new DatabaseHelper(v.getContext());
            new AlertDialog.Builder
                    (new ContextThemeWrapper(v.getContext(), DebtsActivity.dialogThemeID))
                    .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseHelper.deletePayment(mPay[getAdapterPosition()].getPaymentID());
                            v.getContext().startActivity(new Intent(v.getContext(), DebtsActivity.class));
                            ((Activity)new PaymentsAdapter(v.getContext(), mPay).mCtx).finish();
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .setTitle("Delete")
                    .setMessage("This action cannot be undone!")
                    .create().show();
        }
    }
    PaymentsAdapter(Context myContext, Payment[] myPayments) {
        mPay = myPayments;
        mCtx = myContext;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_item, parent, false);
        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mAmountPaid.setText(String.format(Locale.getDefault(), "%1$s %2$,.2f",
                DebtsActivity.currency, mPay[position].getPaymentAmount()));
        holder.mDatePaid.setText(String.format(Locale.getDefault(), "%s",
                mPay[position].getPaymentDate()));
    }
    @Override
    public int getItemCount() {
        return mPay.length;
    }
}