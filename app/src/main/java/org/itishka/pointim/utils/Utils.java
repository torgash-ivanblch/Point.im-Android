package org.itishka.pointim.utils;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.itishka.pointim.R;
import org.itishka.pointim.widgets.spans.PostClickableSpan;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final String BASE_URL_STRING = "https://point.im/api/";
    public static final String AVATAR_URL_STRING = "https://i.point.im/";
    public static final String SITE_URL_STRING = "https://point.im/";
    public static final String BLOG_SITE_URL_TEMPLATE = "https://%s.point.im/blog";

    public static Uri getnerateSiteUri(String postId) {
        return Uri.parse(SITE_URL_STRING + postId);
    }

    public static Uri getnerateSiteUri(String postId, String commendId) {
        return Uri.parse(SITE_URL_STRING + postId + "#" + (commendId == null ? "" : commendId));
    }

    public static Uri getnerateBlogUri(String login) {
        return Uri.parse(String.format(BLOG_SITE_URL_TEMPLATE, login));
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static void showAvatarByLogin(Context context, String login, ImageView imageView) {
        showAvatar(context, "http://point.im/avatar/" + login + "/80", imageView);
    }

    public static void showAvatar(Context context, String avatar, ImageView imageView) {
        if (avatar == null) {
            imageView.setImageResource(R.drawable.ic_launcher);
            return;
        }
        try {
            URL url;
            if (avatar.contains("/"))
                url = new URL(avatar);
            else
                url = new URL(new URL(AVATAR_URL_STRING), "/a/80/" + avatar);
            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(context.getResources().getColor(R.color.form_background))
                    .borderWidthDp(1)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();
            Picasso.with(context)
                    .load(url.toString())
                    .error(R.drawable.ic_action_internet)
                    .placeholder(R.drawable.ic_launcher)
                    .fit()
                    .transform(transformation)
                    .into(imageView);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void showAvatar(Context context, String avatar, ActionBar actionBar) {
        if (avatar == null) {
            actionBar.setLogo(R.drawable.ic_launcher);
            return;
        }
        try {
            URL url;
            if (avatar.contains("/"))
                url = new URL(avatar);
            else
                url = new URL(new URL(AVATAR_URL_STRING), "/a/80/" + avatar);
            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(context.getResources().getColor(R.color.form_background))
                    .borderWidthDp(1)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();
            Picasso.with(context)
                    .load(url.toString())
                    .error(R.drawable.ic_action_internet)
                    .placeholder(R.drawable.ic_launcher)
                    .transform(transformation)
                    .into(new PicassoActionBarTarget(actionBar));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static class PicassoActionBarTarget implements Target {

        private final ActionBar mActionBar;

        public PicassoActionBarTarget(ActionBar actionBar) {
            mActionBar = actionBar;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            BitmapDrawable drawable = new BitmapDrawable(mActionBar.getThemedContext().getResources(), bitmap);
            mActionBar.setLogo(drawable);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mActionBar.setLogo(errorDrawable);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            mActionBar.setLogo(placeHolderDrawable);
        }
    }

    public static void addLinks(TextView view) {
        android.text.util.Linkify.addLinks(view, android.text.util.Linkify.ALL);
        view.setMovementMethod(null);
    }

    public static Spannable addLinks(String text) {
        Spannable spannable = new SpannableString(text);
        android.text.util.Linkify.addLinks(spannable, android.text.util.Linkify.ALL);
        return spannable;
    }


    static final Pattern nickPattern = Pattern.compile("(?<=^|[:(>\\s])@([\\w-]+)");

    public static Spannable markNicks(Spannable text) {
        Matcher m = nickPattern.matcher(text);
        while (m.find()) {
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            text.setSpan(b, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            URLSpan urlSpan = new URLSpan(getnerateBlogUri(m.group(1)).toString());
            text.setSpan(urlSpan, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return text;
    }

    static final Pattern postNumberPattern = Pattern.compile("(?<=^|[:(>\\s])#(\\w+)(?>/(\\d+))?");

    public static Spannable markPostNumbers(Spannable text) {
        Matcher m = postNumberPattern.matcher(text);
        while (m.find()) {
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            text.setSpan(b, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            PostClickableSpan span = new PostClickableSpan(m.group(1), m.group(2));
            //URLSpan urlSpan = new URLSpan(getnerateSiteUri(m.group(1), m.group(2)).toString());
            text.setSpan(span, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return text;
    }
}