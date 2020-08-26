package ntou.project.djidrone.fragment;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import ntou.project.djidrone.utils.DJIApplication;
import ntou.project.djidrone.MobileActivity;
import ntou.project.djidrone.R;
import ntou.project.djidrone.utils.ToastUtil;

public class VideoSurfaceFragment extends Fragment implements VideoFeeder.VideoDataListener, TextureView.SurfaceTextureListener {
    private static final String TAG = VideoSurfaceFragment.class.getName();
    protected TextureView mVideoSurface = null;
    protected DJICodecManager mCodecManager = null;
    private boolean isSmallSurface;

    public VideoSurfaceFragment(boolean isSmallSurface) {
        this.isSmallSurface = isSmallSurface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.texture_video_surface, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoSurface = view.findViewById(R.id.video_surface);

        if (isSmallSurface) {
            mVideoSurface.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getContext() instanceof MobileActivity) {
                        ((MobileActivity) getContext()).triggerOnMapClick();
                    }
                }
            });
        }
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        initPreviewer();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        uninitPreviewer();
    }

    @Override
    public void onReceive(byte[] videoBuffer, int size) {
        if (mCodecManager != null) {
            mCodecManager.sendDataToDecoder(videoBuffer, size);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(getActivity(), surface, width, height);
//            mCodecManager = new DJICodecManager(MobileActivity.this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mCodecManager.onSurfaceSizeChanged(width, height, 0);
        Log.d(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void initListener() {
        mVideoSurface.setSurfaceTextureListener(this);
    }

    private void initPreviewer() {
        BaseProduct product = DJIApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            ToastUtil.showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(this);
            }
        }
    }

    private void uninitPreviewer() {
        if (null != DJIApplication.getCameraInstance()) {
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }
}