package com.sailor.imaging.core.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.sailor.imaging.core.util.PickUtils;

/**
 * Created by felix on 2017/12/26 下午3:07.
 */

public class IMGPathDecoder extends IMGDecoder {

    private Context mContext;

    public IMGPathDecoder(Context context, Uri uri) {
        super(uri);
        this.mContext = context;
    }

    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        Uri uri = getUri();
        if (uri == null) {
            return null;
        }

        String path = PickUtils.getPath(mContext, uri);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        return BitmapFactory.decodeFile(path, options);
    }
}
