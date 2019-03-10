package com.typicalgeek.madeni;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AllDebtsAdapter extends RecyclerView.Adapter<AllDebtsAdapter.MyViewHolder>{
    private static Debt[] mDebt;
    private static Payment[] mPayment;
    static DatabaseHelper databaseHelper;
    private static float[] totalPayment;
    private static RefreshInterface mListener;

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView mCardView;
        TextView mAmountOwed, mDebtType, mPerson, mPaid;
        ImageButton mBtnDotsTresAll;
        ProgressBar mPercentage;
        int thisDay, thisMonth, thisYear, thisHour, thisMinute;

        MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.card_view);
            mPercentage = v.findViewById(R.id.pbPercentagePaidAll);
            mBtnDotsTresAll = v.findViewById(R.id.btnOverflowAll);
            mAmountOwed = v.findViewById(R.id.tv_amount_owed);
            mDebtType = v.findViewById(R.id.tv_debt_type);
            mPerson = v.findViewById(R.id.tv_person_name);
            mPaid = v.findViewById(R.id.tv_percentage_paid_all);
            mBtnDotsTresAll.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                            (new ContextThemeWrapper(view.getContext(), DebtsActivity.dialogThemeID));
                    final Debt d = mDebt[getAdapterPosition()];
                    final String phone, description, type;
                    final DialogInterface.OnClickListener editClick = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final EditText etPer, etPhn, etAmt, etDsc;
                            final Spinner spDsc, spTyp;
                            final CheckBox cbRem;
                            LayoutInflater inflater = LayoutInflater.from(view.getContext());
                            final View editView = inflater.inflate(R.layout.content_new_debt,
                                    (ViewGroup) view.getParent(), false);
                            etPer = editView.findViewById(R.id.etPersonName);
                            etPhn = editView.findViewById(R.id.etPersonPhone);
                            etAmt = editView.findViewById(R.id.etAmountOwed);
                            etDsc = editView.findViewById(R.id.etDebtDescription);
                            spDsc = editView.findViewById(R.id.descriptionSpinner);
                            spTyp = editView.findViewById(R.id.debtTypeSpinner);
                            cbRem = editView.findViewById(R.id.cbReminder);
                            new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), DebtsActivity.dialogThemeID))
                                    .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (etPhn.getText().toString().isEmpty()) etPhn.setText("0");
                                            if (etAmt.getText().toString().isEmpty()) etAmt.setText("0");
                                            final String desc = spDsc.getSelectedItem().toString(),
                                                    descArr[] = view.getResources().getStringArray(R.array.descriptionSpinnerArray),
                                                    description = (desc.equals(descArr[0])||desc.equals(descArr[3]))?
                                                            etDsc.getText().toString().trim() : spDsc.getSelectedItem().toString().trim();
                                            final Debt edits = new Debt(etPer.getText().toString().trim(),
                                                    Long.parseLong(etPhn.getText().toString().trim()),
                                                    Float.parseFloat(etAmt.getText().toString()),
                                                    description, spTyp.getSelectedItem().toString().trim());
                                            if (validate(view, edits)) {
                                                if (cbRem.isChecked()) {
                                                    LayoutInflater inflater = LayoutInflater.from(view.getContext());
                                                    final View remView = inflater.inflate(R.layout.layout_reminder,
                                                            (ViewGroup) view.getParent(), false);
                                                    final CalendarView calendarView = remView.findViewById(R.id.calendarViewReminder);
                                                    thisMinute = Integer.parseInt(new SimpleDateFormat("mm", Locale.getDefault()).format(new Date()));
                                                    thisHour = Integer.parseInt(new SimpleDateFormat("hh", Locale.getDefault()).format(new Date()));
                                                    thisDay = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
                                                    thisMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date()));
                                                    thisYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()));
                                                    new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), DebtsActivity.dialogThemeID))
                                                            .setTitle("Add Reminder")
                                                            .setView(remView)
                                                            .setPositiveButton("ADD AND SAVE", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Calendar remTime = Calendar.getInstance();
                                                                    thisMinute = Integer.parseInt(new SimpleDateFormat("mm", Locale.getDefault()).format(new Date()));
                                                                    thisHour = Integer.parseInt(new SimpleDateFormat("hh", Locale.getDefault()).format(new Date()));
                                                                    remTime.set(thisYear, thisMonth, thisDay, thisHour, thisMinute);
                                                                    Intent intent = new Intent(Intent.ACTION_INSERT)
                                                                            .setData(CalendarContract.Events.CONTENT_URI)
                                                                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, remTime.getTimeInMillis())
                                                                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, remTime.getTimeInMillis())
                                                                            .putExtra(CalendarContract.Events.TITLE, String.format(Locale.getDefault(), "Madeni App: %s\'s debt", edits.getDebtName()))
                                                                            .putExtra(CalendarContract.Events.DESCRIPTION,
                                                                                    String.format(Locale.getDefault(), "Reminder for debt to %s. View in app for more details.", edits.getDebtName()))
                                                                            .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
                                                                    databaseHelper.updateData(edits, d.getDebtID());
                                                                    mListener.refreshAll();
                                                                    view.getContext().startActivity(intent);
                                                                }
                                                            })
                                                            .setNegativeButton("CANCEL", null)
                                                            .create().show();
                                                    calendarView.setMinDate(new Date().getTime());
                                                    calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                                                        @Override
                                                        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                                                            thisDay = dayOfMonth;
                                                            thisMonth = month;
                                                            thisYear = year;
                                                        }
                                                    });
                                                } else {
                                                    databaseHelper.updateData(edits, d.getDebtID());
                                                    mListener.refreshAll();
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("CANCEL", null)
                                    .setTitle("Edit")
                                    .setView(editView)
                                    .create().show();

                            etPer.setText(d.getDebtName());
                            if (d.getDebtPhone()!=0) etPhn.setText(String.format(Locale.getDefault(),"%d",d.getDebtPhone()));
                            etAmt.setText(String.format(Locale.getDefault(), "%.2f", d.getDebtAmount()));
                            if (!d.getDebtDescription().isEmpty()) etDsc.setText(d.getDebtDescription());
                            String[] spVal = view.getContext().getResources().getStringArray(R.array.typeSpinnerArray);
                            spTyp.setSelection(find(spVal, d.getDebtType()));
                            spDsc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (position == 3) editView.findViewById(R.id.tilDescription).setVisibility(View.VISIBLE);
                                    else editView.findViewById(R.id.tilDescription).setVisibility(View.GONE);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    };
                    phone = d.getDebtPhone() == 0 ? "Undefined" : String.format(Locale.getDefault(), "%d", d.getDebtPhone());
                    if (d.getDebtDescription().trim().isEmpty()) description = null; else description = d.getDebtDescription();
                    if (d.getDebtType().equals(DatabaseHelper.DEBT_OWE)) type="owed to"; else type="owed to you by";
                    alertDialogBuilder.setNeutralButton("CLOSE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                            .setPositiveButton("EDIT", editClick)
                            .setTitle("Details")
                            .setMessage(String.format(Locale.getDefault(),
                                    "%1$s %2$,.2f from %1$s %3$,.2f %4$s %5$s.\nPhone Number: %6$s\nEntered on %7$s\nDescription:\n%8$s",
                                    DebtsActivity.currency, (d.getDebtAmount()-totalPayment[getAdapterPosition()]), d.getDebtAmount(), type, d.getDebtName(), phone, d.getDebtDate(), description))
                            .create().show();
                }
            });
        }

        private boolean validate(View view, Debt debt) {
            if (debt.getDebtName().isEmpty()) {
                Toast.makeText(view.getContext(), "ERROR! Person's Name cannot be empty.", Toast.LENGTH_LONG).show();
                return false;
            } else if (debt.getDebtAmount() == 0) {
                Toast.makeText(view.getContext(), "ERROR! Amount cannot be empty.", Toast.LENGTH_LONG).show();
                return false;
            }else if (debt.getDebtType().equals(view.getContext().getResources().getStringArray(R.array.typeSpinnerArray)[0])) {
                Toast.makeText(view.getContext(), "ERROR! Please pick a debt type.", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        private int find(String[] dataSource, String target) {
            for (int i = 0; i<dataSource.length; i++){
                if(target.trim().equals(dataSource[i].trim())) return i;
            }
            return -1;
        }

        @Override
        public void onClick(final View view) {
            databaseHelper = new DatabaseHelper(view.getContext());
            final Debt d = mDebt[getAdapterPosition()];
            final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            final String phone;
            if (d.getDebtPhone()==0) phone = "Undefined"; else phone = "254"+d.getDebtPhone();
            popupMenu.getMenuInflater().inflate(R.menu.dots_overflow, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder
                            (new ContextThemeWrapper(view.getContext(), DebtsActivity.dialogThemeID));
                    switch (id) {
                        case R.id.payEntry: {
                            final LayoutInflater inflater = LayoutInflater.from(view.getContext());
                            final View paymentsView = inflater.inflate(R.layout.layout_payments,
                                    (ViewGroup) view.getParent(), false);
                            final RecyclerView recyclePay = paymentsView.findViewById(R.id.rvPayments);
                            alertDialogBuilder.setPositiveButton("ADD PAYMENT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final View payView = inflater.inflate(R.layout.layout_add_payment,
                                            (ViewGroup) view.getParent(), false);
                                    final EditText etPay = payView.findViewById(R.id.etAmountPaid);
                                    new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), DebtsActivity.dialogThemeID))
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        float payment = Float.parseFloat(etPay.getText().toString().trim());
                                                        if (payment == 0)
                                                            Toast.makeText(view.getContext(), "Enter a value", Toast.LENGTH_SHORT).show();
                                                        else {
                                                            new DebtsActivity().addPayment(view.getContext(), new Payment(d.getDebtID(), payment,
                                                                    new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())));
                                                            mListener.refreshAll();
                                                        }
                                                    }catch (Exception e){
                                                        Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton("CANCEL", null)
                                            .setTitle("Add Payment")
                                            .setView(payView)
                                            .create().show();
                                }
                            })
                                    .setNegativeButton("CLOSE", null)
                                    .setTitle("Payments");

                            Payment[] paymentsArray = DebtsActivity.dbGetFilteredPayments(DatabaseHelper.PAYMENTS_COL_1, d.getDebtID());
                            if (paymentsArray.length != 0) alertDialogBuilder.setView(paymentsView);
                            else {
                                TextView myMsg = new TextView(paymentsView.getContext());
                                myMsg.setText("No payments made yet.");
                                myMsg.setGravity(Gravity.CENTER);
                                alertDialogBuilder.setView(myMsg);
                            }
                            alertDialogBuilder.create().show();
                            PaymentsAdapter paymentsAdapter = new PaymentsAdapter(view.getContext(), paymentsArray);
                            recyclePay.setAdapter(paymentsAdapter);
                            LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
                            recyclePay.setLayoutManager(manager);
                            break;
                        }
                        case R.id.nudgeEntry: {
                            String pref = PreferenceManager.getDefaultSharedPreferences(view.getContext()).getString("nudge", "5").trim();
                            if (!phone.equals("Undefined")){
                                DebtsActivity.vector = 0;
                                if (pref.equals("3")) {
                                    alertDialogBuilder.setPositiveButton("NUDGE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switcher();

                                        }
                                    })
                                            .setNegativeButton("CANCEL", null)
                                            .setTitle("Nudge")
                                            .setSingleChoiceItems(R.array.nudgeArray, 0, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    DebtsActivity.vector = which;
                                                }
                                            }).create().show();
                                } else {
                                    DebtsActivity.vector = Integer.parseInt(pref);
                                    if (!phone.equals("Undefined")) switcher();
                                    else Toast.makeText(view.getContext(),
                                            mDebt[getAdapterPosition()].getDebtName() + " has no phone number.",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }else Toast.makeText(view.getContext(),
                                    mDebt[getAdapterPosition()].getDebtName() + " has no phone number.",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case R.id.deleteEntry: {
                            alertDialogBuilder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseHelper.deleteDebt(d.getDebtID());
                                    mListener.refreshAll();
                                }
                            })
                                    .setNegativeButton("CANCEL", null)
                                    .setTitle("Delete")
                                    .setMessage("This action cannot be undone!")
                                    .create().show();
                            break;
                        }
                    }
                    return true;
                }
                private void switcher() {
                    String msg = "Hey %1$s, %2$s your debt of %3$s %4$,.2f soon. ", joiner;
                    if (d.getDebtType().equals(DatabaseHelper.DEBT_OWED)) joiner="please pay"; else joiner="kindly expect";
                    if (PreferenceManager.getDefaultSharedPreferences(view.getContext())
                            .getBoolean("signature", false))
                        msg = msg.concat("Sent via Madeni App.");
                    switch (DebtsActivity.vector){
                        case 0:
                            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                            smsIntent.setData(Uri.parse("smsto:"));
                            smsIntent.setType("vnd.android-dir/mms-sms");
                            smsIntent.putExtra("address", phone);
                            smsIntent.putExtra("sms_body", String.format(Locale.getDefault(), msg, d.getDebtName(),
                                    joiner, DebtsActivity.currency, d.getDebtAmount()));
                            view.getContext().startActivity(smsIntent);
                            break;
                        case 1:
                            try {
                                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW)
                                        .setData(Uri.parse("https://wa.me/"+phone+"?text="+
                                                String.format(Locale.getDefault(), msg, d.getDebtName(),
                                                        joiner, DebtsActivity.currency, d.getDebtAmount()))));
                            } catch (Exception e){
                                Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 2:
                            Intent i = new Intent(Intent.ACTION_SEND)
                                    .setType("text/plain")
                                    .putExtra(Intent.EXTRA_SUBJECT, String.format(Locale.getDefault(),
                                            "RE: %1$s %2$,.2f debt",
                                            DebtsActivity.currency, d.getDebtAmount()))
                                    .putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), msg, d.getDebtName(),
                                            joiner, DebtsActivity.currency, d.getDebtAmount()));
                            view.getContext().startActivity(new Intent(Intent.createChooser(i, "Nudge via:")));
                    }
                }
            });
            popupMenu.show();
        }
    }

    AllDebtsAdapter(RefreshInterface myListener, Debt[] myDebt, Payment[] myPayment){
        mListener = myListener;
        mDebt = myDebt;
        mPayment = myPayment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item_all, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        totalPayment = new float[mDebt.length];
        for (Payment pay : mPayment) {
            if (pay.getPaymentDebt() == mDebt[position].getDebtID())
                totalPayment[position] += pay.getPaymentAmount();
        }
        int payPercentage = totalPayment[position] == 0 ? 0 : ((int) ((totalPayment[position] / mDebt[position].getDebtAmount() * 100) + 0.5));
        payPercentage = payPercentage>100?100:payPercentage;
        float remainingDebt = mDebt[position].getDebtAmount()-totalPayment[position];
        remainingDebt = remainingDebt<0?0:remainingDebt;
        String debtType, pronoun;
        if (mDebt[position].getDebtType().equals(DatabaseHelper.DEBT_OWE)) {
            debtType = "You owe:";
            pronoun = "to";
        } else {
            debtType = "You are owed:";
            pronoun = "by";
        }
        holder.mDebtType.setText(debtType);
        holder.mPerson.setText(String.format(Locale.getDefault(), "%1$s %2$s",
                pronoun, mDebt[position].getDebtName()));
        holder.mAmountOwed.setText(String.format(Locale.getDefault(), "%1$s %2$,.2f",
                DebtsActivity.currency, remainingDebt));
        holder.mPaid.setText(String.format(Locale.getDefault(),"%d%%", payPercentage));
        holder.mPercentage.setProgress(payPercentage);
    }

    @Override
    public int getItemCount() {
        return mDebt.length;
    }
}