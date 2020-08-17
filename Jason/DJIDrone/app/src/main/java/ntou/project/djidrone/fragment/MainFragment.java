package ntou.project.djidrone.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.List;

import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.Define;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getName();
    private GridView gridViewMain;
    private List<GridItem> gridItemList;
    private String settingArray[];

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

        Log.d(TAG, "onViewCreated");
    }

    @Override
    public void onStart() {
        //寫在onCreate會失敗
        super.onStart();
        Log.d(TAG, "onStart");
        gridItemList = getList();
        gridViewMain.setAdapter(new GridViewAdapter(MainFragment.this, gridItemList));//setAdapter
        setListener();
    }

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
                Toast.makeText(getActivity(), "位置" + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setToast(int position) {
        Toast.makeText(getActivity(), gridItemList.get(position).getName(), Toast.LENGTH_SHORT).show();
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
}
