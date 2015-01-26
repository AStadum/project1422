package com.addressbook.thorrism.addressbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Lucas Crawford on 1/16/2015.
 */
public class ContactExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listHeaders;
    private HashMap<String, Contact> listData;

    public ContactExpandableAdapter(Context context, List<String> listHeader, HashMap<String, Contact> listData){
        this.context    = context;
        this.listHeaders = listHeader;
        this.listData   = listData;
    }

    /**
     * Returns the Contact for a specific header in the ExpandableListView
     * @param groupPosition  - the header clicked
     * @param childPosition - the position of the child we want (always 0 since there is only 1 child)
     * @return
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listData.get(this.listHeaders.get(groupPosition));
    }

    public void addCallListenener(ImageView view, final Contact contact){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + contact.getNumber()));
                context.startActivity(callIntent);
            }
        });

    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final Contact contact = (Contact) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contact_expandable_item, null);
        }

        //Grab the views for each contact item
        TextView contactName         = (TextView) convertView.findViewById(R.id.contactName);
        TextView contactAddress      = (TextView) convertView.findViewById(R.id.contactAddress);
        TextView contactCityStateZip = (TextView) convertView.findViewById(R.id.contactCityStateZip);
        TextView contactEmail        = (TextView) convertView.findViewById(R.id.contactEmail);
        TextView contactNumber       = (TextView) convertView.findViewById(R.id.contactNumber);
        ImageView phoneIcon          = (ImageView) convertView.findViewById(R.id.callContactView);
        phoneIcon.setVisibility(View.GONE);

        //Set the values for the views from the contacts from their information
        if(!contact.getFirstName().equals("")) {
            contactName.setText(contact.getFirstName() + " " + contact.getLastName());
            contactName.setVisibility(View.VISIBLE);
        }
        if(!contact.getAddress().equals("")) {
            contactAddress.setText(contact.getAddress());
            contactAddress.setVisibility(View.VISIBLE);
        }
        else
            contactAddress.setVisibility(View.GONE);

        if(contact.getCity().equals(""))
            contactCityStateZip.setText(contact.getState() + " " + contact.getZipcode());

        if(contact.getState().equals(""))
            contactCityStateZip.setText(contact.getCity() + " " + contact.getZipcode());

        if(!contact.getCity().equals("") && !contact.getState().equals("") && !contact.getZipcode().equals(""))
            contactCityStateZip.setText(contact.getCity() + ", " + contact.getState() + " " + contact.getZipcode());

        if(contact.getCity().equals("") && contact.getState().equals(""))
            contactCityStateZip.setText(contact.getZipcode());

        if(!contact.getEmail().equals("")) {
            contactEmail.setText(contact.getEmail());
            contactEmail.setVisibility(View.VISIBLE);
        }
        else contactEmail.setVisibility(View.GONE);

        if(!contact.getNumber().equals("")) {
            contactNumber.setText(contact.getNumber());
            phoneIcon.setVisibility(View.VISIBLE);
            contactNumber.setVisibility(View.VISIBLE);
        }
        else
            contactNumber.setVisibility(View.GONE);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listHeaders.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listHeaders.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contact_expandable_header, null);
        }

        TextView listHeader = (TextView) convertView.findViewById(R.id.contactHeader);
        DroidBook.setFontRoboto(listHeader, this.context);
        listHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
