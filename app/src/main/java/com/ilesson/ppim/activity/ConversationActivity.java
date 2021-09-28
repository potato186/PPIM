package com.ilesson.ppim.activity;

import static com.ilesson.ppim.activity.AddressActivity.SET_ADDRESS_SUCCESS;
import static com.ilesson.ppim.activity.AvatarActivity.MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.ExchangeActivity.SET_ADDRESS_SUCCESS_TO_USE;
import static com.ilesson.ppim.activity.FriendDetailActivity.USER_ID;
import static com.ilesson.ppim.activity.InvoiceActivity.COMPANY_MEDIUM;
import static com.ilesson.ppim.activity.InvoiceActivity.COMPANY_NAME;
import static com.ilesson.ppim.activity.InvoiceActivity.COMPANY_NUM;
import static com.ilesson.ppim.activity.InvoiceActivity.EMAIL_NAME;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_CANCEL;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_COMPANY;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_DATA;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_ELECT;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_MODIFY;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_MODIFY_SUCCESS;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_PAPER;
import static com.ilesson.ppim.activity.InvoiceActivity.INVOICE_PERSON;
import static com.ilesson.ppim.activity.InvoiceActivity.PERSON_MEDIUM;
import static com.ilesson.ppim.activity.InvoiceActivity.PERSON_NAME;
import static com.ilesson.ppim.activity.LoginActivity.USER_PHONE;
import static com.ilesson.ppim.activity.PayScoreActivity.PAY_DECS;
import static com.ilesson.ppim.activity.PayScoreActivity.PAY_MONEY;
import static com.ilesson.ppim.activity.SettingActivity.XUNFEI;
import static com.ilesson.ppim.activity.VoiceTxtActivity.CHATMODE;
import static com.ilesson.ppim.custom.MyExtensionModule.shopGroup;
import static com.ilesson.ppim.view.SwitchButton.PLAY_TTS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechUtility;
import com.ilesson.ppim.IlessonApp;
import com.ilesson.ppim.R;
import com.ilesson.ppim.custom.ComposeMessage;
import com.ilesson.ppim.db.ConversationDao;
import com.ilesson.ppim.db.GroupUserDao;
import com.ilesson.ppim.db.PPUserDao;
import com.ilesson.ppim.entity.AddressInfo;
import com.ilesson.ppim.entity.BaseCode;
import com.ilesson.ppim.entity.ConversationInfo;
import com.ilesson.ppim.entity.DeleteFriend;
import com.ilesson.ppim.entity.GroupBase;
import com.ilesson.ppim.entity.GroupInfo;
import com.ilesson.ppim.entity.HideProgress;
import com.ilesson.ppim.entity.InvoiceInfo;
import com.ilesson.ppim.entity.ModifyGroupName;
import com.ilesson.ppim.entity.ModifyGroupNike;
import com.ilesson.ppim.entity.ModifyUserNike;
import com.ilesson.ppim.entity.NoteInfo;
import com.ilesson.ppim.entity.PPUserInfo;
import com.ilesson.ppim.entity.PayOrder;
import com.ilesson.ppim.entity.PaySuccess;
import com.ilesson.ppim.entity.PublishNote;
import com.ilesson.ppim.entity.ResetGroupName;
import com.ilesson.ppim.entity.ShowProgress;
import com.ilesson.ppim.entity.SmartOrder;
import com.ilesson.ppim.entity.WaresOrder;
import com.ilesson.ppim.fragment.ConversationFragment;
import com.ilesson.ppim.service.FavoriteHelper;
import com.ilesson.ppim.utils.BigDecimalUtil;
import com.ilesson.ppim.utils.Constants;
import com.ilesson.ppim.utils.IMUtils;
import com.ilesson.ppim.utils.PPConfig;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.utils.PlayerUtils;
import com.ilesson.ppim.utils.SPUtils;
import com.ilesson.ppim.utils.TTSHelper;
import com.ilesson.ppim.utils.TextUtil;
import com.ilesson.ppim.utils.Tool;
import com.ilesson.ppim.view.DragView;
import com.ilesson.ppim.view.IfeyVoiceWidget1;
import com.ilesson.ppim.view.RoundImageView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.data.AudioRecordDataSource;
import com.tencent.aai.auth.AbsCredentialProvider;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.config.ClientConfiguration;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.listener.AudioRecognizeTimeoutListener;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;
import com.tencent.aai.model.type.AudioRecognizeConfiguration;
import com.tencent.aai.model.type.AudioRecognizeTemplate;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.functions.Consumer;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;

@ContentView(R.layout.conversation)
public class ConversationActivity extends BaseActivity implements RongIM.LocationProvider, RongIM.ConversationBehaviorListener {
    public static String title;
    @ViewInject(R.id.title_name)
    private TextView titleTextView;
    @ViewInject(R.id.note_text)
    private TextView noteTextView;
    public String groupName;
    public String mTargetId;
    @ViewInject(R.id.menu)
    private View menu;
    @ViewInject(R.id.note_layout)
    public View noteLayout;
    @ViewInject(R.id.play_tts)
    public View playView;
    @ViewInject(R.id.shop_car_layout)
    public View shopCarView;
    @ViewInject(R.id.order_num)
    public View shopCarNum;
    @ViewInject(R.id.address_layout)
    public View addressLayout;
    @ViewInject(R.id.floatBtn)
    public DragView floatBtn;
    @ViewInject(R.id.request_text)
    public TextView requestText;
    @ViewInject(R.id.help_text)
    public TextView helpTextView;
    @ViewInject(R.id.tts)
    public TextView ttsView;
    @ViewInject(R.id.to_pay)
    public TextView toPay;
    @ViewInject(R.id.to_set_address)
    public TextView toSetAddress;
    @ViewInject(R.id.username)
    public TextView userName;
    @ViewInject(R.id.address_view)
    public TextView addressView;
    @ViewInject(R.id.phone_view)
    public TextView phoneView;
    @ViewInject(R.id.result_text)
    public TextView resultText;
    @ViewInject(R.id.wares_name)
    public TextView waresName;
    @ViewInject(R.id.num)
    public TextView waresNum;
    @ViewInject(R.id.unit_price)
    public TextView unitPrice;
    @ViewInject(R.id.express_fee_price)
    public TextView express;
    @ViewInject(R.id.all_price)
    public TextView allPrice;
    @ViewInject(R.id.tts_modify)
    public TextView ttsModify;
    @ViewInject(R.id.wares_price)
    public TextView waresPrice;
    @ViewInject(R.id.wares_info)
    public TextView waresInfo;
    @ViewInject(R.id.wares_quantity)
    public TextView waresQuantity;
    @ViewInject(R.id.invoice_type)
    public TextView invoiceType;
    @ViewInject(R.id.voice_type)
    public TextView invoiceType1;
    @ViewInject(R.id.title_type)
    public TextView invoiceTitleType;
    @ViewInject(R.id.title_type_name)
    public TextView invoiceTitleName;
    @ViewInject(R.id.company_num)
    public TextView invoiceNum;
    @ViewInject(R.id.invoice_price)
    public TextView invoicePrice;
    @ViewInject(R.id.invoice_email)
    public TextView invoiceEmail;
    @ViewInject(R.id.result_image)
    public ImageView resultImageView;
    @ViewInject(R.id.wares_img)
    public RoundImageView roundImageView;
    @ViewInject(R.id.shop_layout)
    public View shopLayout;
    @ViewInject(R.id.wares_intro_view)
    public View waresIntroView;
    @ViewInject(R.id.tax_num)
    public View taxNumLayout;
    @ViewInject(R.id.show_content)
    public View showContent;
    @ViewInject(R.id.voice_layout)
    public View voiceLayout;
    @ViewInject(R.id.invoice_layout)
    public View invoiceLayout;
    @ViewInject(R.id.no_invoice)
    public View noInvoiceLayout;
    @ViewInject(R.id.email_layout)
    public View emailLayout;
    private TTSHelper ttsHelper;
    private boolean playTts;
    public String currentKey;
    private String helpText;
    private int screenWidth;
    boolean isFromPush = false;
    private String groupNote="";

    private IMUtils imUtils;
    private ConversationFragment conversationFragment;
    public IfeyVoiceWidget1 ifeyBtn;
    private static final String FLOATX = "floatx22";
    private static final String FLOATY = "floaty22";
    public static final String MARKET_SERVER = "market_server";
    /**
     * 会话类型
     */
    private Conversation.ConversationType mConversationType;
    public static final int PAY_SUCCESS = 99;
    public static final int PAY_FIAL = 89;
    private PlayerUtils playerUtils;
    private boolean xunfei = true;
    private int currentRequestId;
    private InvoiceInfo invoiceInfo;
    private List<InvoiceInfo> invoiceInfos;
    private SmartOrder smartOrder;
    private boolean isOwner;
    private GroupUserDao groupUserDao;
    private ConversationDao conversationDao;
    public void onEventMainThread(Conversation conversation) {
        if (!isFinishing()) {
            finish();
        }
    }

