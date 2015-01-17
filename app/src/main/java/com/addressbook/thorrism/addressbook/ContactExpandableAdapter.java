package com.addressbook.thorrism.addressbook;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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

        //Set the data fields of the child
        TextView contactName         = (TextView) convertView.findViewById(R.id.contactName);
        TextView contactAddress      = (TextView) convertView.findViewById(R.id.contactAddress);
        TextView contactCityStateZip = (TextView) convertView.findViewById(R.id.contactCityStateZip);

        contactName.setText(contact.getFirstName() + " " + contact.getLastName());
        contactAddress.setText(contact.getAddress());
        contactCityStateZip.setText(contact.getCity() + ", " + contact.getState() + " " + contact.getZipcode());

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
        listHeader.setTypeface(null, Typeface.BOLD);
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
