package ntou.project.djidrone.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ntou.project.djidrone.R;

public class GridViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<Setting> settingList = new ArrayList<>();
    private MainFragment context;

    public GridViewAdapter(MainFragment context,List<Setting> settingList) {
        this.settingList=settingList;
        this.context=context;
    }

    @Override
    public int getCount() {
        return settingList.size();
    }

    @Override
    public Object getItem(int position) {
        return settingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return settingList.get(position).getImageSrc();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            layoutInflater=context.getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.layout_fragment_main_grid_item, null);
        }
        Setting setting = settingList.get(position);
        TextView gridViewName = convertView.findViewById(R.id.gridViewName);
        gridViewName.setText(setting.getName());
        ImageView gridViewImage = convertView.findViewById(R.id.gridViewImage);
        gridViewImage.setImageResource(setting.getImageSrc());
       /* gridViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GridViewAdapter.this.remove(GridViewAdapter.this.getItem(position));
            }
        });*/
        return convertView;

    }

    private void remove(Object item) {
    }
//    AbsList/*View.LayoutParamslp=newAbsListView.LayoutParams(android.view.ViewGroup
//
//            .LayoutParams.MATCH_PARENT,(gridview 的高度(
//    这里gridview如果设置了wrapcontent 那么你要给他外面包裹一个父控件高度是matchparent然后传过来这个父控件 计算父控件的高度 的这样才知道到底高度是多少）
//            -缝隙的宽度*缝隙个数*/)/行数);

}