    public void onEventMainThread(UIMessage uiMessage) {
//        MessageContent messageContent = uiMessage.getMessage().getContent();
//        Intent intent = new Intent(this, ForwadSelectActivity.class);
//        intent.putExtra("msg",messageContent);
//        startActivity(intent);
    }

    private int groupSize;
    private int themeId;

    public void showShopTheme(boolean state) {
        if (state) {
            themeId = R.style.AppTheme_shop;
        } else {
            themeId = R.style.AppTheme_white;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", themeId);
    }

    @Event(R.id.shop_car_layout)
    private void shop_car_view(View v) {
        Log.d(TAG, "shop_car_view: ");
        shopCarEvent();
    }

    @Event(R.id.note_layout)
    private void noteLayout(View v) {
        GroupNoteActivity.launch(this,mTargetId,groupNote,isOwner);
        SPUtils.put(mTargetId+groupNote,"");
    }

    @Event(R.id.modify)
    private void modify(View v) {
        setInvoice(invoiceInfo);
    }

    @Event(R.id.no_invoice)
    private void no_invoice(View v) {
        setInvoice(null);
    }

    @Event(R.id.play_tts)
    private void play_tts(View v) {
        playTTsEvent();
    }

    @Event(R.id.back_btn)
    private void back_btn(View v) {
        out();
    }

    @Event(R.id.to_pay)
    private void to_pay(View v) {
        if (null != api && null != req) {
            api.sendReq(req);
        }
    }

    @Event(R.id.close)
    private void close(View v) {
        closeVoice();
    }

    @Event(R.id.to_set_address)
    private void to_set_address(View v) {
        startActivityForResult(new Intent(ConversationActivity.this, AddressActivity.class), 0);
    }

    @Event(R.id.show_content)
    private void show_content(View v) {
        if (currentOrder != null && !TextUtils.isEmpty(currentOrder.getLink())) {
            Intent intent1 = new Intent(ConversationActivity.this, PWebActivity.class);
            intent1.putExtra(PWebActivity.URL, currentOrder.getLink());
            startActivity(intent1);
        }
    }

    @Event(R.id.menu)
    private void menu(View v) {
        if (mConversationType == Conversation.ConversationType.GROUP) {
            if(null==groupInfo)return;
            Intent intent = new Intent(this, ChatInfoActivity.class);
            intent.putExtra(ChatInfoActivity.GROUP_ID, mTargetId);
            intent.putExtra(ChatInfoActivity.GROUP_NAME, groupInfo.getName());
            intent.putExtra(ChatInfoActivity.GROUP_ICON, groupIcon);
//                intent.putExtra(ChatInfoActivity.GROUP_INFO, groupBase);
            intent.putExtra(ChatInfoActivity.NIKE_NAME, nikeName);
            intent.putExtra(GroupNoteActivity.GROUP_NOTE, groupNote);
            intent.putExtra(ChatInfoActivity.GROUP_TAG, groupInfo.getTag());
            intent.putExtra(ChatInfoActivity.ISOWNER, isOwner);
            startActivityForResult(intent, 0);
        } else {
            Intent intent = new Intent(this, UserSttingActivity.class);
            intent.putExtra(USER_ID, mTargetId);
            startActivity(intent);
        }
    }
    public static final String TARGET_ID = "targetId";
    public static final String TARGET_NAME = "target_name";
    public static final String MESSAGE_ID = "message_id";
    public int messageId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupUserDao = new GroupUserDao();
        conversationDao = new ConversationDao();
        Log.d(TAG, "onCreate:currentActivity " + getCurrentActivity());
        setCurrentActivity(this);
        EventBus.getDefault().register(this);
        screenWidth = PPScreenUtils.getScreenWidth(this);
        setStatusBarLightMode(this, true);
        playerUtils = new PlayerUtils();
        playerUtils.initPlayer(this);
        Intent intent = getIntent();
        mTargetId = intent.getData().getQueryParameter(TARGET_ID);
        title = intent.getData().getQueryParameter("title");
        xunfei = SPUtils.get(SettingActivity.VOICE_NAME, XUNFEI).equals(XUNFEI) ? true : false;
        conversationFragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_conversation);

