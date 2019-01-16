package com.gmitmedia.ngelong;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;

import com.gmitmedia.helpers.Alert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by melvin on 05/10/2016.
 * <p>
 * Contains general functions
 */

public class Functions {

    public static Drawable setTint(Drawable drawable, int color) {
        final Drawable newDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(newDrawable, color);
        return newDrawable;
    }

    /**
     * Display an alert when no internet is detected
     *
     * @param activity
     */
    public static void noInternetAlert(FragmentActivity activity) {
        //error connecting
        Alert alert = new Alert();
        alert.DisplayText(activity.getString(R.string.no_internet_error_title), activity.getString(R.string.no_internet_error), activity.getString(R.string.Alert_accept), activity.getString(R.string.Alert_cancel), activity);
        alert.show(activity.getSupportFragmentManager(), activity.getString(R.string.no_internet_error));
    }

    public static void errorAlert(FragmentActivity activity, String error ) {
        //error connecting
        Alert alert = new Alert();
        alert.DisplayText(activity.getString(R.string.error_title), error, activity.getString(R.string.Alert_accept), activity.getString(R.string.Alert_cancel), activity);
        alert.show(activity.getSupportFragmentManager(), activity.getString(R.string.no_internet_error));
    }

    /**
     * This is a template to create a responsive HTML document for the webview. The content is then downloaded/stored locally.
     *
     * @param content - Content to show in <body></body>
     * @return - Html Document with content
     */
    public static String HTMLTemplate(String content) {
        return "<!doctype html> \n" +
                "<html> \n" +
                "<head> \n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">   \n" +
                "<script type=\"text/javascript\" src=\"https://platform.twitter.com/widgets.js\"></script>"+
                "</head>  \n" +
                "\n" +
                "<body> \n" +
                "<div class='content'>" + content + "</div>" +
                "</body> \n" +
                "\n" +
                "</html>\n" +
                "\n" +
                "<style>\n" +

                //font style
                "@font-face {" +
                "  font-family: MyFont;" +
                "  src: url('file:///android_asset/Roboto-Regular.tff')" +
                "}" +
                "body {" +
                " font-family: MyFont;" +
                " line-height: 150%;"+
                " font-size: medium;" +
                " text-align: justify;" +
                "}" +

                //video
                "\n" +
                ".content {\n" +
                "    padding:  1px 1px 1px 1px;\n" +
                "}" +
                ".note-video-clip{\n" +
                "    position: absolute;\n" +
                "    top: 0;\n" +
                "    left: 0;\n" +
                "    width: 100%;\n" +
                "    height: 100%;" +
                "}\n" +
                ".video_container {\n" +
                "    position: relative;\n" +
                "    width: 100%;\n" +
                "    height: 0;\n" +
                "    padding-bottom: 56.25%;\n" +
                "}" +
                "\n" +
//                "@media screen and (min-width: 500px) {\n" +
//                "  .note-video-clip{\n" +
//                "    width: 100%;\n" +
//                "  } \n" +
//                "}\n" +
//                "\n" +
//                "@media screen and (min-width: 750px) {\n" +
//                "  .note-video-clip{\n" +
//                "    width: 100%;\n" +
//                "    max-width: 400px;\n" +
//                "  } \n" +
                "}\n" +
                "img{\n" +
                "  display: block;\n" +
                "  width: 100%;\n" +
                "  height: auto   !important;\n" +
                "}\n" +
                "\n" +
//                "@media screen and (min-width: 500px) {\n" +
//                "  img{\n" +
//                "    width: 60%;\n" +
//                "  } \n" +
//                "}\n" +
//                "\n" +
//                "@media screen and (min-width: 750px) {\n" +
//                "  img{\n" +
//                "    width: 40%;\n" +
//                "    max-width: 400px;\n" +
//                "  } \n" +
//                "}\n" +
                "</style>";

    }


    static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    static String urlEncodeUTF8(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }


    public static void tintColorWidget(View view, int color, Context context) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable.mutate(), context.getResources().getColor(color));
        view.setBackgroundDrawable(wrappedDrawable);
    }
}
