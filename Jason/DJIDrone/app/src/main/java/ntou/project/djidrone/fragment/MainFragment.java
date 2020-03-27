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
import ntou.project.djidrone.define;

public class MainFragment extends Fragment {

    private GridView gridViewMain;
    private List<Setting> settingList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridViewMain = view.findViewById(R.id.gridViewMain);
        settingList = getList();
        gridViewMain.setAdapter(new GridViewAdapter(MainFragment.this,settingList));//setAdapter
        setListener();
    }
    private void setListener() {
        gridViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(define.LOG_TAG,settingList.get(position).getName());
                Log.d(define.LOG_TAG,"activity:"+getActivity());
                Log.d(define.LOG_TAG,"context:"+getContext());
                Toast.makeText(getActivity(),settingList.get(position).getName(), Toast.LENGTH_SHORT).show();
                setToast(position);
            }
        });
        gridViewMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),"位置"+position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setToast (int position){
        Toast.makeText(getActivity(),settingList.get(position).getName(), Toast.LENGTH_SHORT).show();
    }

    private List<Setting> getList(){
        settingList.add(new Setting("battery",R.drawable.bettery));
        settingList.add(new Setting("sensor",R.drawable.sensor_surround));
        settingList.add(new Setting("signal",R.drawable.signal));
        settingList.add(new Setting("controller",R.drawable.controller));
        settingList.add(new Setting("camera",R.drawable.camera));
        settingList.add(new Setting("setting",R.drawable.setting));
        return settingList;
    }
}