        init();
        IntentFilter intentFilter = new IntentFilter(FINISH_CURRENT);
        registerReceiver(receiver, intentFilter);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (!aBoolean) {
                    finish();
                }
            }
        });
        RongIM.getInstance().getConversation(mConversationType, mTargetId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if(null==conversation)return;
                ConversationInfo conversationInfo = new ConversationInfo();
                conversationInfo.setConversationTitle(title);
                conversationInfo.setTargetId(mTargetId);
                conversationInfo.setType(mConversationType.getValue());
                conversationInfo.setPortraitUrl(conversation.getPortraitUrl());
                conversationInfo.setDate(System.currentTimeMillis());
                conversationDao.update(conversationInfo);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
        RongIM.getInstance().getLatestMessages(mConversationType, mTargetId, 20, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
        RongIM.setLocationProvider(this);
        RongIM.setConversationBehaviorListener(this);
        MessageItemLongClickAction actionListener = new MessageItemLongClickAction.Builder().title("转发").actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
            @Override
            public boolean onMessageItemLongClick(Context context, UIMessage uiMessage) {
//                EventBus.getDefault().post(uiMessage);
                MessageContent messageContent = uiMessage.getMessage().getContent();
                Intent intent = new Intent(ConversationActivity.this, ForwadSelectActivity.class);
                intent.putExtra("msg", messageContent);
                startActivity(intent);
                return true;
            }
        }).showFilter(new MessageItemLongClickAction.Filter() {
            @Override
            public boolean filter(UIMessage uiMessage) {
                MessageContent messageContent = uiMessage.getMessage().getContent();
                if (messageContent instanceof TextMessage) {
                    return true;
                } else if (messageContent instanceof ImageMessage) {
                    return true;
                } else if (messageContent instanceof FileMessage) {
                    return true;
                } else if (messageContent instanceof ComposeMessage) {
                    return true;
                } else if (messageContent instanceof LocationMessage) {
                    return true;
                }
                return false;
            }
        }).build();
        MessageItemLongClickAction favListener = new MessageItemLongClickAction.Builder().title("收藏").actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
            @Override
            public boolean onMessageItemLongClick(Context context, UIMessage uiMessage) {
//                FavMessage favMessage = new FavMessage();
//                favMessage.setUiMessage(uiMessage);
//                EventBus.getDefault().post(favMessage);
                MessageContent messageContent = uiMessage.getMessage().getContent();
                FavoriteHelper helper = new FavoriteHelper();
                Gson gson = new Gson();
                if (messageContent instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) messageContent;
                    NoteInfo note = new NoteInfo();
                    note.setType(FavoriteHelper.TYPE_TEXT);
                    note.setText(textMessage.getContent());
                    helper.post(gson.toJson(note), null);
                    return true;
                } else if (messageContent instanceof ImageMessage) {
                    ImageMessage message = (ImageMessage) messageContent;
                    NoteInfo note = new NoteInfo();
                    note.setType(FavoriteHelper.TYPE_IMAGE);
                    String url = message.getMediaUrl().toString();
                    note.setUrl(url);
                    helper.post(gson.toJson(note), null);
                    return true;
                } else if (messageContent instanceof FileMessage) {
                    FileMessage message = (FileMessage) messageContent;
                    NoteInfo note = new NoteInfo();
                    note.setType(FavoriteHelper.TYPE_FILE);
                    String url = message.getMediaUrl().toString();
                    note.setUrl(url);
                    note.setSize(message.getSize());
                    helper.post(gson.toJson(note), null);
                    return true;
                } else if (messageContent instanceof ComposeMessage) {
                    return true;
                } else if (messageContent instanceof LocationMessage) {
                    LocationMessage message = (LocationMessage) messageContent;
                    NoteInfo note = new NoteInfo();
                    note.setType(FavoriteHelper.TYPE_LOCATION);
                    note.setAddress(message.getPoi());
                    note.setLongitude(message.getLng());
                    note.setLatitude(message.getLat());
                    helper.post(gson.toJson(note), null);
                    return true;
                }
                return true;
            }
        }).showFilter(new MessageItemLongClickAction.Filter() {
            @Override
            public boolean filter(UIMessage uiMessage) {
                MessageContent messageContent = uiMessage.getMessage().getContent();
                if (messageContent instanceof TextMessage || messageContent instanceof ImageMessage
                        || messageContent instanceof FileMessage || messageContent instanceof ComposeMessage
                        || messageContent instanceof LocationMessage) {
                    return true;
                } else if (messageContent instanceof ImageMessage) {
                    return true;
                } else if (messageContent instanceof FileMessage) {
                    return true;
                } else if (messageContent instanceof ComposeMessage) {
                    return true;
                } else if (messageContent instanceof LocationMessage) {
                    return true;
                }
                return false;
            }
        }).build();

        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(actionListener, 0);
        if (!TextUtils.isEmpty(mTargetId)) {
            if (mTargetId.contains("market")) {
                playTts = SPUtils.get(PLAY_TTS, true);
                showPlayState();
                request("", false);
                handler.sendEmptyMessageDelayed(0, 200);
                groupName = intent.getData().getQueryParameter("title");
                SPUtils.put(MARKET_SERVER, groupName);
            } else {
                voiceLayout.setVisibility(View.GONE);
            }
        }

        floatBtn.setOnPressListener(new DragView.OnPressListener() {
            @Override
            public void onPressUp(boolean isDrag) {
                if (!isDrag) {

                }
            }

            @Override
            public void onPressDown() {
                if (recording) {
                    stop(false);
                } else {
                    toSpeech();
                }
            }

            @Override
            public void onDoubleClick() {

            }
        });
        floatBtn.setOnLocationListener(new DragView.OnLocationListener() {
            @Override
            public void onLocation(int l, int t) {
                SPUtils.put(FLOATX, l);
                SPUtils.put(FLOATY, t);
            }
        });
        final EditText editTag = findViewById(R.id.editTag);
        editTag.postDelayed(new Runnable() {
            @Override
            public void run() {
                editTag.requestFocus();
            }
        }, 500);
        setListenerToRootView();
        initSpeech();
    }

    private boolean show;

    private void setListenerToRootView() {
        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > PPScreenUtils.getScreenHeight(ConversationActivity.this) / 3) { // if more than 100 pixels, its probably a keyboard...
                    if (!show) {
                        setVoiceBtnLocationOnSoftInput();
                    }
                    show = true;
                } else {
                    if (show&&mTargetId.contains("market")) {
                        setVoiceBtnLocation();
                    }
                    show = false;
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        imUtils = new IMUtils();
        getIntentDate(intent);
//        imUtils.setOnAddListener(new IMUtils.OnQueryInfoListener() {
//            @Override
//            public void onResult(int num, String name) {
//                title = name;
//                groupSize = num;
//                showTitle();
//            }
//        });
    }

    public static final String FINISH_CURRENT = "finish_current";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FINISH_CURRENT)) {
                finish();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stoped = false;
        if (resultCode == PAY_SUCCESS) {
            String money = data.getStringExtra(PAY_MONEY);
            String desc = data.getStringExtra(PAY_DECS);
            if (null == desc) {
                desc = "";
            }
            return;
        }
        if (resultCode == INVOICE_MODIFY_SUCCESS) {
            invoiceInfo = (InvoiceInfo) data.getSerializableExtra(INVOICE_MODIFY);
            for (InvoiceInfo info : invoiceInfos) {
                if (info.getType().equals(invoiceInfo.getType())) {
                    info.setMedium(invoiceInfo.getMedium());
                    info.setTypeName(invoiceInfo.getTypeName());
                    info.setType(invoiceInfo.getType());
                    info.setName(invoiceInfo.getName());
                    info.setMediumName(invoiceInfo.getMediumName());
                    if (!TextUtils.isEmpty(invoiceInfo.getEmail())) {
                        info.setEmail(invoiceInfo.getEmail());
                    }
                    if (!TextUtils.isEmpty(invoiceInfo.getNumber())) {
                        info.setNumber(invoiceInfo.getNumber());
                    }
                    if (!TextUtils.isEmpty(invoiceInfo.getEmail())) {
                        info.setEmail(invoiceInfo.getEmail());
                    }
                }
            }
            if (invoiceInfos == null || invoiceInfos.isEmpty()) {
                invoiceInfos = new ArrayList<>();
                invoiceInfos.add(invoiceInfo);
            }
            String tag = invoiceInfo.getType().equals(INVOICE_PERSON) ? getResources().getString(R.string.personal) : getResources().getString(R.string.enterprise);
            if (null != lastOrder) {
                lastOrder.setInvoicetag(tag);
            }
            invoiceTag = tag;
            invoiceMedium = INVOICE_PAPER.equals(invoiceInfo.getMedium()) ? getResources().getString(R.string.paper_type) : "";
            if (null != lastOrder) {
                lastOrder.setInv_eptype(invoiceMedium);
            }
            showInvoiceView(invoiceInfo);
            return;
        }
        if (resultCode == INVOICE_CANCEL) {
            invoiceTag = null;
            showInvoiceView(null);
            return;
        }
        if (resultCode == MODIFY_SUCCESS) {
//            title = data.getStringExtra(MODIFY_RESULT);
//            showTitle();
            requestGroupInfo(true);
            return;
        }
        if (resultCode == SET_ADDRESS_SUCCESS_TO_USE || resultCode == SET_ADDRESS_SUCCESS) {
            String key = buyKey;
            String text = "寄到";
            if (key.length() > 2) {
                if (key.startsWith(text) || key.substring(1).startsWith(text) || key.substring(2).startsWith(text)) {
                    key = postPlace + key;
                }
            }
            if (!key.contains("改地址")) {
                request(key, false);
                requestText.setText(key);
            }
        }
    }

    private void showTitle() {
        titleTextView.setText(title + "(" + groupSize + ")");
    }

    private static final String TAG = "ConversationActivity";
    public static final String CONVERSATION_TYPE = "conversation_type";

    /**
     * 展示如何从 Intent 中得到 融云会话页面传递的 Uri
     */
    private String phone;

    private void getIntentDate(Intent intent) {
        OutlineActivity.title = title;
        messageId = intent.getIntExtra(MESSAGE_ID,-1);
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN, "");
        phone = SPUtils.get(LoginActivity.USER_PHONE, "");
        OutlineActivity.targetId = mTargetId;
        //intent.getData().getLastPathSegment();//获得当前会话类型
        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
        OutlineActivity.conversationType = mConversationType;
        if (mConversationType == Conversation.ConversationType.GROUP) {
            imUtils.searchGroupInfo(token, mTargetId);
            if (null != titleTextView) {
                titleTextView.setText(title + "(" + 2 + ")");
            }
            requestGroupInfo(false);
//            imUtils.searchGroupUserInfo();
            requestAllGroupUsers();
        } else {
            titleTextView.setText(title);
            imUtils.searchUserInfo(token, mTargetId);
        }
    }

    @Override
    public void onBackPressed() {
        out();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            out();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void out() {
        title = "";
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN, "");
        if (CHATMODE) {
            startActivity(new Intent(this, VoiceTxtActivity.class));
            finish();
            return;
        }
        if (TextUtils.isEmpty(token)) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
//            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noteLayout.setVisibility(View.GONE);
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        shopGroup = false;
    }

    private String nikeName;
    private String groupIcon;
    private GroupBase groupBase;
    private GroupInfo groupInfo=new GroupInfo();
    public void requestGroupInfo(boolean justRefreshName) {
        ///pp/group?action=info&token=%s&group=%s
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN, "");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addParameter("action", "info");
        params.addParameter("token", token);
        params.addParameter("group", mTargetId);
        Log.d(TAG, "exitGroup: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: =" + result);
                BaseCode<GroupBase> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<GroupBase>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    groupBase = base.getData();
                    groupInfo = groupBase.getGroup();
                    List<PPUserInfo> list = groupBase.getMembers();
                    title = groupBase.getGroup().getName();
                    if(!TextUtils.isEmpty(groupBase.getGroup().getTag())){
                        title=groupBase.getGroup().getTag();
                    }
                    groupSize = groupBase.getSize();
                    isOwner = groupBase.isOwner();
                    nikeName = groupInfo.getTag();
                    groupIcon = groupInfo.getIcon();
                    groupNote = groupInfo.getBroadcast();
                    groupUserDao.update(groupInfo);
                    if(TextUtils.isEmpty(groupNote)){
                        noteLayout.setVisibility(View.GONE);
                    }else{
                        if(TextUtils.isEmpty(SPUtils.get(mTargetId+groupNote,""))){
                            noteLayout.setVisibility(View.VISIBLE);
                            noteTextView.setText(groupNote);
                            SPUtils.put(mTargetId+groupNote,groupNote);
                        }else{
                            noteLayout.setVisibility(View.GONE);
                        }
                    }
                    EventBus.getDefault().post(new ResetGroupName(groupInfo.getName()));
                    showTitle();
                } else {
                    Toast.makeText(ConversationActivity.this, base.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
            }
        });
    }

    public void requestAllGroupUsers() {
        ///pp/group?action=info&token=%s&group=%s
        String token = SPUtils.get(LoginActivity.LOGIN_TOKEN,"");
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.GROUP_URL);
        params.addParameter("action", "list");
        params.addParameter("token", token);
        params.addParameter("page", 0);
        params.addParameter("size", 20000);
        params.addParameter("group", mTargetId);
        showProgress();
        Log.d(TAG, "requestGroupInfo: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseCode<List<PPUserInfo>> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<List<PPUserInfo>>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    List<PPUserInfo> list = base.getData();
                    final List<UserInfo> users = new ArrayList<>();
                    PPUserDao ppUserDao = new PPUserDao();
                    for (PPUserInfo ppUserInfo : list) {
                        SPUtils.put(ppUserInfo.getPhone()+"icon",ppUserInfo.getIcon());
                        SPUtils.put(ppUserInfo.getPhone()+"name",ppUserInfo.getName());
                        GroupUserInfo groupUserInfo = new GroupUserInfo(mTargetId,ppUserInfo.getPhone(),ppUserInfo.getName());
                        RongIM.getInstance().refreshGroupUserInfoCache(groupUserInfo);
                        Uri portraitUri = Uri.parse(ppUserInfo.getIcon());
                        UserInfo user = new UserInfo(ppUserInfo.getPhone(), ppUserInfo.getName(), portraitUri);
                        users.add(user);
                        PPUserInfo chenInfo = ppUserDao.getFriendByKey(ppUserInfo.getPhone());
                        if(null!=chenInfo){
                            ppUserInfo.setId(chenInfo.getId());
                            ppUserInfo.setFriend(chenInfo.isFriend());
                        }
                        ppUserInfo.setGroupId(mTargetId);
                        ppUserDao.update(ppUserInfo);
                    }
                    RongIM.getInstance().setGroupMembersProvider((groupId, callback) -> {
                        callback.onGetGroupMembersResult(users); // 调用 callback 的 onGetGroupMembersResult 回传群组信息
                    });
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }
            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        PPUserInfo user = new PPUserInfo();
        Uri uri = userInfo.getPortraitUri();
        if (null == uri) {
            return false;
        }
        user.setPhone(userInfo.getUserId());
        user.setName(SPUtils.get(userInfo.getUserId()+"name",""));
        user.setNick(SPUtils.get(userInfo.getUserId()+"nike",""));
        String url = "https://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
