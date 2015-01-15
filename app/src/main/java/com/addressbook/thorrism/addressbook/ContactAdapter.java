package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lucas Crawford on 1/11/2015.
 */
public class ContactAdapter extends ArrayAdapter<Contact> {

    public static Context mContext;
    public int mLayoutId;
    public List<Contact> mData;
    public List<Character> mCharHeaders;
    public char mCurrentLetter;

    public ContactAdapter(Context context, int layoutId, List<Contact> data){
        super(context, layoutId, data);
        mContext       = context;
        mLayoutId      = layoutId;
        mData          = data;
        mCharHeaders   = new ArrayList<Character>();
        mCurrentLetter = ',';
    }


    public char getCurrentLetter() {
        return mCurrentLetter;
    }

    public void setCurrentLetter(char mCurrentLetter) {
        this.mCurrentLetter = mCurrentLetter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactHolder holder = null;
        final TextView contactName;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, parent, false);
            holder = new ContactHolder();
            convertView.setTag(holder);
        } else {
            holder = (ContactHolder) convertView.getTag();
        }

        //Set the text for the contact's name within the TextView in our ContactHolder
        contactName = (TextView) convertView.findViewById(R.id.contactNameView);
        Contact contact = mData.get(position);
        String firstName = contact.getFirstName();
        contactName.setText(firstName + " " + contact.getLastName());
        holder.setContactName(contactName);

        //Check if we need to add a header for the current contact
        if (firstName.charAt(0) != getCurrentLetter()) {
            holder.setHeaderView((TextView) convertView.findViewById(R.id.contactItemHeader));
            holder.getHeaderView().setText(Character.toString(firstName.charAt(0)).toUpperCase());
            setCurrentLetter(firstName.charAt(0));
            mCharHeaders.add(firstName.charAt(0));
            holder.getHeaderView().setVisibility(View.VISIBLE);

            //This overrides clicking on a header view... weird but works.
            holder.getHeaderView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(DroidBook.TAG, "Header clicked");
                }
            });
        }
        return convertView;
    }

    /*Class for holding the views associated with a Contact.*/
    public class ContactHolder {
        private TextView mContactName;
        private TextView mHeaderView;

        public ContactHolder(){
            mHeaderView    = null;
            mContactName   = null;
        }

        public TextView getHeaderView() {
            return mHeaderView;
        }

        public void setHeaderView(TextView mHeaderView) {
            this.mHeaderView = mHeaderView;
        }

        public TextView getContactName() {
            return mContactName;
        }

        public void setContactName(TextView mContactName) {
            this.mContactName = mContactName;
        }
    }
}

