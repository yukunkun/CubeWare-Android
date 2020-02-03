package cube.ware.service.message.chat.activity.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.mvp.base.BasePresenter;
import com.common.mvp.rx.RxPermissionUtil;
import com.common.utils.utils.ToastUtil;
import com.common.utils.utils.log.LogUtil;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import cube.service.message.Receiver;
import cube.ware.core.CubeConstants;
import cube.ware.core.CubeCore;
import cube.ware.data.model.dataModel.enmu.CubeSessionType;
import cube.ware.data.repository.CubeUserRepository;
import cube.ware.data.room.model.CubeMessage;
import cube.ware.data.room.model.CubeUser;
import cube.ware.service.message.R;
import cube.ware.service.message.chat.BaseChatActivity;
import cube.ware.service.message.chat.ChatCustomization;
import cube.ware.service.message.chat.activity.file.FileActivity;
import cube.ware.service.message.chat.message.MessageFragment;
import cube.ware.service.message.chat.panel.input.InputPanel;
import cube.ware.service.message.manager.MessageManager;
import cube.ware.utils.GlideImageLoader;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by dth
 * Des: 群组聊天activity
 * Date: 2018/9/10.
 */
@Route(path = CubeConstants.Router.GroupChatActivity)
public class GroupChatActivity extends BaseChatActivity implements InputPanel.OnBottomNavigationListener {
    private ImagePicker mImagePicker;

    private static final int REQUEST_CODE_CAMERA_IMAGE = 1; // 拍照图片
    private static final int REQUEST_CODE_LOCAL_IMAGE  = 2;  // 本地图片
    private static final int REQUEST_CODE_LOCAL_FILE   = 3;  // 本地文件

    @Override
    protected int getContentViewId() {
        return R.layout.activity_p2p_chat;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void initView() {
        //        ARouter.getInstance().inject(this);
    }

    public static void start(Context context, String chatId, String name, ChatCustomization chatCustomization, long messageSn) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CHAT_ID, chatId);
        intent.putExtra(EXTRA_CHAT_NAME, name);
        intent.putExtra(EXTRA_CHAT_CUSTOMIZATION, chatCustomization);
        intent.putExtra(EXTRA_CHAT_MESSAGE, messageSn);
        intent.setClass(context, GroupChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void initToolBar() {

    }

    @Override
    protected MessageFragment buildFragment() {
        Bundle arguments = getIntent().getExtras();
        MessageFragment fragment = GroupMessageFragment.newInstance(CubeSessionType.Group, arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        fragment.setBottomNavigationListener(this);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LOCAL_FILE: { // 发送文件
                if (resultCode == FileActivity.TAKE_FILE_CODE && null != data) {
                    ArrayList<String> filePathList = data.getStringArrayListExtra(FileActivity.TAKE_FILE_LIST);
                    if (null != filePathList && !filePathList.isEmpty()) {
                        Observable.from(filePathList).subscribe(new Action1<String>() {
                            @Override
                            public void call(String filePath) {
                                MessageManager.getInstance().sendFileMessage(mContext, CubeSessionType.Group, new Receiver(mChatId, mChatName), filePath, isAnonymous, false);
                            }
                        });
                    }
                }
                break;
            }
            case REQUEST_CODE_LOCAL_IMAGE: {    // 发送本地图片
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS && null != data) {
                    final List<ImageItem> imageItemList = data.getParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    final boolean isOrigin = data.getBooleanExtra("isOrigin", false);
                    mImagePicker.clearSelectedImages();
                    if (null != imageItemList && !imageItemList.isEmpty()) {
                        Observable.from(imageItemList).subscribe(new Observer<ImageItem>() {
                            @Override
                            public void onNext(ImageItem imageItem) {
                                LogUtil.i("选中图片的路径：" + imageItem.path);
                                String imagePath = imageItem.path;
                                MessageManager.getInstance().sendFileMessage(mContext, CubeSessionType.Group, new Receiver(mChatId, mChatName), imagePath, isAnonymous, isOrigin);
                            }

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ToastUtil.showToast(GroupChatActivity.this, 0, "图片无效");
                            }
                        });
                    }
                }
                break;
            }
        }
    }

    public String getChatId() {
        return this.mChatId;
    }

    @Override
    public void onCameraListener() {

    }

    @Override
    public void onSendFileListener() {
        RxPermissionUtil.requestStoragePermission(this).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(GroupChatActivity.this, FileActivity.class);
                    intent.putExtra(FileActivity.REQUEST_CODE, REQUEST_CODE_LOCAL_FILE);
                    startActivityForResult(intent, REQUEST_CODE_LOCAL_FILE);
                    overridePendingTransition(R.anim.activity_open, 0);
                }
                else {
                    ToastUtil.showToast(GroupChatActivity.this, 0, getString(R.string.request_storage_permission));
                }
            }
        });
    }

    @Override
    public void onSendImageListener() {

        mImagePicker = ImagePicker.getInstance();
        mImagePicker.setImageLoader(new GlideImageLoader()); // 设置图片加载器
        mImagePicker.setMultiMode(true); // 设置为多选模式
        mImagePicker.setShowCamera(true);    // 设置显示相机
        mImagePicker.setSelectLimit(9);
        mImagePicker.setCrop(false);     // 设置拍照后不裁剪

        RxPermissionUtil.requestStoragePermission(this).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) {
                    Intent intent = new Intent(GroupChatActivity.this, ImageGridActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_LOCAL_IMAGE);
                    overridePendingTransition(R.anim.activity_open, 0);
                }
                else {
                    ToastUtil.showToast(GroupChatActivity.this, 0, getString(R.string.request_storage_permission));
                }
            }
        });
    }

    @Override
    public void onAvatarClicked(final Context context, CubeMessage cubeMessage) {
        //        ToastUtil.showToast(App.getContext(),"点击头像");
        if (!TextUtils.equals(CubeCore.getInstance().getCubeId(), cubeMessage.getSenderId())) {
            CubeUserRepository.getInstance().queryUser(cubeMessage.getSenderId()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<CubeUser>() {
                @Override
                public void call(CubeUser cubeUser) {
                    ARouter.getInstance().build(CubeConstants.Router.FriendDetailsActivity).withObject("user", cubeUser).navigation(context);
                }
            });
        }
    }

    @Override
    public void onAvatarLongClicked(Context context, CubeMessage cubeMessage) {
        //        ToastUtil.showToast(App.getContext(),"长按头像");
    }
}