//                user.setUri(url);
        user.setIcon(url);
        FriendDetailActivity.launch(ConversationActivity.this,user);
        return true;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        RongMentionManager.getInstance().mentionMember(Conversation.ConversationType.GROUP, mTargetId, userInfo.getUserId());
        return true;
    }

    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        if (message.getContent() instanceof LocationMessage) {
            Intent intent = new Intent(ConversationActivity.this, MapLocationActivity.class);
            intent.putExtra("location", message.getContent());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {
        Tool.mLastLocationCallback = locationCallback;
        Intent intent = new Intent(context, MapLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private void showInvoiceContent(SmartOrder order) {
        invoiceInfos = order.getInvoice();
//        invoiceInfos = new ArrayList<>();
        String pName = SPUtils.get(PERSON_NAME + phone, "");
        String cName = SPUtils.get(COMPANY_NAME + phone, "");
        String eName = SPUtils.get(EMAIL_NAME + phone, "");
        String cNum = SPUtils.get(COMPANY_NUM + phone, "");
        String pMedium = SPUtils.get(PERSON_MEDIUM + phone, "");
        String cMedium = SPUtils.get(COMPANY_MEDIUM + phone, "");
        if (null != invoiceInfos && !invoiceInfos.isEmpty()) {
            for (InvoiceInfo invoiceInfo : invoiceInfos) {
                if (invoiceInfo.getType().equals(INVOICE_PERSON)) {
                    if (!TextUtils.isEmpty(pName)) {
                        invoiceInfo.setName(pName);
                    }
                    if (!TextUtils.isEmpty(pMedium)) {
                        invoiceInfo.setMedium(pMedium);
                    }
                } else {
                    if (!TextUtils.isEmpty(cName)) {
                        invoiceInfo.setName(cName);
                    }
                    if (!TextUtils.isEmpty(cNum)) {
                        invoiceInfo.setNumber(cNum);
                    }
                    if (!TextUtils.isEmpty(cMedium)) {
                        invoiceInfo.setMedium(cMedium);
                    }
                }
                if (!TextUtils.isEmpty(eName)) {
                    invoiceInfo.setEmail(eName);
                }
            }
        }
        invoiceTag = order.getInvoicetag();
        invoiceMedium = order.getInv_eptype();
        if (TextUtils.isEmpty(invoiceTag) && !TextUtils.isEmpty(invoiceMedium)) {
            invoiceTag = getResources().getString(R.string.personal);
        }
        invoiceInfo = null;

        invoiceLayout.setVisibility(View.GONE);
        noInvoiceLayout.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(invoiceTag)) {
            String tag = getResources().getString(R.string.personal).equals(invoiceTag) ? INVOICE_PERSON : INVOICE_COMPANY;
            if (null != invoiceInfos && invoiceInfos.size() > 0) {
                for (InvoiceInfo info : invoiceInfos) {
                    if (info.getType().equals(tag)) {
                        invoiceInfo = info;
                        break;
                    }
                }
            }
            if (null == invoiceInfo) {
                invoiceInfo = new InvoiceInfo();
                if (tag.equals(INVOICE_PERSON)) {
                    if (!TextUtils.isEmpty(pName)) {
                        invoiceInfo.setName(pName);
                    }
                } else {
                    if (!TextUtils.isEmpty(cName)) {
                        invoiceInfo.setName(cName);
                    }
                    if (!TextUtils.isEmpty(cNum)) {
                        invoiceInfo.setNumber(cNum);
                    }
                }
                if (!TextUtils.isEmpty(eName)) {
                    invoiceInfo.setEmail(eName);
                }
                invoiceInfo.setType(tag);
                String medium = getResources().getString(R.string.paper_type).equals(invoiceMedium) ? INVOICE_PAPER : INVOICE_ELECT;
                if (TextUtils.isEmpty(invoiceInfo.getName()) || (medium.equals(INVOICE_ELECT) && TextUtils.isEmpty(invoiceInfo.getEmail()))) {
                    noInvoiceLayout.setVisibility(View.VISIBLE);
                    setInvoice(invoiceInfo);
                } else {
                    showInvoiceView(invoiceInfo);
                }
            } else {
                showInvoiceView(invoiceInfo);
            }
        } else {
            noInvoiceLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showInvoiceView(InvoiceInfo invoice) {
        invoiceLayout.setVisibility(View.GONE);
        noInvoiceLayout.setVisibility(View.GONE);
        if (null == invoice) {
            noInvoiceLayout.setVisibility(View.VISIBLE);
        } else {
            invoiceLayout.setVisibility(View.VISIBLE);
            String medium = getResources().getString(R.string.paper_type).equals(invoiceMedium) ? INVOICE_PAPER : INVOICE_ELECT;
            String mediumName = medium.equals(INVOICE_ELECT) ? getResources().getString(R.string.elec_invoice) : getResources().getString(R.string.paper_invoice);
            invoiceInfo.setMedium(medium);
            invoiceInfo.setMediumName(mediumName);
            String title = invoiceInfo.getType().equals(INVOICE_PERSON) ? getResources().getString(R.string.personal) : getResources().getString(R.string.enterprise);
            invoiceType.setText(invoiceInfo.getMediumName());
            invoiceType1.setText(invoiceInfo.getMediumName());
            invoiceTitleType.setText(title);
            invoiceTitleName.setText(invoiceInfo.getName());
            if (INVOICE_PERSON.equals(invoiceInfo.getType())) {
                taxNumLayout.setVisibility(View.GONE);
            } else {
                taxNumLayout.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(invoiceInfo.getNumber())) {
                invoiceNum.setText(invoiceInfo.getNumber());
            }
            invoicePrice.setText(String.format(getResources().getString(R.string.format_yuan_s), BigDecimalUtil.format(Double.valueOf((double) lastOrder.getAllPrice() / 100))));
            if (invoiceInfo.getMedium().equals(INVOICE_ELECT)) {
                invoiceEmail.setText(invoiceInfo.getEmail());
                emailLayout.setVisibility(View.VISIBLE);
            } else {
                emailLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setInvoice(InvoiceInfo invoice) {
        Intent invoiceIntent = new Intent(ConversationActivity.this, InvoiceActivity.class);
        invoiceIntent.putExtra(INVOICE_DATA, (Serializable) invoiceInfos);
        invoiceIntent.putExtra(INVOICE_MODIFY, invoice);
        startActivityForResult(invoiceIntent, 0);
        overridePendingTransition(0, 0);
    }
    public void onEventMainThread(ModifyUserNike var) {
        PPUserInfo ppUserInfo = var.getPpUserInfo();
//        showData();
    }
    public String serverId;

    public void getServer() {
        //https://pp.fangnaokeji.com:9443/pp/express?action=server&group=XX
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.EXPRESS);
        params.addParameter("action", "server");
        params.addParameter("group", mTargetId);
//        params.addParameter("no", waresOrder.getTrade_no());
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
//                readJson(result);
                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "getServer: " + result);
                BaseCode<String> base = new Gson().fromJson(
                        result,
                        new TypeToken<BaseCode<String>>() {
                        }.getType());
                if (base.getCode() == 0) {
                    serverId = base.getData();
                    String name = SPUtils.get(MARKET_SERVER, "");

                    String id = TextUtil.getServerId(serverId);
                    RongIM.getInstance().startConversation(ConversationActivity.this, Conversation.ConversationType.PRIVATE, id, String.format(getResources().getString(R.string.custom_server), name));
//                    RongIM.getInstance().startConversation(ConversationActivity.this, Conversation.ConversationType.PRIVATE, id, name + getResources().getString(R.string.custom_server));
                }
//                readJson(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
            }
        });
    }

    private int[] imgs = {R.mipmap.speak0, R.mipmap.speak1, R.mipmap.speak2, R.mipmap.speak3, R.mipmap.speak4, R.mipmap.speak5, R.mipmap.speak6, R.mipmap.speak7, R.mipmap.speak7};
    private int current = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                handler.removeMessages(2);
                if (!recording) {
                    return;
                }
                if (index < 0) index = 0;
                if (index > imgs.length - 1) index = imgs.length - 1;
                floatBtn.setBackgroundResource(imgs[index]);
                handler.sendEmptyMessageDelayed(2, 100);
                current = index - 1;
            } else if (msg.what == 2) {
                if (!recording) {
                    return;
                }
                if (current < index - 1) {
                    current = index;
                }
                if (current < 0) current = 0;
                if (current > imgs.length - 1) current = imgs.length - 1;
                floatBtn.setBackgroundResource(imgs[current]);
                handler.sendEmptyMessageDelayed(2, 50);
            } else if (msg.what == 3) {
                floatBtn.setBackgroundResource(R.mipmap.speak0);
            } else if (msg.what == 4) {
                getServer();
            } else if (msg.what == 5) {
                stopTencenVoice();
                String key = buildMessage(resMap);
                if (!TextUtils.isEmpty(key)) {
                    handleContent(key);
                }
//                getServer();
            }
//            else if (msg.what == 6) {
//                String tts = (String) msg.obj;
//                ttsHelper.start(0, ConversationActivity.this, tts);
//            }
            else {
                setVoiceBtnLocation();
            }
        }
    };

    private void setVoiceBtnLocation() {
        int maxX = PPScreenUtils.getScreenWidth(ConversationActivity.this) - PPScreenUtils.dip2px(ConversationActivity.this, 120);
        int maxY = PPScreenUtils.getScreenHeight(ConversationActivity.this) - PPScreenUtils.dip2px(ConversationActivity.this, 120);
        int x = SPUtils.get(FLOATX, maxX);
        int y = SPUtils.get(FLOATY, maxY);
        if (x > maxX) {
            x = maxX;
        }
        if (y > maxY) {
            y = maxY;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) floatBtn.getLayoutParams();
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        floatBtn.setLayoutParams(layoutParams);
        floatBtn.requestFocus();
        voiceLayout.setVisibility(View.VISIBLE);
    }

    private void setVoiceBtnLocationOnSoftInput() {
        int x = SPUtils.get(FLOATX, PPScreenUtils.dip2px(ConversationActivity.this, 60));
        int maxX = PPScreenUtils.getScreenWidth(ConversationActivity.this) - PPScreenUtils.dip2px(ConversationActivity.this, 120);
        if (x > maxX) {
            x = maxX;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) floatBtn.getLayoutParams();
        layoutParams.leftMargin = x;
        layoutParams.topMargin = PPScreenUtils.getScreenHeight(this) / 2 - PPScreenUtils.dip2px(this, 100);
        floatBtn.setLayoutParams(layoutParams);
    }

    private void showPlayState() {
        if (playTts) {
            playView.setBackgroundResource(R.mipmap.sy_on);
        } else {
            playView.setBackgroundResource(R.mipmap.sy_of);
        }
    }

    public void playTTsEvent() {
        playTts = !playTts;
        showPlayState();
        SPUtils.put(PLAY_TTS, playTts);
        if (!playTts && ttsHelper != null) {
            ttsHelper.stop();
        }
    }

    public void shopCarEvent() {
        if (null != ttsHelper && ttsHelper != null) {
            ttsHelper.stop();
        }
        if (shopCarNum.getVisibility() == View.GONE) {
            return;
        }
        if (waresIntroView.getVisibility() == View.VISIBLE) {
            return;
        }
        if (lastOrder != null) {
            showOrderState(lastOrder);
        }
//        waresIntroView.setVisibility(View.VISIBLE);
//        shopLayout.setVisibility(View.VISIBLE);
//        ttsView.setVisibility(View.GONE);
//        requestText.setVisibility(View.GONE);
//        resultImageView.setVisibility(View.GONE);
//        helpTextView.setVisibility(View.VISIBLE);
//        helpTextView.setText(helpText);
    }

    private boolean recording;

    private void start() {
        if (xunfei) {
            ifeyBtn.start();
        } else {
            startTencenVoice();
        }
        handler.removeMessages(2);
        LinearInterpolator lir = new LinearInterpolator();
        anim.setInterpolator(lir);
        floatBtn.startAnimation(anim);
        if (ttsHelper != null) {
            ttsHelper.stop();
        }
        recording = true;
    }

    private void stop(boolean showDialog) {
        recording = false;
        handler.removeMessages(2);
        setVoiceBtnLocation();
        ifeyBtn.stop();
        if (showDialog) {
            floatBtn.setBackgroundResource(R.drawable.shop_dialog);
            AnimationDrawable anim = (AnimationDrawable) floatBtn.getBackground();
            anim.start();
        } else {
            floatBtn.setBackgroundResource(imgs[0]);
            floatBtn.clearAnimation();
        }
    }

    @Event(value = R.id.bg_layout)
    private void bg_layout(View v) {
    }

    @Event(value = R.id.shop_layout)
    private void shop_layout(View v) {
        closeVoice();
    }

    private void closeVoice() {
        shopLayout.setVisibility(View.GONE);
        stopVoice();
    }

    private void stopVoice() {
        if (recording) {
            stop(false);
        }
        if (null != ttsHelper && ttsHelper != null) {
            ttsHelper.stop();
        }
    }

    private boolean stoped;

    @Override
    public void onStop() {
        super.onStop();
        stoped = true;
        stopVoice();
    }

    @Override
    public void onResume() {
        super.onResume();
        stoped = false;
        setCurrentActivity(this);
    }

    private IWXAPI api;
    private PayReq req;

    private void pay(String result) {
        if (null == api) {
            api = WXAPIFactory.createWXAPI(ConversationActivity.this, getString(R.string.wx_key));
            api.registerApp(getString(R.string.wx_key));
        }
        req = new PayReq();
        try {
            BaseCode<PayOrder> base = new Gson().fromJson(
                    result,
                    new TypeToken<BaseCode<PayOrder>>() {
                    }.getType());
            if (base.getCode() == 0) {
                PayOrder order = base.getData();
                IlessonApp.getInstance().setCommonBuy(false);
                trade = order.getTrade();
                req.appId = order.getAppid();
                req.partnerId = order.getPartnerid();
                req.prepayId = order.getPrepayid();
                req.nonceStr = order.getNoncestr();
                req.timeStamp = order.getTimestamp();
                req.packageValue = "Sign=WXPay";
                req.sign = order.getSign();
                req.extData = "app data";
                if (waresIntroView.getVisibility() == View.GONE) {
                    waresIntroView.setVisibility(View.VISIBLE);
//                    showContent.setVisibility(View.GONE);
                    ttsView.setVisibility(View.GONE);
                    toPay.setVisibility(View.VISIBLE);
                    if (lastOrder != null) {
                        showOrderState(lastOrder);
                    }
                } else {
                    api.sendReq(req);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private AddressInfo addressInfo;
    private String buyKey = "";
    private String invoiceTag;
    private String invoiceMedium;
    private String postPlace = "";

    public void request(final String key, final boolean voice) {
        if (stoped) {
            return;
        }
        if (key == null || key.length() > 100) {
            return;
        }
        currentKey = key;
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
//        params.addParameter("pid", choicePrice.getId() + "");
//        params.addParameter("user", account);
        params.addParameter("action", "talk");
        params.addParameter("group", mTargetId);
        params.addParameter("key", key);
        if (!TextUtils.isEmpty(invoiceTag) && null != invoiceInfo) {
            params.addParameter("invoice_type", invoiceInfo.getType());
            params.addParameter("invoice_medium", invoiceInfo.getMedium());
            params.addParameter("invoice_name", invoiceInfo.getName());
            params.addParameter("invoice_email", invoiceInfo.getEmail());
            params.addParameter("invoice_number", invoiceInfo.getNumber());
        }

//       showProgress();
        Log.d(TAG, "loadData: " + params.toString());
        x.http().post(params, new Callback.CommonCallback<String>() {
            @SuppressLint("StringFormatMatches")
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "request onSuccess: " + result);
                try {
                    BaseCode base = new Gson().fromJson(
                            result,
                            new TypeToken<BaseCode>() {
                            }.getType());
                    if (base.getCode() == 0) {
                        if (base.getTag() == 2) {
                            pay(result);
                            return;
                        }
                        shopLayout.setVisibility(View.VISIBLE);
                        toPay.setVisibility(View.GONE);
                        helpTextView.setVisibility(View.GONE);
                        toSetAddress.setVisibility(View.GONE);
                        noInvoiceLayout.setVisibility(View.GONE);
                        invoiceLayout.setVisibility(View.GONE);
                        resultImageView.setVisibility(View.GONE);
//                        waresIntroView.setVisibility(View.GONE);
//                        resultImageView.setVisibility(View.GONE);
//                        addressLayout.setVisibility(View.GONE);
                        BaseCode<List<SmartOrder>> data = new Gson().fromJson(
                                result,
                                new TypeToken<BaseCode<List<SmartOrder>>>() {
                                }.getType());
                        List<SmartOrder> list = data.getData();
                        if (list.isEmpty()) {
                            return;
                        }
                        SmartOrder order = list.get(0);
                        smartOrder = order;
                        if (null == order) {
                            return;
                        }
                        String tts = order.getTts();
                        if (!TextUtils.isEmpty(tts)) {
                            ttsView.setText(tts);
                            ttsView.setVisibility(View.VISIBLE);
                        } else {
                            ttsView.setVisibility(View.GONE);
                        }
                        String ttsm = base.getBak();
//                        if(TextUtils.isEmpty(ttsm)){
//                            ttsm=tts;
//                        }
                        if (!TextUtils.isEmpty(ttsm)) {
                            String text = getResources().getString(R.string.modify_tts);
                            SpannableStringBuilder style = new SpannableStringBuilder(text + ttsm);
                            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.theme_color)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ttsModify.setText(style);
                            ttsModify.setVisibility(View.VISIBLE);
                        } else {
                            ttsModify.setVisibility(View.GONE);
                        }
                        String help = order.getHelp().trim();
                        if (!TextUtils.isEmpty(help)) {
                            helpTextView.setText(help);
                            helpTextView.setVisibility(View.VISIBLE);
                        }
                        if (playTts) {
                            if ("en".equals(order.getTtsType())) {
                                ttsHelper.startEnglish(data.getTag(), tts);
                            } else {
                                ttsHelper.startChinese(data.getTag(), tts);
                            }
                        } else {
                            if (!TextUtils.isEmpty(currentKey) && voice) {
                                playerUtils.play();
                            } else {
//                                ttsHelper.start(data.getTag(), ConversationActivity.this, tts);
                            }
                        }

                        if (data.getTag() == 0) {
                            if (tts.contains("寄到哪里")) {
                                postPlace = key;
                            }
                            showContent(order);
                        } else if (data.getTag() == 1) {//订单
//                            showContent.setVisibility(View.GONE);
                            showOrderState(order);
                        } else if (data.getTag() == 3) {//没有地址
                            buyKey = currentKey;
                            toSetAddress.setVisibility(View.VISIBLE);
                            addressLayout.setVisibility(View.GONE);
                            express.setVisibility(View.GONE);
                            allPrice.setVisibility(View.GONE);
                            resultText.setVisibility(View.GONE);
                            int num = Integer.valueOf(order.getNum());
                            waresNum.setText("x" + num);
                            resultImageView.setVisibility(View.GONE);
                            if (!TextUtils.isEmpty(order.getImgurl())) {
                                roundImageView.setVisibility(View.VISIBLE);
                                Glide.with(ConversationActivity.this).load(order.getImgurl()).into(roundImageView);
                            }
                            waresIntroView.setVisibility(View.VISIBLE);
                            if (!TextUtils.isEmpty(order.getSubUnit())) {
                                waresQuantity.setText("/" + order.getSubUnit());
                            }
                            if (!TextUtils.isEmpty(order.getSubName())) {
                                waresName.setText(order.getSubName());
                            }
                            if (!TextUtils.isEmpty(order.getSubDesc())) {
                                waresInfo.setText(order.getSubDesc());
                            }
                            if (!TextUtils.isEmpty(order.getName())) {
                                userName.setText(order.getName());
                            }
                            double price = order.getPrice();
                            if (order.getPrice() <= 0) {
                                price = num * order.getSubPrice();
                            }
                            if (price >= 0) {
                                waresPrice.setText(String.format(getResources().getString(R.string.wares_price), BigDecimalUtil.format(price / 100)));
                                waresPrice.setVisibility(View.VISIBLE);
                            }
//                            if (order.getAllPrice() >= 0) {
//                                allPrice.setText(TextUtil.getFei(ConversationActivity.this, order.getAllPrice()));
//                                allPrice.setVisibility(View.VISIBLE);
//                            }
                            if (order.getSubPrice() >= 0) {
                                unitPrice.setText(getResources().getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(order.getSubPrice()) / 100));
                                unitPrice.setVisibility(View.VISIBLE);
                            }
//                            showContent(order);
                        } else if (data.getTag() == 4) {
//                            showContent.setVisibility(View.GONE);
                            shopCarNum.setVisibility(View.GONE);
                            shopCarView.setVisibility(View.GONE);
                            showContent(order);
                        } else if (data.getTag() == 5) {//找客服
//                            waresIntroView.setVisibility(View.GONE);
//                            resultImageView.setVisibility(View.GONE);
//                            resultText.setVisibility(View.GONE);
//                            shopLayout.setVisibility(View.GONE);
                            if (!playTts) {
                                getServer();
                            }
                        } else if (data.getTag() == 6) {//修改地址
                            waresIntroView.setVisibility(View.GONE);
                            addressInfo = base.getExtra();
                            if (!playTts) {
                                modifyAddress();
                            }
                        } else if (data.getTag() == 8) {

                        } else if (data.getTag() == 9) {
//                            toOrderList();
                        } else if (data.getTag() == 10) {
//                            toOrderList();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
            }


            @Override
            public void onFinished() {
                floatBtn.setBackgroundResource(R.mipmap.speak0);
                floatBtn.clearAnimation();
                setVoiceBtnLocation();
            }
        });
    }

    private SmartOrder lastOrder;

    private void showOrderState(SmartOrder order) {
        lastOrder = order;
        shopCarNum.setVisibility(View.GONE);
        shopCarView.setVisibility(View.GONE);
        userName.setText("");
        addressView.setText("");
        phoneView.setText("");
        waresNum.setText("x" + order.getNum());
        helpText = order.getHelp();
        resultText.setVisibility(View.GONE);
        roundImageView.setVisibility(View.GONE);
        addressLayout.setVisibility(View.VISIBLE);
        resultImageView.setVisibility(View.GONE);
        buyKey = currentKey;
        if (!TextUtils.isEmpty(order.getIcon())) {
            roundImageView.setVisibility(View.VISIBLE);
            Glide.with(ConversationActivity.this).load(order.getIcon()).into(roundImageView);
        }
        waresIntroView.setVisibility(View.VISIBLE);
        shopCarNum.setVisibility(View.VISIBLE);
        shopCarView.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(order.getSubUnit())) {
            waresQuantity.setText("/" + order.getSubUnit());
        }
        if (!TextUtils.isEmpty(order.getSubName())) {
            waresName.setText(order.getSubName());
        }
        if (!TextUtils.isEmpty(order.getSubDesc())) {
            waresInfo.setText(order.getSubDesc());
        }
        if (!TextUtils.isEmpty(order.getName())) {
            userName.setText(order.getName());
        }
        if (order.getFeiPrice() >= 0) {
            //BigDecimalUtil.format(Double.valueOf(waresIntro.getPrice())/100)
            express.setText(String.format(getResources().getString(R.string.express_fee), BigDecimalUtil.format(Double.valueOf(order.getFeiPrice()) / 100)));
            express.setVisibility(View.VISIBLE);
        }
        if (order.getPrice() >= 0) {
            waresPrice.setText(String.format(getResources().getString(R.string.wares_price), BigDecimalUtil.format(Double.valueOf(order.getPrice()) / 100)));
            waresPrice.setVisibility(View.VISIBLE);
        }
        if (order.getAllPrice() >= 0) {
            allPrice.setText(TextUtil.getFei(ConversationActivity.this, order.getAllPrice()));
            allPrice.setVisibility(View.VISIBLE);
        }
        if (order.getSubPrice() >= 0) {
            unitPrice.setText(getResources().getString(R.string.rmb) + BigDecimalUtil.format(Double.valueOf(order.getSubPrice()) / 100));
            unitPrice.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(order.getAddress())) {
            addressView.setText(order.getAddress());
        }
        if (!TextUtils.isEmpty(order.getPhone())) {
            phoneView.setText(order.getPhone());
        }
        if (TextUtils.isEmpty(order.getInvoicetag())) {
            noInvoiceLayout.setVisibility(View.VISIBLE);
        }
        showInvoiceContent(order);
    }

    private void toOrderList() {
        Intent intent = new Intent(ConversationActivity.this, WareOrderListActivity.class);
        startActivity(intent);
    }

    private void modifyAddress() {
        Intent intent = new Intent(ConversationActivity.this, AddressActivity.class);
        if (null == addressInfo) {
            intent.setClass(this, AddressListActivity.class);
        }
        waresIntroView.setVisibility(View.GONE);
        intent.putExtra(AddressActivity.CURRENTADDRESS, addressInfo);
        intent.putExtra(ExchangeActivity.ADDRESS_INFO, true);
        intent.putExtra(AddressActivity.ACTION_MODIFY_BUY, true);
        startActivityForResult(intent, 99);
    }


    private SmartOrder currentOrder;

    private void showContent(final SmartOrder order) {
        currentOrder = order;
        showContent.setVisibility(View.VISIBLE);
        waresIntroView.setVisibility(View.GONE);
        String tag = mTargetId + phone + "first";
        boolean first = SPUtils.get(tag, true);
        if (first && TextUtils.isEmpty(currentKey)) {
            requestText.setVisibility(View.GONE);
            ttsView.setVisibility(View.VISIBLE);
//            showContent.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(order.getHelp())) {
                helpTextView.setVisibility(View.VISIBLE);
                helpTextView.setText(order.getHelp());
            }
            SPUtils.put(tag, false);
        }
        if (!first && TextUtils.isEmpty(currentKey)) {
            shopLayout.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(order.getAddress()) && TextUtils.isEmpty(order.getLink()) && TextUtils.isEmpty(order.getPhone()) && TextUtils.isEmpty(order.getName()) && TextUtils.isEmpty(order.getText()) && TextUtils.isEmpty(order.getHelp())
                && order.getPrice() > 0 && TextUtils.isEmpty(order.getImgurl())) {
            shopLayout.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.isEmpty(order.getText())) {
            resultText.setText(order.getText());
            resultText.setVisibility(View.VISIBLE);
        } else {
            resultText.setText("");
            resultText.setVisibility(View.GONE);
            if (TextUtils.isEmpty(order.getImgurl())) {
//                showContent.setVisibility(View.GONE);
            }
        }
        if (TextUtils.isEmpty(order.getImgurl())) {
            resultImageView.setVisibility(View.GONE);
            return;
        }
        Glide.with(ConversationActivity.this).asBitmap().load(order.getImgurl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ViewGroup.LayoutParams params = resultImageView.getLayoutParams();
                int width = 0;
                int height = 0;
                int maxW = (int) (screenWidth * .85);
                if ((double) resource.getWidth() / (double) resource.getHeight() > 1.2) {
                    width = maxW;
                    height = resource.getHeight() * width / resource.getWidth();
                } else {
                    int calW = PPScreenUtils.dip2px(ConversationActivity.this, resource.getWidth());
                    if (calW < maxW) {
                        width = calW;
                        height = PPScreenUtils.dip2px(ConversationActivity.this, resource.getHeight());
                    }
                }
                params.width = width;
                params.height = height;
                resultImageView.setLayoutParams(params);
                resultImageView.setImageBitmap(resource);
                resultImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                resultImageView.setVisibility(View.VISIBLE);
            }
        });
        resultImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePreviewActivity.startPreview(ConversationActivity.this, order.getImgurl());
            }
        });
    }

    private void initSpeech() {
        ttsHelper = new TTSHelper(this);
        ttsHelper.setOnTTSFinish(new TTSHelper.OnTTSFinish() {
            @Override
            public void onTTSFinish(int type) {
                switch (type) {
                    case 5:
                        getServer();
                        break;
                    case 6:
                        modifyAddress();
                        break;
                    case 8:
                    case 9:
                    case 10:
                        toOrderList();
                        break;
                }
            }

            @Override
            public void onTTSstart() {

            }
        });
        SpeechUtility.createUtility(this,
                getResources().getString(R.string.xunfei_appid));
        initIfey();
    }

    public void toSpeech() {

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            if (recording) {
                                Log.d(TAG, "record_view: stop();");
                                stop(true);
                            } else {
                                start();
                            }
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时。还会提示请求权限的对话框
//                            finish();
                        } else {
                            getPermission();
                            // 用户拒绝了该权限，而且选中『不再询问』那么下次启动时，就不会提示出来了，
                        }
                    }
                });
