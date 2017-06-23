package com.accengage.samples.inbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.accengage.samples.R;
import com.accengage.samples.base.AccengageFragment;
import com.accengage.samples.firebase.Constants;
import com.accengage.samples.firebase.models.InboxMessage;
import com.accengage.samples.inbox.InboxNavActivity;
import com.accengage.samples.inbox.InboxUtils;
import com.accengage.samples.inbox.InboxViewHolder;
import com.ad4screen.sdk.Acc;
import com.ad4screen.sdk.Log;
import com.ad4screen.sdk.Message;


public class InboxMessageDetailFragment extends AccengageFragment {

    public static final String TAG = "InboxMsgDetailFrag ";

    private InboxMessage mMessage;

    private TextView mSender;
    private TextView mTitle;
    private TextView mBody;
    private WebView mWebView;
    private ImageView mIconView;
    private TextView mSentTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mMessage = ((InboxNavActivity) getActivity()).getClickedMessage();
    }

    @Override
    public void onCreatingView(View fragmentView) {
        super.onCreatingView(fragmentView);

        mSender = fragmentView.findViewById(R.id.inbox_msg_sender);
        mTitle = fragmentView.findViewById(R.id.inbox_msg_title);
        mBody = fragmentView.findViewById(R.id.inbox_msg_body);
        mWebView = fragmentView.findViewById(R.id.inbox_msg_webview);
        mIconView = fragmentView.findViewById(R.id.inbox_msg_sender_photo);
        mSentTime = fragmentView.findViewById(R.id.inbox_msg_sent_time);

        mSender.setText(mMessage.sender);
        mSentTime.setText(mMessage.getFormatedDateTime());
        mTitle.setText(mMessage.title);

        if (!TextUtils.isEmpty(mMessage.icon)) {
            InboxViewHolder.loadAndSetIcon(InboxMessageDetailFragment.this.getContext(), mIconView, mMessage.icon);
        }

        Message accMessage = mMessage.getAccMessage();
        accMessage.display(getContext(), new Acc.Callback<Message>() {
            @Override
            public void onResult(Message result) {
                Log.debug(TAG + "onResult display OK");

                if (mMessage.contentType.equals(Message.MessageContentType.Web.name())) {
                    Log.debug(TAG + "message with a web content");
                    if (mMessage.body != null) {
                        Log.debug(TAG + "message with a web body is not null " + mMessage.body);
                        mWebView.setVisibility(View.VISIBLE);
                        mWebView.setWebViewClient(new WebViewClient());
                        WebSettings webSettings = mWebView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        mWebView.loadUrl(mMessage.body);
                        mBody.setVisibility(View.GONE);
                    }
                } else {
                    Log.debug(TAG + "message with an other content: " + mMessage.contentType);
                    mWebView.setVisibility(View.GONE);
                    mBody.setText(mMessage.body);
                    mBody.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int error, String errorMessage) {
                Log.debug(TAG + "onError display KO");
            }
        });
    }

    @Override
    public String getViewName(Context context) {
        return "Message Details";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inbox_message_details;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.inbox_msg_actions, menu);

        // Hide actions according to the message (label, archive status, etc)
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int id = item.getItemId();
            if (id == R.id.action_inbox_archive) {
                if (mMessage.archived || mMessage.label.equals(Constants.Inbox.Messages.EXPIRED)) {
                    item.setVisible(false);
                }
            } else if (id == R.id.action_inbox_delete) {
                if (mMessage.label.equals(Constants.Inbox.Messages.TRASH)) {
                    item.setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_inbox_archive) {
            InboxUtils.archiveMessage(getContext(), mMessage);
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        } else if (i == R.id.action_inbox_delete) {
            InboxUtils.archiveMessage(getContext(), mMessage); // We need to archive the message to not obtain it any more from Accengage server
            InboxUtils.moveMessageTo(Constants.Inbox.Messages.TRASH, mMessage);
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        } else if (i == R.id.action_inbox_more) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}