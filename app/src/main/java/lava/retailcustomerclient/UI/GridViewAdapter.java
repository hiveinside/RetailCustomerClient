package lava.retailcustomerclient.ui;

import android.app.Activity;
import android.content.Context;
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
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;

        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null)
        {
            view = new ViewHolder();
            convertView = inflater.inflate(R.layout.grid_row, null);

            view.txtViewName = (TextView) convertView.findViewById(R.id.textView1);
            view.imgViewIcon = (ImageView) convertView.findViewById(R.id.imageView1);

            convertView.setTag(view);
        }
        else
        {
            view = (ViewHolder) convertView.getTag();
        }

        view.txtViewName.setText(a.get(position).AppName);
        Picasso.with(context).load(a.get(position).iconUrl).into(view.imgViewIcon);

        return convertView;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getViewOld(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            //   imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //   imageView.setPadding(1, 1, 1, 1);
        } else {
            imageView = (ImageView) convertView;
        }



        try {
            //URL url = new URL("http://192.168.43.1:8080/sdcard/AppsShare/amazon.bmp");
            //URL url = new URL("http://192.168.34.1:8080/?getIcon");
            //URL url = new URL("http://amherstburg.com/wp-content/uploads/2015/10/104-Fescue-24-480x480.bmp");
            //Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            //Picasso.with(context).load("http://192.168.43.1:8080/sdcard/AppsShare/amazon.bmp").into(imageView);
            Picasso.with(context).load(a.get(position).iconUrl).into(imageView);

            //imageView.setImageBitmap(bmp);

        } catch (Exception e) {

        }


        //imageView.setImageResource(mThumbIds[position]);


        return imageView;
    }
}