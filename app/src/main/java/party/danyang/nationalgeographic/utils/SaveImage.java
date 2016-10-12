package party.danyang.nationalgeographic.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.List;

import party.danyang.nationalgeographic.R;

/**
 * Created by dream on 16-9-17.
 */
public class SaveImage {

    public static long saveImg(Context context, View snackView, String name, String url) {
        if (!NetUtils.isConnected(context)) {
            Utils.makeSnackBar(snackView, R.string.offline, true);
            return -1;
        }
        //if wifionly and not in wifi
        if (SettingsModel.getWifiOnly(context) && !NetUtils.isWiFi(context)) {
            Utils.makeSnackBar(snackView, R.string.load_not_in_wifi_while_in_wifi_only, true);
            return -1;
        }
        File dir = new File(Environment.getExternalStorageDirectory(), "Lavender");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, name);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationUri(Uri.fromFile(file));
        request.setTitle(name);
        request.setDescription(file.getAbsolutePath());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("image/");
        request.allowScanningByMediaScanner();
        return downloadManager.enqueue(request);
    }
}
