package ntou.project.djidrone.fragment;

import android.content.Intent;
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

import ntou.project.djidrone.DJIApplication;
import ntou.project.djidrone.MainActivity;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.define;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getName();
    private GridView gridViewMain;
    private List<GridItem> gridItemList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, "onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridViewMain = view.findViewById(R.id.gridViewMain);
        gridItemList = getList();
        gridViewMain.setAdapter(new GridViewAdapter(MainFragment.this, gridItemList));//setAdapter
        setListener();
        Log.d(TAG, "onViewCreated");
    }

    private void setListener() {
        gridViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent("fragment." + gridItemList.get(position).getName());
                ((MobileActivity) getActivity()).changeFragment(position + 1);
                Log.d(define.LOG_TAG, gridItemList.get(position).getName());
                Log.d(define.LOG_TAG, "activity:" + getActivity());
                Log.d(define.LOG_TAG, "context:" + getContext());
                setToast(position);
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
        newList.add(new GridItem("battery", R.drawable.bettery));
        newList.add(new GridItem("sensor", R.drawable.sensor_surround));
        newList.add(new GridItem("signal", R.drawable.signal));
        newList.add(new GridItem("controller", R.drawable.controller));
        newList.add(new GridItem("camera", R.drawable.camera));
        newList.add(new GridItem("setting", R.drawable.setting));
        return newList;
    }
}
