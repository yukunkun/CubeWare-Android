package cube.ware.ui.group.details;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.mvp.base.BaseActivity;
import com.common.sdk.RouterUtil;
import com.common.utils.utils.ScreenUtil;
import com.common.utils.utils.ToastUtil;
import com.common.utils.utils.glide.GlideUtil;
import com.common.utils.utils.log.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cube.service.CubeEngine;
import cube.service.common.CubeCallback;
import cube.service.common.model.CubeError;
import cube.service.group.model.Group;
import cube.service.group.model.Member;
import cube.service.user.model.User;
import cube.ware.App;
import cube.ware.AppConstants;
import cube.ware.R;
import cube.ware.service.group.GroupHandle;
import cube.ware.ui.chat.activity.group.GroupChatCustomization;
import cube.ware.ui.contact.select.SelectContactActivity;
import cube.ware.ui.group.adapter.GroupListenerAdapter;
import cube.ware.ui.group.adapter.GroupMembersAdapter;
import cube.ware.utils.SpUtil;
import cube.ware.widget.DividerItemDecoration;
import cube.ware.widget.bottomPopupDialog.BottomPopupDialog;

/**
 * Created by dth
 * Des: 群组详情页面
 * Date: 2018/8/28.
 */
@Route(path = AppConstants.Router.GroupDetailsActivity)
public class GroupDetailsActivity extends BaseActivity<GroupDetailsContract.Presenter> implements GroupDetailsContract.View {

    private ImageView               mTitleBack;
    private ImageView               mTitleIv;
    private ImageView               mTitleMore;
    private RelativeLayout          mToolbarLayout;
    private ImageView               mFaceIv;
    private TextView                mDisplayNameTv;
    private CollapsingToolbarLayout mCoolToolbarLayout;
    private TextView                mSendMessageTv;
    private AppBarLayout            mAppbarLayout;
    private TextView                mGroupNumCodeTv;
    private RelativeLayout          mGroupNumCodeRl;
    private TextView                mGroupChatNameTv;
    private ImageView               mGroupChatArrowIv;
    private RelativeLayout          mGroupNameRl;
    private RecyclerView            mRecyclerView;
    private LinearLayout            mGroupMemberRl;
    private LinearLayout            mDetailGroupLayout;

    @Autowired(name = "group")
    public Group mGroup;
    @Autowired(name = "groupId")
    public String mGroupId;
    private GroupMembersAdapter mGroupMembersAdapter;
    List<Member> mMembersData = new ArrayList<>();

    @Override
    protected int getContentViewId() {
        return R.layout.activity_group_details;
    }

    @Override
    protected GroupDetailsContract.Presenter createPresenter() {
        return new GroupDetailsPresenter(this, this);
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);