//                .subscribe(new Consumer<Boolean>() {
//            public void accept(Boolean aBoolean) {
//                if (aBoolean) {
//                    initSpeech();
//                } else {
//                    Toast.makeText(ConversationActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    public void getPermission() {
        AlertDialog.Builder alertDialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(this);
        }
        alertDialog.setTitle("权限设置")
                .setMessage("应用缺乏录音权限，是否前往手动授予该权限？")
                .setPositiveButton("前往", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        alertDialog.show();
    }


    private void initIfey() {
        ifeyBtn = new IfeyVoiceWidget1(this);
        ifeyBtn.initIfey(new IfeyVoiceWidget1.MessageListener() {

            @Override
            public void onReceiverMessage(String content) {
                if (null != ifeyBtn) {
                    ifeyBtn.stop();
                }
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                content = content.toLowerCase();
                handleContent(content);
                stop(true);
                Log.d(TAG, "onReceiverMessage: ");
            }

            @Override
            public void onStateChanged(boolean state) {
                if (state) {
//                    start();
                } else {
                    recording = false;
                    stop(false);
                    ifeyBtn.stop();
                }
            }
        }, null, false);
        ifeyBtn.setOnVolumeChangeListener(new IfeyVoiceWidget1.OnVolumeChangeListener() {
            @Override
            public void onVolumeChanged(int progress, short[] data) {
                showVolume(progress);
            }
        });
        ifeyBtn.setOnTextReceiverListener(new IfeyVoiceWidget1.OnTextReceiverListener() {
            @Override
            public void TextReceiver(String text) {
            }
        });
        anim = AnimationUtils.loadAnimation(this, R.anim.voice_view_anim);
    }

    private void showVolume(int volume) {
        int p = (last + volume) / 2;
        last = p;
        index = volume;
        handler.sendEmptyMessage(1);
    }

    private int last = 0;
    private int index = 0;
    Animation anim;


    @Override
    public boolean onSearchRequested(@Nullable SearchEvent searchEvent) {
        return super.onSearchRequested(searchEvent);
    }


    private void handleContent(String content) {
        if (requestText.getVisibility() == View.GONE && !TextUtils.isEmpty(content)) {
            requestText.setVisibility(View.VISIBLE);
        }
        content = content.replace("，", "").replace("。", "").replace("！", "").replace("？", "");
        requestText.setText(content);
        request(content, true);
    }

    public void clickVoice() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    if (recording) {
                        Log.d(TAG, "record_view: stop();");
                        stop(true);
                    } else {
                        start();
                    }
                } else {
                    Toast.makeText(ConversationActivity.this, R.string.no_permission, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onEventMainThread(PaySuccess var) {
        shopLayout.setVisibility(View.GONE);
        showShopTheme(false);
        shopCarView.setVisibility(View.GONE);
        shopCarNum.setVisibility(View.GONE);
        if (!IlessonApp.getInstance().isCommonBuy()) {
            new IMUtils().loadTrade(ConversationActivity.this, trade, false);
        }
    }

    public void onEventMainThread(AddressInfo var) {
//        search(buyKey,false);
    }
    public void onEventMainThread(DeleteFriend var) {
        finish();
    }
    public void onEventMainThread(ModifyGroupNike var) {
        requestGroupInfo(true);
    }

    public void onEventMainThread(ModifyGroupName var) {
        requestGroupInfo(true);
    }

    private AAIClient aaiClient;
    private AudioRecognizeRequest audioRecognizeRequest;
    private AudioRecognizeResultListener audioRecognizeResultListener;
    private AudioRecognizeConfiguration audioRecognizeConfiguration;
    private AudioRecognizeTimeoutListener audioRecognizeTimeoutListener;
    private AudioRecognizeStateListener audioRecognizeStateListener;
    boolean dontHaveResult = true;

    private void startTencenVoice() {
        int appid = Integer.valueOf(PPConfig.apppId);
        int projectid = 1217813;
        String secretId = PPConfig.secretId;
// 为了方便用户测试，sdk提供了本地签名，但是为了secretKey的安全性，正式环境下请自行在第三方服务器上生成签名。
        AbsCredentialProvider credentialProvider = new LocalCredentialProvider(PPConfig.secretKey);
        ClientConfiguration.setMaxRecognizeSliceConcurrentNumber(1);
        try {
            // 1、初始化AAIClient对象。
            aaiClient = new AAIClient(this, appid, projectid, secretId, credentialProvider);
            // 3、初始化语音识别结果监听器。
            audioRecognizeResultListener = new AudioRecognizeResultListener() {
                @Override
                public void onSliceSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult result, int seq) {

                    resMap.put(String.valueOf(seq), result.getText());
                    final String msg = buildMessage(resMap);
                    Log.d(TAG, "onSliceSuccess: " + msg);
                    // 返回语音分片的识别结果
                }

                @Override
                public void onSegmentSuccess(AudioRecognizeRequest audioRecognizeRequest, AudioRecognizeResult result, int seq) {
                    handler.removeMessages(5);
                    handler.sendEmptyMessageDelayed(5, 500);
                    // 返回语音流的识别结果
                    dontHaveResult = true;
                    resMap.put(String.valueOf(seq), result.getText());
                    final String msg = buildMessage(resMap);
                    Log.d(TAG, "onSegmentSuccess: " + msg);
                }

                @Override
                public void onSuccess(AudioRecognizeRequest audioRecognizeRequest, String content) {
                    Log.d(TAG, "audioRecognizeResultListener onSuccess: ");

                    // 返回所有的识别结果
                }

                @Override
                public void onFailure(AudioRecognizeRequest audioRecognizeRequest, ClientException e, ServerException e1) {
                    // 识别失败
                }

            };
            audioRecognizeStateListener = new AudioRecognizeStateListener() {

                /**
                 * 开始录音
                 * @param request
                 */
                @Override
                public void onStartRecord(AudioRecognizeRequest request) {
                    currentRequestId = request.getRequestId();
                    Log.d(TAG, "onStartRecord: ");
                }

                public void onNextAudioData(final short[] audioDatas, final int readBufferLength) {
                }

                /**
                 * 结束录音
                 * @param request
                 */
                @Override
                public void onStopRecord(AudioRecognizeRequest request) {
                    Log.d(TAG, "onStopRecord: ");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            final String msg = buildMessage(resMap);
                            stop(TextUtils.isEmpty(msg) ? false : true);
                        }
                    });
                }

                /**
                 * 第seq个语音流开始识别
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowStartRecognize(AudioRecognizeRequest request, int seq) {

                }

                /**
                 * 第seq个语音流结束识别
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowFinishRecognize(AudioRecognizeRequest request, int seq) {
                    Log.d(TAG, "onVoiceFlowFinishRecognize: " + request);
                }

                /**
                 * 第seq个语音流开始
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowStart(AudioRecognizeRequest request, int seq) {
                }

                /**
                 * 第seq个语音流结束
                 * @param request
                 * @param seq
                 */
                @Override
                public void onVoiceFlowFinish(AudioRecognizeRequest request, int seq) {
                }

                /**
                 * 语音音量回调
                 * @param request
                 * @param volume
                 */
                @Override
                public void onVoiceVolume(AudioRecognizeRequest request, final int volume) {
                    Log.d(TAG, "onVoiceVolume: " + volume);
                    showVolume(volume / 3);
                }

            };
            AudioRecognizeRequest.Builder builder = new AudioRecognizeRequest.Builder();

            boolean isSaveAudioRecordFiles = false;//默认是关的 false
            // 初始化识别请求
            audioRecognizeRequest = builder
