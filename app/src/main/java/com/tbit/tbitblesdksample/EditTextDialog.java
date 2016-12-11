package com.tbit.tbitblesdksample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Salmon on 2016/4/26 0026.
 */
public class EditTextDialog extends DialogFragment {

    protected EditTextListener mListener;
    protected TextView mPositiveText;
    protected TextView mNegativeText;
    protected TextView mNeutralText;
    protected TextView mTitleView;
    protected EditText mEditText;
    protected TextInputLayout mInputLayout;

    protected String mNeutralContent = "";
    protected String mEditHint = "";
    protected String mEditContent = "";
    protected String mTitle = "";
    private int mInputType = -1;
    private Toast mToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_dialog_fragment, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mEditText = (EditText) view.findViewById(R.id.edit_dialog_fragment);
        mPositiveText = (TextView) view.findViewById(R.id.text_dialog_positive);
        mNegativeText = (TextView) view.findViewById(R.id.text_dialog_negative);
        mNeutralText = (TextView) view.findViewById(R.id.text_dialog_neutral);
        mInputLayout = (TextInputLayout) view.findViewById(R.id.input_layout);

        mTitleView.setText(mTitle);
        mEditText.setText(mEditContent);
        mEditText.setHint(mEditHint);

        if (mInputType != -1) {
            mEditText.setInputType(mInputType);
        }

        mPositiveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirm(mEditText.getText().toString());
                }
            }
        });

        mNegativeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancel();
                } else {
                    dismiss();
                }
            }
        });

        mNeutralText.setText(mNeutralContent);
        mNeutralText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onNeutral();
                }
            }
        });

    }

    public void setEditTextListener(EditTextListener listener) {
        mListener = listener;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setNeutralButton(String neutral) {
        mNeutralContent = neutral;
    }

    public void setEditTextHint(String hint) {
        mEditHint = hint;
    }

    public void setInputType(int type) {
        mInputType = type;
    }

    public void setEditTextContent(String content) {
        mEditContent = content;
    }

    protected void showToast(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        }
        mToast.setText(s);
        mToast.show();
    }

    public interface EditTextListener {
        void onConfirm(String editString);

        void onCancel();

        void onNeutral();
    }

}
