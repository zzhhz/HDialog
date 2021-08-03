package com.zzh.lib.dialog.load;

import android.content.Context;

/**
 * Created by ZZH on 2020/10/20.
 *
 * @Date: 2020/10/20
 * @Email: zzh_hz@126.com
 * @QQ: 1299234582
 * @Author: zzh
 * @Description:
 */
class SizeUtils {
    /**
     * dp转px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 屏幕尺寸的1/3
     *
     * @param ctx
     * @return 尺寸
     */
    public static int getDefaultLoadingDialogWidth(Context ctx) {
        return ctx.getResources().getDisplayMetrics().widthPixels / 3;
    }
}
