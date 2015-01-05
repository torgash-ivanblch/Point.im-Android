package org.itishka.pointim;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.itishka.pointim.api.ConnectionManager;


public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private ImageView avatar;
        private ImageButton logout;
        private TextView name;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
            name = (TextView) rootView.findViewById(R.id.login);
            name.setText(ConnectionManager.getInstance().loginResult.login);

            avatar = (ImageView) rootView.findViewById(R.id.avatar);
            Utils.showAvatarByLogin(getActivity(), ConnectionManager.getInstance().loginResult.login, avatar);

            logout = (ImageButton) rootView.findViewById(R.id.action_logout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askLogout();
                }
            });


            TextView version = (TextView) rootView.findViewById(R.id.version);
            version.setText("Version: "+BuildConfig.VERSION_NAME);

            rootView.findViewById(R.id.google_play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.itishka.pointim"));
                    getActivity().startActivity(browserIntent);
                }
            });
            rootView.findViewById(R.id.github).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Tishka17/Point.im-Android/"));
                    getActivity().startActivity(browserIntent);
                }
            });

            ImageView avatarTishka17 = (ImageView) rootView.findViewById(R.id.avatar_tishka17);
            Utils.showAvatarByLogin(getActivity(), "Tishka17", avatarTishka17);
            avatarTishka17.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tishka17.point.im/blog"));
                    getActivity().startActivity(browserIntent);
                }
            });

            ImageView avatarArts = (ImageView) rootView.findViewById(R.id.avatar_arts);
            Utils.showAvatarByLogin(getActivity(), "arts", avatarArts);
            avatarArts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://arts.point.im/blog"));
                    getActivity().startActivity(browserIntent);
                }
            });
            return rootView;
        }

        private void askLogout() {
            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title("Really logout?")
                    .positiveText(android.R.string.ok)
                    .negativeText("Cancel")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            ConnectionManager.getInstance().resetAuthorization(getActivity());
                            avatar.setVisibility(View.GONE);
                            logout.setVisibility(View.GONE);
                            name.setText("<logged out>");
                        }
                    })
                    .build();
            dialog.show();
        }
    }
}