package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.List;


/**
 * Created by Lucas Crawford on 1/11/2015.
 */
public class CreateContactAdapter extends ArrayAdapter<String> {

    public static Context mContext;
    public int mLayoutId;
    public List<String> mData;

    public CreateContactAdapter(Context context, int layoutId, List<String> data){
        super(context, layoutId, data);
        mContext  = context;
        mLayoutId = layoutId;
        mData     = data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        CreateContactHolder holder = null;

        if(convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, parent, false);
            EditText[] formEdits = {
                    (EditText)convertView.findViewById(R.id.firstNameEdit),
                    (EditText)convertView.findViewById(R.id.lastNameEdit),
                    (EditText)convertView.findViewById(R.id.cityNameEdit),
                    (EditText)convertView.findViewById(R.id.addressNameEdit),
                    (EditText)convertView.findViewById(R.id.stateNameEdit),
                    (EditText)convertView.findViewById(R.id.zipcodeEdit)};
            EditText formEdit = formEdits[position];

            holder = new CreateContactHolder(formEdit);
            convertView.setTag(holder);
        }else {
            holder = (CreateContactHolder) convertView.getTag();
        }
        return convertView;
    }

    /*Class for holding the views associated with an Address Book.*/
    public class CreateContactHolder{
        private EditText mFormEdit;

        public CreateContactHolder(EditText formEdit){
            mFormEdit = formEdit;
        }


    }
}

