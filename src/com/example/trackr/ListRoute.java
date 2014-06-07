package com.example.trackr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.List;

public class ListRoute extends Activity {
	private List<data> rdata = null;
	private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);

		rdata = HomeActivity.getAllRoutes();
		list = (ListView) findViewById(R.id.listView);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l) {
				Toast.makeText(getApplicationContext(),
						"Click ListItem Number " + i, Toast.LENGTH_LONG).show();

				Intent intent = new Intent(getApplicationContext(),RouteInfoActivity.class);
				intent.putExtra("dataIndex", i);
				startActivity(intent);
			}
		});
		if (rdata != null) {
			updateRoutes();
		}
	}

	public void updateRoutes() {
		CustomAdapter adapter = new CustomAdapter(this, rdata);
		list.setAdapter(adapter);
	}

	public void refreshRoutes(View v) {
		rdata.add(rdata.get(0));

		// request server()
		// rdata = HomeActivity.getAllRoutes();
		updateRoutes();
	}

	public class CustomAdapter extends ArrayAdapter<data> {

		Context mContext;
		List<data> objects = null;

		public CustomAdapter(Context context, List<data> object) {
			super(context, R.layout.item_layout, object);
			this.mContext = context;
			this.objects = object;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			View view = inflater.inflate(R.layout.item_layout, parent, false);

			TextView s1 = (TextView) view.findViewById(R.id.itemFrom);
			TextView s2 = (TextView) view.findViewById(R.id.itemTo);

			s1.setText(objects.get(position).start);
			s2.setText(objects.get(position).destination);

			return view;
		}

	}
}