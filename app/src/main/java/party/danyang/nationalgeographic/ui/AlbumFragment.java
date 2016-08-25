package party.danyang.nationalgeographic.ui;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import party.danyang.nationalgeographic.R;
import party.danyang.nationalgeographic.databinding.FragmentBigPicBinding;
import party.danyang.nationalgeographic.utils.SettingsModel;
import party.danyang.nationalgeographic.utils.Utils;
import party.danyang.nationalgeographic.utils.singleton.PicassoHelper;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AlbumFragment extends Fragment {
    private static final String TAG = AlbumFragment.class.getSimpleName();

    public static final String TAG_ALBUN_FRAGMENT = "tag.albunfragment";

    private static final String URLS = "party.danyang.ng.af.urls";
    private static final String INDEX = "party.danyang.ng.af.index";

    private AlbumActivity activity;
    private FragmentBigPicBinding binding;

    private List<String> urls;
    private int index;
    protected CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (AlbumActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            urls = getArguments().getStringArrayList(URLS);
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
                binding.imgTouch.requestFocus();
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
                            Utils.makeSnackBar(binding.getRoot(), R.string.permission_denied, true);
                        }
                    }
                });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        String url = urls.get(index);
        //如果是us的大图，直接缩小
        int length = SettingsModel.getAccelerateImageSize(activity);
        if (url.startsWith("http://yourshot.nationalgeographic.com/")) {
            url = url.replace("http://yourshot.nationalgeographic.com/", "http://ocgawl9z2.qnssl.com/") + "?imageMogr2/thumbnail/" + length + "x" + length;
        }
        if (SettingsModel.getAccelerate(activity)) {
            if (url.startsWith("http://pic01.bdatu.com/Upload/picimg/")) {
                url = url.replace("http://pic01.bdatu.com/Upload/picimg/", "http://ocgasl9gh.qnssl.com/") + "?imageMogr2/thumbnail/" + length + "x" + length;
            }
        }
        PicassoHelper.getInstance(binding.imgTouch.getContext()).load(url)
                .config(Bitmap.Config.ARGB_8888)
                .noFade()
                .priority(Picasso.Priority.HIGH)
                .tag(TAG_ALBUN_FRAGMENT)
                .into(binding.imgTouch, new Callback() {
                    @Override
                    public void onSuccess() {
                        binding.imgTouch.setZoom(1);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public void onDestroyView() {
        PicassoHelper.getInstance(getContext()).cancelRequest(binding.imgTouch);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscriptions != null) {
            mSubscriptions.unsubscribe();
        }
    }

    public static AlbumFragment newInstance(ArrayList<String> urls, int position) {
        Bundle args = new Bundle();
        args.putStringArrayList(URLS, urls);
        args.putInt(INDEX, position);
        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);
        return fragment;
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
        mSubscriptions.add(Utils.saveImgFromUrl(
                activity, urls.get(index), String.valueOf(urls.get(index).hashCode()))
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Uri>() {
                    @Override
                    public void onCompleted() {
                        File appDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                        String msg = String.format(getString(R.string.save_in_file), appDir.getAbsolutePath());
                        Utils.makeSnackBar(binding.getRoot(), msg, false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.makeSnackBar(binding.getRoot(), e.toString(), true);
                    }

                    @Override
                    public void onNext(Uri uri) {
                    }
                }));
    }

    public View getSharedElement() {
        return binding.imgTouch;
    }
}
