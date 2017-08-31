package com.ramotion.garlandview.example.profile;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ramotion.garlandview.example.R;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ProfileActivity extends AppCompatActivity {

    private static final String BUNDLE_NAME = "BUNDLE_NAME";
    private static final String BUNDLE_INFO = "BUNDLE_INFO";
    private static final String BUNDLE_STATUS = "BUNDLE_STATUS";
    private static final String BUNDLE_AVATAR_URL = "BUNDLE_AVATAR_URL";

    public static void start(Activity activity,
                             String url, String name, String info, String status,
                             View avatar, View card, View image, View list) {
        Intent starter = new Intent(activity, ProfileActivity.class);
        starter.putExtra(BUNDLE_NAME, name);
        starter.putExtra(BUNDLE_INFO, info);
        starter.putExtra(BUNDLE_STATUS, status);
        starter.putExtra(BUNDLE_AVATAR_URL, url);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Pair<View, String> p1 = Pair.create(avatar, activity.getString(R.string.transition_avatar_border));
            final Pair<View, String> p2 = Pair.create(card, activity.getString(R.string.transition_card));
            final Pair<View, String> p3 = Pair.create(image, activity.getString(R.string.transition_background));
            final Pair<View, String> p4 = Pair.create(list, activity.getString(R.string.transition_list));

            final ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, p1, p2, p3, p4);
            activity.startActivity(starter, options.toBundle());
        } else {
            activity.startActivity(starter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(R.id.tv_name)).setText(getIntent().getStringExtra(BUNDLE_NAME));
        ((TextView) findViewById(R.id.tv_info)).setText(getIntent().getStringExtra(BUNDLE_INFO));
        ((TextView) findViewById(R.id.tv_status)).setText(getIntent().getStringExtra(BUNDLE_STATUS));

        Glide.with(this)
                .load(getIntent().getStringExtra(BUNDLE_AVATAR_URL))
                .bitmapTransform(new CropCircleTransformation(this))
                .into((ImageView) findViewById(R.id.avatar));

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            final View headerImage = findViewById(R.id.header_image);
            final View headerInfo = findViewById(R.id.header_info);
            final View avatar = findViewById(R.id.avatar_border);
            final LinearLayout texts = (LinearLayout) findViewById(R.id.texts);

            final int avatarHOffset = getResources().getDimensionPixelSize(R.dimen.profile_avatar_h_offset);
            final int avatarVOffset = getResources().getDimensionPixelSize(R.dimen.profile_avatar_v_offset);
            final int avatarSize = getResources().getDimensionPixelSize(R.dimen.profile_avatar_size);
            final int textVMinOffset = getResources().getDimensionPixelSize(R.dimen.profile_texts_v_min_offset);
            final int textVMaxOffset = getResources().getDimensionPixelSize(R.dimen.profile_texts_v_max_offset);
            final int textVDiff = textVMaxOffset - textVMinOffset;
            final int header160 = getResources().getDimensionPixelSize(R.dimen.dp160);
            final int toolBarHeight;

            {
                final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                        new int[] { android.R.attr.actionBarSize });
                toolBarHeight = (int) styledAttributes.getDimension(0, 0) + getStatusBarHeight();
                styledAttributes.recycle();

                avatar.setPivotX(0);
                avatar.setPivotY(0);
                texts.setPivotX(0);
                texts.setPivotY(0);
            }

            final ArrayList<Float> textStart = new ArrayList<>();

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                final int diff = toolBarHeight + verticalOffset;
                final int y = diff < 0 ? header160 - diff : header160;
                headerImage.setBottom(y);
                headerInfo.setTop(y);

                final int totalScrollRange = appBarLayout.getTotalScrollRange();
                final float ratio = ((float)totalScrollRange + verticalOffset) / totalScrollRange;

                final int avatarHalf = avatar.getMeasuredHeight() / 2;
                final int avatarRightest = appBarLayout.getMeasuredWidth() / 2 - avatarHalf - avatarHOffset;
                final int avatarTopest = avatarHalf + avatarVOffset;

                avatar.setX(avatarHOffset + avatarRightest * ratio);
                avatar.setY(avatarVOffset - avatarTopest * ratio);
                avatar.setScaleX(0.5f + 0.5f * ratio);
                avatar.setScaleY(0.5f + 0.5f * ratio);

                if (textStart.isEmpty() && verticalOffset == 0) {
                    for (int i = 0; i < texts.getChildCount(); i++) {
                        textStart.add(texts.getChildAt(i).getX());
                        Log.d("D", String.format("i: %d, x: %f", i, textStart.get(i)));
                    }
                }

                texts.setY(textVMinOffset + textVDiff * ratio);
                texts.setScaleX(0.8f + 0.2f * ratio);
                texts.setScaleY(0.8f + 0.2f * ratio);
                for (int i = 0; i < textStart.size(); i++) {
                    texts.getChildAt(i).setX(Math.max(
                            avatarSize * 0.5f + avatarHOffset * 3,
                            textStart.get(i) * ratio));
                }

                Log.d("D", String.format("total: %d, current: %d, ratio: %f",
                        appBarLayout.getTotalScrollRange(), verticalOffset, ratio));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
