package lava.retailcustomerclient.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import lava.retailcustomerclient.R;
import lava.retailcustomerclient.utils.AppInfoObject;

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private List<AppInfoObject> a;
    private static int i = 0;
    private Activity activity;

    public GridViewAdapter(Activity activity, Context c, List<AppInfoObject> appsList) {
        context = c;
        a = appsList;
        this.activity = activity;
    }

    public int getCount() {
        return a.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder
    {
        public ImageView imgViewIcon;
        public TextView txtViewName;
        public ImageView imgViewStar;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;

        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null)
        {
            view = new ViewHolder();
            convertView = inflater.inflate(R.layout.grid_row, null);

            view.txtViewName = (TextView) convertView.findViewById(R.id.textName);
            view.imgViewIcon = (ImageView) convertView.findViewById(R.id.imageIcon);
            view.imgViewStar = (ImageView) convertView.findViewById(R.id.imageStar);

            convertView.setTag(view);
        }
        else
        {
            view = (ViewHolder) convertView.getTag();
        }

        view.txtViewName.setText(a.get(position).appName);
        Picasso.with(context).load(a.get(position).iconUrl).into(view.imgViewIcon);

        view.imgViewStar.setImageResource(getRatingDrawable(a.get(position).rating));

        return convertView;
    }

    private int getRatingDrawable(float rating) {

        int id;
        if (rating > 4.5)
            id = R.drawable.stars_5;
        else if (rating > 4.0)
            id = R.drawable.stars_4_5;
        else if (rating > 3.5)
            id = R.drawable.stars_4;
        else if (rating > 3.0)
            id = R.drawable.stars_3_5;
        else if (rating > 2.5)
            id = R.drawable.stars_3;
        else if (rating > 2.0)
            id = R.drawable.stars_2_5;
        else if (rating > 1.5)
            id = R.drawable.stars_2;
        else if (rating > 1.0)
            id = R.drawable.stars_1_5;
        else
            id = R.drawable.stars_1;

        return id;
    }
}