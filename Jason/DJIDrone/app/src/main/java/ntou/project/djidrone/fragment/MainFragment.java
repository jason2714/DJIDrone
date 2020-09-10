package ntou.project.djidrone.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.List;

import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.Define;
import ntou.project.djidrone.utils.OthersUtil;
import ntou.project.djidrone.utils.ToastUtil;
import ntou.project.djidrone.view.GridItem;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getName();
    private GridView gridViewMain;
    private List<GridItem> gridItemList;
    private String settingArray[];
    private GridViewAdapter mGridAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridViewMain = view.findViewById(R.id.gridViewMain);
        gridItemList = getList();
        mGridAdapter = new GridViewAdapter(MainFragment.this, gridItemList);
        gridViewMain.setAdapter(mGridAdapter);//setAdapter
        setListener();
        Log.d(TAG, "onViewCreated");
    }

//    @Override
//    public void onStart() {
//        //寫在onCreate會失敗
//        super.onStart();
//        Log.d(TAG, "onStart");
//    }

    private void setListener() {
        gridViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MobileActivity) getActivity()).changeFragment(position + 1);
                Log.d(Define.LOG_TAG, gridItemList.get(position).getName());
//                setToast(position);
            }
        });
        gridViewMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ToastUtil.showToast("位置" + position);
                return true;
            }
        });
    }

    public List<GridItem> getList() {
        List<GridItem> newList = new ArrayList<>();
        if (isAdded())//判斷是否attach到context了 不然getResource會fail
            settingArray = getResources().getStringArray(R.array.setting_array);
        else
            return newList;
        newList.add(new GridItem(settingArray[0], R.drawable.bettery));
        newList.add(new GridItem(settingArray[1], R.drawable.sensor_surround));
        newList.add(new GridItem(settingArray[2], R.drawable.signal));
        newList.add(new GridItem(settingArray[3], R.drawable.controller));
        newList.add(new GridItem(settingArray[4], R.drawable.camera));
        newList.add(new GridItem(settingArray[5], R.drawable.setting));
        return newList;
    }

    public void setGridLLayout(Size parentSize) {
//        View itemView = mGridAdapter.getView(0);
        int nRows = gridItemList.size() / gridViewMain.getNumColumns();
        int nCols = gridViewMain.getNumColumns();
        int widthSpace = (parentSize.getWidth() - (int) OthersUtil.convertDpToPixel(70 * nCols, getActivity())) / (nCols + 1);
        int heightSpace = (parentSize.getHeight() - (int) OthersUtil.convertDpToPixel(63 * nRows, getActivity())) / (nRows + 1);
        gridViewMain.setPadding(widthSpace, heightSpace, widthSpace, heightSpace);
        gridViewMain.setHorizontalSpacing(widthSpace);
        gridViewMain.setVerticalSpacing(heightSpace);
    }
}
