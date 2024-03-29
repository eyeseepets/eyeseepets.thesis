package com.thesis.eyeseepets.Applications;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.thesis.eyeseepets.R;
import com.thesis.eyeseepets.Utilities.Globals;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.ui.messages.listeners.OnMessageClickListener;
import org.chat21.android.utils.IOUtils;

import static org.chat21.android.core.ChatManager._SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER;

public class AppContext extends Application {
    private String TAG;
    private static AppContext instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this); // add this
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //enable persistence must be made before any other usage of FirebaseDatabase instance.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        instance = this;

        TAG = "EYESEEPETS";

        initChatSDK();
    }

    public void initChatSDK() {
        // it creates the chat configurations
        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.chat_firebase_appId))
                        .firebaseUrl(getString(R.string.chat_firebase_url))
                        .storageBucket(getString(R.string.chat_firebase_storage_bucket))
                        .build();

        Globals.currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // assuming you have a login, check if the logged user (converted to IChatUser) is valid
//        if (currentUser != null) {
        if (Globals.currentUser != null) {
            IChatUser iChatUser = (IChatUser) IOUtils.getObjectFromFile(instance,
                    _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER);

//            IChatUser iChatUser = new ChatUser();
//            iChatUser.setId(currentUser.getUid());
//            iChatUser.setEmail(currentUser.getEmail());

            ChatManager.start(this, mChatConfiguration, iChatUser);
            Log.i(TAG, "chat has been initialized with success");

//            ChatManager.getInstance().initContactsSyncronizer();

            ChatUI.getInstance().enableGroups(true);

            ChatUI.getInstance().setOnMessageClickListener(new OnMessageClickListener() {
                @Override
                public void onMessageLinkClick(TextView message, ClickableSpan clickableSpan) {
                    String text = ((URLSpan) clickableSpan).getURL();

                    Uri uri = Uri.parse(text);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(browserIntent);
                }
            });

            // set on new conversation click listener
//            final IChatUser support = new ChatUser("support", "Chat21 Support");
            final IChatUser support = null;
            ChatUI.getInstance().setOnNewConversationClickListener(new OnNewConversationClickListener() {
                @Override
                public void onNewConversationClicked() {
                    if (support != null) {
                        ChatUI.getInstance().openConversationMessagesActivity(support);
                    } else {
                        Intent intent = new Intent(instance, ContactListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                        startActivity(intent);
                    }
                }
            });
        } else {
            Log.w(TAG, "chat can't be initialized because chatUser is null");
        }
    }
}
