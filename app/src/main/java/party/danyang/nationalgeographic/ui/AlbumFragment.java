package party.danyang.nationalgeographic.ui;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.FragmentBigPicBinding;
import party.danyang.nationalgeographic.model.album.Picture;
import party.danyang.nationalgeographic.utils.PicassoHelper;
import party.danyang.nationalgeographic.utils.Utils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AlbumFragment extends Fragment {
    private static final String TAG = AlbumFragment.class.getSimpleName();

    private static final String PICTURES = "party.danyang.ng.af.pictures";
    private static final String INDEX = "party.danyang.ng.af.index";

    private AlbumActivity activity;
    private FragmentBigPicBinding binding;

    private List<Picture> pictures;
    private int index;
    protected CompositeSubscription mSubscriptions = new CompositeSubscription();

    public static AlbumFragment newInstance(ArrayList<Picture> pictures, int position) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(PICTURES, pictures);
        args.putInt(INDEX, position);
        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (AlbumActivity) context;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pictures = getArguments().getParcelableArrayList(PICTURES);
            index = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_big_pic, container, false);
        binding.imgTouch.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                binding.imgTouch.getViewTreeObserver().removeOnPreDrawListener(this);
                getActivity().supportStartPostponedEnterTransition();
                return true;
            }
        });
        RxView.clicks(binding.imgTouch).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                toggle();
            }
        });
        //检查读写权限
        RxView.longClicks(binding.imgTouch)
                .compose(RxPermissions.getInstance(activity).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {//有权限
                            showSaveImgDialog();
                        } else {//无权限
                            makeSnackBar(R.string.permission_denied, true);
                        }
                    }
                });
        return binding.getRoot();
    }


    private void toggle() {
        if (activity.mVisible) {
            activity.hide();
        } else {
            activity.show();
        }
    }

    private void showSaveImgDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.save_img);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                saveImg();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void saveImg() {
        mSubscriptions.add(Utils.saveImageAndGetPathObservable(activity, pictures.get(index).getUrl(),
                pictures.get(index).getAlbumid() + "_" + index)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Uri>() {
                    @Override
                    public void onCompleted() {
                        File appDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                        String msg = String.format(getString(R.string.save_in_file), appDir.getAbsolutePath());
                        makeSnackBar(msg, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        makeSnackBar(e.toString(), true);
                    }

                    @Override
                    public void onNext(Uri uri) {

                    }
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.setUrl(pictures.get(index).getUrl());
    }

    public View getSharedElement() {
        return binding.imgTouch;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptions != null) {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    public void onDestroyView() {
        PicassoHelper.getInstance(getContext()).cancelRequest(binding.imgTouch);
        super.onDestroyView();
    }

    private void makeSnackBar(String msg, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar.make(binding.getRoot(), msg, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG).show();
        }
    }

    private void makeSnackBar(int resId, boolean lengthShort) {
        if (binding != null && binding.getRoot() != null) {
            Snackbar.make(binding.getRoot(), resId, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG).show();
        }
    }
}