        mTitleBack = (ImageView) findViewById(R.id.title_back);
        mTitleIv = (ImageView) findViewById(R.id.title_iv);
        mTitleMore = (ImageView) findViewById(R.id.title_more);
        mToolbarLayout = (RelativeLayout) findViewById(R.id.toolbar_layout);
        mFaceIv = (ImageView) findViewById(R.id.face_iv);
        mDisplayNameTv = (TextView) findViewById(R.id.display_name_tv);
        mCoolToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.cool_toolbar_layout);
        mSendMessageTv = (TextView) findViewById(R.id.send_message_tv);
        mAppbarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mGroupNumCodeTv = (TextView) findViewById(R.id.group_num_code_tv);
        mGroupNumCodeRl = (RelativeLayout) findViewById(R.id.group_num_code_rl);
        mGroupChatNameTv = (TextView) findViewById(R.id.group_chat_name_tv);
        mGroupChatArrowIv = (ImageView) findViewById(R.id.group_chat_arrow_iv);
        mGroupNameRl = (RelativeLayout) findViewById(R.id.group_name_rl);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mGroupMemberRl = (LinearLayout) findViewById(R.id.group_member_rl);
        mDetailGroupLayout = (LinearLayout) findViewById(R.id.detail_group_layout);
        mGroupMembersAdapter = new GroupMembersAdapter(R.layout.item_group_members);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 6);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST, ScreenUtil.dip2px(5), getResources().getColor(R.color.white));
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mGroupMembersAdapter);
    }

    @Override
    protected void initData() {
        LogUtil.d("Group: " + mGroup + " --- mGroupId: "+mGroupId);
        CubeEngine.getInstance().getGroupService().queryGroupDetails(mGroupId, new CubeCallback<Group>() {
            @Override
            public void onSucceed(Group group) {
                mGroup = group;
                if (mGroup == null) {
                    ToastUtil.showToast(App.getContext(), "查询结果为null");
                    return;
                }

               showData(group);
            }

            @Override
            public void onFailed(CubeError cubeError) {

                LogUtil.e(cubeError.toString());
            }
        });
    }

    @Override
    protected void initListener() {
        GroupHandle.getInstance().addGroupListener(groupListenerAdapter);
        mTitleBack.setOnClickListener(this);
        mTitleMore.setOnClickListener(this);
        mSendMessageTv.setOnClickListener(this);
        mGroupNameRl.setOnClickListener(this);

        mGroupMembersAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                List<Member> data = mGroupMembersAdapter.getData();
                if (position == data.size() - 1) {
                    ArrayList<String> cubeIds = new ArrayList<>();
                    for (Member datum : data) {
                        cubeIds.add(datum.cubeId);
                    }
                    ARouter.getInstance().build(AppConstants.Router.SelectContactActivity)
                            .withObject(SelectContactActivity.NOT_CHECKED_LIST, cubeIds)
                            .withString(SelectContactActivity.GROUP_ID, mGroup.groupId)
                            .withInt(SelectContactActivity.TYPE, 1)
                            .navigation();
                } else {
//                    Member member = data.get(position);
//
//                    ARouter.getInstance().build(AppConstants.Router.FriendDetailsActivity)
//                            .withObject("user", member)
//                            .navigation();
                }
            }
        });

        mGroupMembersAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                List<Member> data = mGroupMembersAdapter.getData();
                Member member = data.get(position);
                if (position != data.size() - 1 && isMaster(mGroup) && !TextUtils.equals(member.cubeId,SpUtil.getCubeId())) {
//                    new AlertDialog.Builder(GroupDetailsActivity.this)
//                            .setMessage("确认删除群成员" + member.cubeId + "吗")
//                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    CubeEngine.getInstance().getGroupService().removeMembers(mGroupId, Collections.singletonList(member.cubeId));
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .create()
//                            .show();
                    showPop(mGroupId,member.cubeId);
                }
                return true;
            }
        });

    }

    private void showData(Group group) {
        //自己创建的群才能修改群名称
        if (group.founder.equals(SpUtil.getCubeId())){
            mGroupChatArrowIv.setVisibility(View.VISIBLE);
        }else{
            mGroupChatArrowIv.setVisibility(View.GONE);
        }
        mGroup = group;
        GlideUtil.loadCircleImage(group.avatar, mContext, mFaceIv, R.drawable.default_head_group);
        mDisplayNameTv.setText(group.displayName);
        mGroupChatNameTv.setText(group.displayName);
        mGroupNumCodeTv.setText(group.groupId);

        mMembersData.clear();
        mMembersData.addAll(group.masters);
        mMembersData.addAll(group.members);
        refreshData(mMembersData);

    }

    GroupListenerAdapter groupListenerAdapter = new GroupListenerAdapter() {
        @Override
        public void onMasterAdded(Group group, User user, List<User> list) {
            showData(group);
        }

        @Override
        public void onMasterRemoved(Group group, User user, List<User> list) {
            showData(group);
        }

        @Override
        public void onMemberAdded(Group group, User user, List<User> list) {
            showData(group);
        }

        @Override
        public void onMemberRemoved(Group group, User user, List<User> list) {
            showData(group);
        }

        @Override
        public void onGroupFailed(Group group, CubeError cubeError) {
            ToastUtil.showToast(App.getContext(),cubeError.desc);
        }

        @Override
        public void onGroupDestroyed(Group group, User user) {
            finish();
        }

        @Override
        public void onGroupQuited(Group group, User from) {
            if (TextUtils.equals(from.cubeId, SpUtil.getCubeId())) {
                finish();
            }
        }

        @Override
        public void onGroupUpdated(Group group, User user) {
            showData(group);
        }
    };

    @Override
    protected void onDestroy() {
       GroupHandle.getInstance().removeGroupListener(groupListenerAdapter);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                finish();
                break;
            case R.id.title_more:
                if (isMaster(mGroup)) {
                    showMasterMorePop();
                } else {
                    showMemberMorePop();
                }
                break;
            case R.id.send_message_tv:
                ARouter.getInstance().build(AppConstants.Router.GroupChatActivity)
                        .withString(AppConstants.EXTRA_CHAT_ID,mGroupId)
                        .withString(AppConstants.EXTRA_CHAT_NAME,TextUtils.isEmpty(mGroup.displayName) ? mGroupId : mGroup.displayName)
                        .withSerializable(AppConstants.EXTRA_CHAT_CUSTOMIZATION,new GroupChatCustomization())
//                        .withFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        .navigation();
                break;
            case R.id.group_name_rl:
                if (isMaster(mGroup)) {
                    Bundle bundle=new Bundle();
                    bundle.putString("displayname",String.valueOf(mGroup.displayName));
                    bundle.putSerializable("group",mGroup);
                    bundle.putInt("type",1);
                    RouterUtil.navigation(this,bundle, AppConstants.Router.ModifyNameActivity);
//                    showUpdateGroupName();
                }
                break;
        }



    }

    private boolean isMaster(Group group) {
        String cubeId = SpUtil.getCubeId();
        return TextUtils.equals(group.owner, cubeId) || group.masters.contains(cubeId);
    }

    private void refreshData(List<Member> members) {
        members.add(new Member("我是加号"));
        mGroupMembersAdapter.replaceData(members);
    }

    /**
     * 弹出更多对话框
     */
    private void showMemberMorePop() {
        final BottomPopupDialog bottomPopupDialog;
        List<String> bottomDialogContents;//弹出列表的内容
        bottomDialogContents = new ArrayList<>();
        bottomDialogContents.add("退出该群");
        bottomPopupDialog = new BottomPopupDialog(this, bottomDialogContents);
        bottomPopupDialog.showCancelBtn(true);
        bottomPopupDialog.setCancelable(true);
        bottomPopupDialog.setRedPosition(0);
        bottomPopupDialog.setRedPosition(1);
        bottomPopupDialog.show();
        bottomPopupDialog.setOnItemClickListener(new BottomPopupDialog.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (position) {
                    case 0:
                        CubeEngine.getInstance().getGroupService().quit(mGroup.groupId);
                        bottomPopupDialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showMasterMorePop() {
        final BottomPopupDialog bottomPopupDialog;
        List<String> bottomDialogContents;//弹出列表的内容
        bottomDialogContents = new ArrayList<>();
        bottomDialogContents.add("解散该群");
        bottomDialogContents.add("退出该群");
        bottomPopupDialog = new BottomPopupDialog(this, bottomDialogContents);
        bottomPopupDialog.showCancelBtn(true);
        bottomPopupDialog.setCancelable(true);
        bottomPopupDialog.setRedPosition(0);
        bottomPopupDialog.setRedPosition(1);
        bottomPopupDialog.show();
        bottomPopupDialog.setOnItemClickListener(new BottomPopupDialog.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (position) {
                    case 0:
                        CubeEngine.getInstance().getGroupService().destroy(mGroup.groupId);
                        bottomPopupDialog.dismiss();
                        break;
                    case 1:
                        ToastUtil.showToast(mContext,position+"");
                        CubeEngine.getInstance().getGroupService().quit(mGroup.groupId);
                        bottomPopupDialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showPop(String groupId, String cubeId) {
        final BottomPopupDialog bottomPopupDialog;
        List<String> bottomDialogContents;//弹出列表的内容
        bottomDialogContents = new ArrayList<>();
        if (TextUtils.equals(mGroup.owner, SpUtil.getCubeId())) {
            if (mGroup.masters.contains(cubeId) ) {
                bottomDialogContents.add("删除管理员");
            } else {
                bottomDialogContents.add("添加管理员");
            }
        }
        bottomDialogContents.add("删除群成员");
        bottomPopupDialog = new BottomPopupDialog(this, bottomDialogContents);
        bottomPopupDialog.showCancelBtn(true);
        bottomPopupDialog.setCancelable(true);
        bottomPopupDialog.setRedPosition(0);
        bottomPopupDialog.setRedPosition(1);
        bottomPopupDialog.show();
        bottomPopupDialog.setOnItemClickListener(new BottomPopupDialog.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (position) {
                    case 0:
                        if (mGroup.masters.contains(cubeId)) {
                            CubeEngine.getInstance().getGroupService().removeMaster(groupId,Collections.singletonList(cubeId));
                        } else {
                            CubeEngine.getInstance().getGroupService().addMaster(groupId,Collections.singletonList(cubeId));
                        }
                        bottomPopupDialog.dismiss();
                        break;
                    case 1:
                        CubeEngine.getInstance().getGroupService().removeMembers(mGroupId, Collections.singletonList(cubeId));
                        bottomPopupDialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showUpdateGroupName() {
        View view = View.inflate(this, R.layout.dialog_update_group_name, null);
        EditText editText = view.findViewById(R.id.et_group_name);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setTitle("修改群名称")
                .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString();
                        if (!TextUtils.isEmpty(name)) {
                            mGroup.displayName = name;
                            CubeEngine.getInstance().getGroupService().update(mGroup);
                            dialog.dismiss();
                        } else {
                            ToastUtil.showToast(App.getContext(), "群名称不能为空");
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();

    }
}