//                        .pcmAudioDataSource(new AudioRecordDataSource()) // 设置数据源
                    .pcmAudioDataSource(new AudioRecordDataSource(isSaveAudioRecordFiles)) // 设置数据源
                    //.templateName(templateName) // 设置模板
                    .template(new AudioRecognizeTemplate("16k_ca", 0, 0)) // 设置自定义模板
                    .setFilterDirty(1)  // 0 ：默认状态 不过滤脏话 1：过滤脏话
                    .setFilterModal(0) // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
                    .setFilterPunc(2) // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
                    .setConvert_num_mode(1) //1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
                    .setVadSilenceTime(2000) // 语音断句检测阈值，静音时长超过该阈值会被认为断句（多用在智能客服场景，需配合 needvad = 1 使用） 默认不传递该参数
                    .setNeedvad(1) //0：关闭 vad，1：默认状态 开启 vad。
                    .setHotWordId("2d36d9727d7e11eb80b3446a2eb5fd98")//热词 id。用于调用对应的热词表，如果在调用语音识别服务时，不进行单独的热词 id 设置，自动生效默认热词；如果进行了单独的热词 id 设置，那么将生效单独设置的热词 id。
                    .build();

            // 自定义识别配置
            audioRecognizeConfiguration = new AudioRecognizeConfiguration.Builder()
                    .setSilentDetectTimeOut(false)// 是否使能静音检测，true表示不检查静音部分
                    .audioFlowSilenceTimeOut(1000) // 静音检测超时停止录音
                    .minAudioFlowSilenceTime(2000) // 语音流识别时的间隔时间
                    .minVolumeCallbackTime(80) // 音量回调时间
                    .sensitive(2.5f)
                    .build();
            audioRecognizeTimeoutListener = new AudioRecognizeTimeoutListener() {

                /**
                 * 检测第一个语音流超时
                 * @param request
                 */
                @Override
                public void onFirstVoiceFlowTimeout(AudioRecognizeRequest request) {
                    Log.d(TAG, "onFirstVoiceFlowTimeout: ");
                }

                /**
                 * 检测下一个语音流超时
                 * @param request
                 */
                @Override
                public void onNextVoiceFlowTimeout(AudioRecognizeRequest request) {
                    Log.d(TAG, "onNextVoiceFlowTimeout: ");
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        resMap.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (aaiClient != null) {
                    aaiClient.startAudioRecognize(audioRecognizeRequest,
                            audioRecognizeResultListener,
                            audioRecognizeStateListener,
                            audioRecognizeTimeoutListener,
                            audioRecognizeConfiguration);
                }
            }
        }).start();
    }

    private void stopTencenVoice() {
        floatBtn.setBackgroundResource(imgs[0]);
        floatBtn.clearAnimation();
        if (aaiClient != null) {
            //停止语音识别，等待当前任务结束
            boolean state = aaiClient.stopAudioRecognize(currentRequestId);
            aaiClient.cancelAudioRecognize(currentRequestId);
            aaiClient.release();
            Log.d(TAG, "stopTencenVoice: " + state);
        }
    }

    LinkedHashMap<String, String> resMap = new LinkedHashMap<>();

    private String buildMessage(Map<String, String> msg) {

        StringBuffer stringBuffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = msg.entrySet().iterator();
        while (iter.hasNext()) {
            String value = iter.next().getValue();
            stringBuffer.append(value);
        }
        return stringBuffer.toString();
    }

    public void onEventMainThread(ShowProgress showProgress) {
        showProgress();
    }

    public void onEventMainThread(HideProgress hideProgress) {
        hideProgress();
    }

    private void loadOrder(String id, int type) {
        RequestParams params = new RequestParams(Constants.BASE_URL + Constants.ORDER);
        params.addParameter("action", "info");
        params.addParameter("oid", id);
        String phone = SPUtils.get(USER_PHONE, "");
        params.addParameter("phone", phone);
        Log.d(TAG, "loadData: " + params.toString());
        showProgress();
        x.http().post(params, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                WaresOrder order = readOrderJson(result, type);
                if (null == order || TextUtils.isEmpty(order.getName())) {
                    return true;
                }

                return false;
            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, " onSuccess: " + result);
                readOrderJson(result, type);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }


            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }


            @Override
            public void onFinished() {
                hideProgress();
            }
        });
    }

    public static final int SHOPKEEPER_DETAIL = 1;
    public static final int TO_POST = 2;
    public static final int CHECK_LOGISTIC = 3;

    private WaresOrder readOrderJson(String json, int type) {
        BaseCode<WaresOrder> base = new Gson().fromJson(
                json,
                new TypeToken<BaseCode<WaresOrder>>() {
                }.getType());
        if (base == null || base.getCode() != 0) {
            return null;
        }
        WaresOrder order = base.getData();
        Intent intent = new Intent(this, WaresOrderDetailctivity.class);
        intent.putExtra(WaresOrderDetailctivity.ORDER_DETAIL, order);
        switch (type) {
            case SHOPKEEPER_DETAIL:
                intent.putExtra(WaresOrderDetailctivity.SHOP_ORDER, true);
                break;
            case TO_POST:
                intent.setClass(this, ModifyLogisticActivity.class);
                break;
            case CHECK_LOGISTIC:
                intent.setClass(this, WaresLogistcDetailctivity.class);
                break;
        }
        startActivity(intent);
        return order;
    }

    private String trade;

    public void onEventMainThread(PublishNote var) {
        groupNote = var.getNote();
        if(TextUtils.isEmpty(var.getNote())){
            noteLayout.setVisibility(View.GONE);
        }else{
            noteLayout.setVisibility(View.VISIBLE);
            noteTextView.setText(var.getNote());
        }
    }
}