package xyz.parti.catan.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Message;
import xyz.parti.catan.ui.view.LooselyRelativeTimeTextView;
import xyz.parti.catan.ui.view.SmartTextView;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class MessageItem extends AbstractItem<MessageItem, MessageItem.ViewHolder> {
    private Message message;

    public MessageItem(Message message) {
        this.message = message;
    }

    @Override
    public int getType() {
        return R.id.message_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.message_item;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        if(message.user != null) {
            holder.messagableImageDraweeView.getHierarchy().setFailureImage(R.drawable.ic_account_circle_white_24dp);
            holder.messagableImageDraweeView.setImageURI(message.sender.image_url);
        } else {
            holder.messagableImageDraweeView.getHierarchy().setPlaceholderImage(R.drawable.img_sample_parti_logo);
            if(message.parti != null) {
                holder.messagableImageDraweeView.setImageURI(message.parti.logo_url);
            } else {
                holder.messagableImageDraweeView.getHierarchy().setPlaceholderImage(R.drawable.img_sample_parti_logo);
            }
        }

        holder.messageHeaderTextView.setText(message.header);
        holder.messageTitleSmartTextView.setNoImageRichText(message.title);
        if(TextUtils.isEmpty(message.body)) {
            holder.messageBodySmartTextView.setVisibility(View.GONE);
        } else {
            holder.messageBodySmartTextView.setVisibility(View.VISIBLE);
            holder.messageBodySmartTextView.setNoImageRichText(message.body);
        }
        holder.messageCreatedAtTimeTextView.setReferenceTime(message.created_at.getTime());
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    public Message getMessage() {
        return message;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.draweeview_message_image)
        SimpleDraweeView messagableImageDraweeView;
        @BindView(R.id.textview_message_header)
        TextView messageHeaderTextView;
        @BindView(R.id.timetextview_message_created_at)
        LooselyRelativeTimeTextView messageCreatedAtTimeTextView;
        @BindView(R.id.smarttextview_message_title)
        SmartTextView messageTitleSmartTextView;
        @BindView(R.id.smarttextview_message_body)
        SmartTextView messageBodySmartTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
