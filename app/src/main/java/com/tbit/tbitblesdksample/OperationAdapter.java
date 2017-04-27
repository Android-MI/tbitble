package com.tbit.tbitblesdksample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * Created by Salmon on 2017/4/19 0019.
 */

public class OperationAdapter extends RecyclerView.Adapter<OperationAdapter.OperationHolder> {

    private List<Operation> operationList;
    private Context context;
    private OperationListener operationListener;

    public OperationAdapter(List<Operation> operationList, Context context) {
        this.operationList = operationList;
        this.context = context;
    }

    @Override
    public OperationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        OperationHolder holder = new OperationHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_operation, parent,
                        false));
        return holder;
    }

    @Override
    public void onBindViewHolder(OperationHolder holder, final int position) {
        final Operation operation = operationList.get(position);
        String name = operation.getName();
        holder.button.setText(name);

        if (operationListener == null)
            return;

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operationListener.onOperationClick(operation.getCode());
            }
        });
    }

    @Override
    public int getItemCount() {
        return operationList.size();
    }

    public void setOprationListener(OperationListener oprationListener) {
        this.operationListener = oprationListener;
    }

    interface OperationListener {
        void onOperationClick(int operationCode);
    }

    class OperationHolder extends RecyclerView.ViewHolder {

        private Button button;

        public OperationHolder(View itemView) {
            super(itemView);

            button = (Button) itemView.findViewById(R.id.button_operation);
        }
    }
}
