package com.addressbook.thorrism.addressbook;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Lucas Crawford on 1/11/2015.
 */
public class BookAdapter extends ArrayAdapter<AddressBook> {

    public static Context mContext;
    public int mLayoutId;
    public List<AddressBook> mData;

    public BookAdapter(Context context, int layoutId, List<AddressBook> data){
        super(context, layoutId, data);
        mContext  = context;
        mLayoutId = layoutId;
        mData     = data;
        initParse();
    }

    public void initParse(){
        ParseObject.registerSubclass(AddressBook.class);
        ParseObject.registerSubclass(Contact.class);
        Parse.initialize(mContext, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        BookHolder holder = null;

        if(convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, parent, false);

            ImageView exitIcon = (ImageView) convertView.findViewById(R.id.exitViewIcon);
            TextView bookName  = (TextView) convertView.findViewById(R.id.bookNameView);
            DroidBook.setFontRoboto(bookName, mContext);
            holder = new BookHolder(exitIcon, bookName);
            convertView.setTag(holder);
        }else {
            holder = (BookHolder) convertView.getTag();
        }

        AddressBook book = mData.get(position);
        holder.getmNameView().setText(book.getBookName());
        return convertView;
    }

    /*Class for holding the views associated with an Address Book.*/
    public class BookHolder{
        private ImageView mExitIcon;
        private TextView mNameView;


        public BookHolder(ImageView icon, TextView name){
            mExitIcon = icon;
            mNameView = name;
            initParse();
        }

        public void initParse(){
            ParseObject.registerSubclass(AddressBook.class);
            ParseObject.registerSubclass(Contact.class);
            Parse.initialize(mContext, "kpVXSqTA4cCxBYcDlcz1gGJKPZvMeofiKlWKzcV3", "T4FqPFp0ufX4qs8rIUDL8EX8RSluB0wGX51ZpL12");
        }

        public ImageView getmExitIcon() {
            return mExitIcon;
        }

        public void setmExitIcon(ImageView mExitIcon) {
            this.mExitIcon = mExitIcon;
        }

        public TextView getmNameView() {
            return mNameView;
        }

        public void setmNameView(TextView mNameView) {
            this.mNameView = mNameView;
        }

    }
}
