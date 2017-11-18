package in.aerl.googleapi;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class RouteAdapter extends ArrayAdapter<Routes> {
    public RouteAdapter(Context context, ArrayList<Routes> routes) {
        super(context, 0, routes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Routes routes = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.routerow, parent, false);
        }
        // Lookup view for data population
        TextView distance = (TextView) convertView.findViewById(R.id.distance);
        TextView path = (TextView) convertView.findViewById(R.id.routename);
        // Populate the data into the template view using the data object
        distance.setText(routes.distance);
        path.setText(Html.fromHtml(routes.path));
        return convertView;
    }
}