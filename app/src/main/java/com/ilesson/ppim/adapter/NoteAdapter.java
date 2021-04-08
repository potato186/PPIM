package com.ilesson.ppim.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ilesson.ppim.R;
import com.ilesson.ppim.activity.ImagePreviewActivity;
import com.ilesson.ppim.activity.MapLocationActivity;
import com.ilesson.ppim.activity.NoteActivity;
import com.ilesson.ppim.activity.TbsFileActivity;
import com.ilesson.ppim.entity.LocalMedia;
import com.ilesson.ppim.entity.NoteInfo;
import com.ilesson.ppim.utils.FileTool;
import com.ilesson.ppim.utils.PPScreenUtils;
import com.ilesson.ppim.view.PlayView;
import com.noober.menu.FloatMenu;
import com.tencent.smtt.sdk.TbsVideo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.message.LocationMessage;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.ilesson.ppim.activity.ImagePreviewActivity.ONLY_PREVIEW;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_FILE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_IMAGE;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_LOCATION;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_TEXT;
import static com.ilesson.ppim.service.FavoriteHelper.TYPE_VOICE;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<NoteInfo> resultList = new ArrayList<>();
    public static int editIndex;
    public int recordIndex;
    private HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
    private RecyclerView recyclerView;
    private NoteActivity noteActivity;
    private List<EditText> editTexts = new ArrayList<>();
    private int screenWidth;

