package ntou.project.djidrone.fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.ToastUtil;
import ntou.project.djidrone.view.GridItem;

public class GridViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<GridItem> gridItemList = new ArrayList<>();
    private MainFragment context;

    public GridViewAdapter(MainFragment context, List<GridItem> gridItemList) {
        this.gridItemList = gridItemList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return gridItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return gridItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return gridItemList.get(position).getImageSrc();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            layoutInflater = context.getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.layout_fragment_main_grid_item, null);
        }
        GridItem gridItem = gridItemList.get(position);
        TextView gridViewName = convertView.findViewById(R.id.gridViewName);
        gridViewName.setText(gridItem.getName());
        ImageView gridViewImage = convertView.findViewById(R.id.gridViewImage);
        gridViewImage.setImageResource(gridItem.getImageSrc());
       /* gridViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GridViewAdapter.this.remove(GridViewAdapter.this.getItem(position));
            }
        });*/
        return convertView;
    }

    public View getView(final int position) {
        layoutInflater = context.getLayoutInflater();
        View itemView = layoutInflater.inflate(R.layout.layout_fragment_main_grid_item, null);
        GridItem gridItem = gridItemList.get(position);
        TextView gridViewName = itemView.findViewById(R.id.gridViewName);
        gridViewName.setText(gridItem.getName());
        ImageView gridViewImage = itemView.findViewById(R.id.gridViewImage);
        gridViewImage.setImageResource(gridItem.getImageSrc());
        return itemView;
    }

//    public View getItemView() {
//        layoutInflater=context.getLayoutInflater();
//        View view = layoutInflater.inflate(R.layout.layout_fragment_main_grid_item, null);
//        GridItem gridItem = gridItemList.get(0);
//        return view;
//    }

    private void remove(Object item) {
    }

}