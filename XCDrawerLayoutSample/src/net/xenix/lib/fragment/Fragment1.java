package net.xenix.lib.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Fragment1 extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Context context = getActivity();
		
		String[] items = new String[100];
		for ( int i = 0; i < items.length; i++ ) {
			items[i] = "Item " + i; 
		}
		
		ListView listView = new ListView(context);
		listView.setOnItemClickListener(mItemClickListener);
		listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items));
		listView.setBackgroundColor(Color.GRAY);
		
		return listView;
	}
	
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			Object item = adapter.getItemAtPosition(position);
			Toast.makeText(getActivity(), "OnItemClick : " + item.toString(), Toast.LENGTH_SHORT).show();
		}
	};
}