//    public enum ITEM_TYPE {
//        ITEM_TYPE_TEXT,
//        ITEM_TYPE_IMAGE,
//        ITEM_TYPE_LOCATION,
//        ITEM_TYPE_FILE,
//        ITEM_TYPE_VOICE
//    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public NoteAdapter(Context context, List<NoteInfo> datas) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        resultList = datas;
        editIndex = resultList.size();
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public NoteAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        screenWidth = PPScreenUtils.getScreenWidth(context);
    }

    public void setNoteActivity(NoteActivity noteActivity) {
        this.noteActivity = noteActivity;
    }

    public List<NoteInfo> getResultList() {
        return resultList;
    }

    public void setResultList(List<NoteInfo> resultList) {
        this.resultList = resultList;
        editIndex = resultList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TEXT) {
            return new TextHolder(mLayoutInflater.inflate(R.layout.item_note_edittext, parent, false));
        } else if (viewType == TYPE_IMAGE) {
            return new ImageHolder(mLayoutInflater.inflate(R.layout.item_note_image, parent, false));
        } else if (viewType == TYPE_LOCATION) {
            return new LocationHolder(mLayoutInflater.inflate(R.layout.item_note_location, parent, false));
        } else if (viewType == TYPE_FILE) {
            return new FileHolder(mLayoutInflater.inflate(R.layout.item_note_file, parent, false));
        } else if (viewType == TYPE_VOICE) {
            return new VoiceHolder(mLayoutInflater.inflate(R.layout.item_note_voice, parent, false));
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NoteInfo noteInfo = resultList.get(position);
        if (holder instanceof TextHolder) {
            ((TextHolder) holder).editText.setText(noteInfo.getText());
        } else if (holder instanceof ImageHolder) {
//            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
//            builder.showImageOnLoading(R.mipmap.default_icon)
//                    .cacheInMemory(true).cacheOnDisk(true);
//            ImageLoader.getInstance().displayImage(noteImage.getUrl(), ((ImageHolder) holder).imageView,
//                    builder.build());
            final ImageView imageView = ((ImageHolder) holder).imageView;
            Glide.with(mContext).asBitmap().load(noteInfo.getUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    int vw = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
                    float scale = (float) vw / (float) resource.getWidth();
                    int vh = Math.round(resource.getHeight() * scale);
                    int width = (int) (screenWidth * 0.95);
                    int height = resource.getHeight() * width / resource.getWidth();
//                    params.height = vh + imageView.getPaddingTop() + imageView.getPaddingBottom();
                    params.width = width;
                    params.height = height;
                    imageView.setLayoutParams(params);
                    imageView.setImageBitmap(resource);
                }
            });
        } else if (holder instanceof FileHolder) {
            FileHolder fileHolder = (FileHolder) holder;
            ImageView imageView = fileHolder.imageView;
            if (noteInfo.getUrl().endsWith("zip")) {
                imageView.setImageResource(R.mipmap.zip_icon);
            } else if (noteInfo.getUrl().endsWith("doc") || noteInfo.getUrl().endsWith("docx") || noteInfo.getUrl().endsWith("docm") || noteInfo.getUrl().endsWith("dotx") || noteInfo.getUrl().endsWith("dotm")) {
                imageView.setImageResource(R.mipmap.doc_icon);
            } else if (noteInfo.getUrl().endsWith("xls") || noteInfo.getUrl().endsWith("xlsx")) {
                imageView.setImageResource(R.mipmap.xls_icon);
            } else if (noteInfo.getUrl().endsWith("ppt") || noteInfo.getUrl().endsWith("pptx")) {
                imageView.setImageResource(R.mipmap.ppt_icon);
            } else {
                imageView.setImageResource(R.mipmap.other_icon);
            }
            File file = new File(noteInfo.getUrl());
            fileHolder.nameTv.setText(file.getName());
            fileHolder.sizeTv.setText(FileTool.getFormatSize(file.length()));
        } else if (holder instanceof LocationHolder) {
            LocationHolder locationHolder = (LocationHolder) holder;
            locationHolder.addressTv.setText(noteInfo.getAddress());
//            locationHolder.nameTv.setText(noteLocation.getDesc());
        } else if (holder instanceof VoiceHolder) {
            VoiceHolder voiceHolder = (VoiceHolder) holder;
//            long totalTime = noteVoice.getTime();
//            if (totalTime > 0) {
//                voiceHolder.voiceTime.setText(Dateuitls.formatSeconds(totalTime));
//            }
            voiceHolder.playView.setUrl(noteInfo.getUrl());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return resultList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

    public void itemFocus() {
        int index = editIndex >= resultList.size() ? resultList.size() - 1 : editIndex;
        int pos = index>=editTexts.size()?editTexts.size()-1:index;
        if(pos<=0)pos=0;
        EditText editText = editTexts.get(pos);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
        showInput(editText);
        recyclerView.scrollTo(0, 50);
    }

    class BaseHolder extends RecyclerView.ViewHolder {
        EditText editText;

        BaseHolder(View view) {
            super(view);
        }
    }

    public class TextHolder extends BaseHolder {
        TextHolder(View view) {
            super(view);
            editText = view.findViewById(R.id.note_edit);
            setFocusAction(editText, getLayoutPosition());
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String content = s.toString();
                    if (TextUtils.isEmpty(content)) {
                    } else {
                        NoteInfo noteInfo = resultList.get(getLayoutPosition());
                        noteInfo.setText(content);
                        if(!editText.hasFocus()){
                            editText.requestFocus();
                            editText.setSelection(editText.getText().length());
                        }
                    }
                }
            });
        }
    }

    public class NoteHolder extends BaseHolder {
        EditText editText;
        ConstraintLayout layout;
        View itemLayout;
        TextView cover;

        NoteHolder(View view) {
            super(view);
        }

        public void setAction() {
            if (null != layout) {
                setOnLongClickAction(layout);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != editText) {
                            editText.requestFocus();
                            showInput(editText);
                        }
                    }
                });
            }
            if (null != itemLayout) {
                setOnLongClickAction(itemLayout);
//                itemLayout.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (null != editText) {
//                            editText.requestFocus();
//                            showInput(editText);
//                        }
//                    }
//                });
            }
        }

        public void setOnLongClickAction(View view) {
            if (null == view) {
                return;
            }
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    FloatMenu floatMenu = new FloatMenu(noteActivity);
                    floatMenu.items("删除");
                    floatMenu.show(noteActivity.point);
                    floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                        @Override
                        public void onClick(View v, int position) {
                            resultList.remove(getLayoutPosition());
                            editTexts.remove(getLayoutPosition());
                            notifyItemRemoved(getLayoutPosition());
                        }
                    });
                    return false;
                }
            });
        }
    }

    private void setFocusAction(EditText editText, final int postion) {
        editTexts.add(editText);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editIndex = postion;
                    recyclerView.scrollTo(0, 50);
                }
            }
        });
    }

    private static final String TAG = "NoteAdapter";

    public void setSelect(final NoteHolder holder) {
        setFocusAction(holder.editText, holder.getLayoutPosition());
//        holder.editText.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_DEL) {
//                    if (holder.editText.getText().toString().equals("")) {
//                        if (holder.cover.getVisibility() == View.GONE) {
//                            resultList.remove(holder.getLayoutPosition());
//                            notifyItemRemoved(holder.getLayoutPosition());
//                        } else {
//                            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.cover.getLayoutParams();
//                            layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
//                            layoutParams.height = holder.layout.getWidth() + 50;
//                            holder.cover.setLayoutParams(layoutParams);
//                            holder.cover.setVisibility(View.VISIBLE);
//                        }
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (TextUtils.isEmpty(content)) {

                } else {
                    int next = holder.getLayoutPosition() + 1;
                    if (resultList.size() > next) {
                        NoteInfo note = resultList.get(next);
                        if (note.getType()==TYPE_TEXT) {
                            String text = content + "\n" + note.getText();
                            note.setText(text);
                        } else {
                            NoteInfo noteText = new NoteInfo();
                            noteText.setText(content);
                            resultList.add(next, noteText);
                        }
                    } else {
                        NoteInfo noteText = new NoteInfo();
                        noteText.setText(content);
                        resultList.add(next, noteText);
                    }
                    editIndex = next;
                    holder.editText.setText("");
//                    notifyItemInserted(next);
                    notifyDataSetChanged();
//                    itemFocus();
                }
            }
        });
    }

    public class ImageHolder extends NoteHolder {
        ImageView imageView;

        ImageHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.note_image);
            editText = view.findViewById(R.id.note_editting);
            layout = view.findViewById(R.id.layout);
            cover = view.findViewById(R.id.cover);
            cover.setVisibility(View.GONE);
            setAction();
            setSelect(this);
            setOnLongClickAction(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImagePreviewActivity.class);
                    intent.putExtra(ONLY_PREVIEW, true);
                    List<LocalMedia> list = new ArrayList<LocalMedia>();
                    LocalMedia localMedia = new LocalMedia(resultList.get(getLayoutPosition()).getUrl().replace("file:///", ""));
                    list.add(localMedia);
                    intent.putExtra(ImagePreviewActivity.EXTRA_PREVIEW_LIST, (ArrayList) list);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    public class FileHolder extends NoteHolder {
        ImageView imageView;
        TextView nameTv;
        TextView sizeTv;

        FileHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.loction_icon);
            editText = view.findViewById(R.id.note_editting);
            layout = view.findViewById(R.id.layout);
            itemLayout = view.findViewById(R.id.item_layout);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(mContext, TbsFileActivity.class);
                    NoteInfo noteInfo = (NoteInfo) resultList.get(getLayoutPosition());
                    if (noteInfo.getUrl().contains("http")) {
                        intent.putExtra(TbsFileActivity.FILE_URL, noteInfo.getUrl());
                    } else {
                        intent.putExtra(TbsFileActivity.FILE_LOCAL_URL, noteInfo.getUrl());
                    }
                    String mFileName = noteInfo.getName();
                    if (mFileName.endsWith(".mp4") || mFileName.endsWith(".3gp") || mFileName.endsWith(".avi") || mFileName.endsWith(".mkv") || mFileName.endsWith(".rmvb") && TbsVideo.canUseTbsPlayer(mContext)) {
                        TbsVideo.openVideo(mContext, noteInfo.getUrl());
                        return;
                    }
                    intent.putExtra(TbsFileActivity.FILE_NAME, noteInfo.getName());
                    mContext.startActivity(intent);
                }
            });
            setAction();
            setSelect(this);
            nameTv = view.findViewById(R.id.name);
            sizeTv = view.findViewById(R.id.size);
        }
    }

    public class VoiceHolder extends NoteHolder {
//        TextView voiceTime;
        TextView voiceState;
        PlayView playView;
//        View recordView;

        VoiceHolder(View view) {
            super(view);
//            voiceTime = view.findViewById(R.id.record_time);
//            voiceState = view.findViewById(R.id.voice_state);
            editText = view.findViewById(R.id.note_editting);
            playView = view.findViewById(R.id.play_view);
//            recordView = view.findViewById(R.id.item_layout);
            layout = view.findViewById(R.id.layout);
            itemLayout = playView;
            setOnLongClickAction(playView);
            setAction();
//            int pos = getLayoutPosition();
//            NoteVoice noteVoice = (NoteVoice) resultList.get(pos);
//            if (TextUtils.isEmpty(noteVoice.getUrl())) {
//                recordView.setVisibility(View.VISIBLE);
//                playView.setVisibility(View.GONE);
//                handler.sendEmptyMessageDelayed(0, 500);
//            } else {
                playView.setVisibility(View.VISIBLE);
//                recordView.setVisibility(View.GONE);

//            }
            setSelect(this);

        }

        private boolean show;
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                show = !show;
                if (show) {
                    voiceState.setVisibility(View.VISIBLE);
                } else {
                    voiceState.setVisibility(View.GONE);
                }
            }
        };
    }

    public class LocationHolder extends NoteHolder {
        TextView nameTv;
        TextView addressTv;

        LocationHolder(View view) {
            super(view);
            editText = view.findViewById(R.id.note_editting);
            layout = view.findViewById(R.id.layout);
            itemLayout = view.findViewById(R.id.item_layout);
            setAction();
            setSelect(this);
            nameTv = view.findViewById(R.id.name);
            addressTv = view.findViewById(R.id.address);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MapLocationActivity.class);
                    NoteInfo note = resultList.get(getLayoutPosition());
                    double longitude = note.getLongitude();
                    double latitude = note.getLatitude();
                    String adderss = note.getAddress();
                    Uri uri = Uri.parse("http://api.map.baidu.com/staticimage?width=300&height=200&center="+ longitude + "," + latitude + "&zoom=17&markers=" + longitude + "," + latitude + "&markerStyles=m,A");
                    LocationMessage locationMessage = LocationMessage.obtain(latitude, longitude, adderss, uri);
                    intent.putExtra("location", locationMessage);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    public void showInput(View view) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setFocusPosition(BaseHolder holder) {

    }
}
