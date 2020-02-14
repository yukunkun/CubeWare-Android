package cube.ware.ui.main;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.base.BasePresenter;
import com.common.eventbus.Event;
import com.common.router.RouterUtil;
import cube.service.CubeEngine;
import cube.service.CubeError;
import cube.service.Session;
import cube.service.account.AccountListener;
import cube.ware.AppConstants;
import cube.ware.AppManager;
import cube.ware.R;
import cube.ware.common.MessageConstants;
import cube.ware.core.CubeCore;
import cube.ware.service.message.recent.RecentFragment;
import cube.ware.ui.contact.ContactFragment;
import cube.ware.ui.mine.MineFragment;
import cube.ware.utils.SpUtil;
import cube.ware.widget.tabbar.NavigateTabBar;

@Route(path = AppConstants.Router.MainActivity)
public class MainActivity extends BaseActivity implements AccountListener {

    private static final String MAIN_PAGE_MESSAGE    = "消息";
    private static final String MAIN_PAGE_CONFERENCE = "会议";
    private static final String MAIN_PAGE_CONTACTS   = "联系人";
    private static final String MAIN_PAGE_MINE       = "我的";

    private RecentFragment  mRecentFragment;//基于本地数据的最近会话列表
    //private ConferenceListFragment mConferenceFragment;
    private ContactFragment mContactMainFragment;
    private MineFragment    mPersonalFragment;

    private NavigateTabBar  mNavigateTabBar;    //底部NavigateTabBar
    private FragmentManager mFragmentManager;   // Fragment 管理器
    private Fragment        mCurrentFragment;   // 当前展示的Fragment
    private long            lastClickTimeStamp = 0;
    public  boolean         isForceSync        = true;//是否强制同步
    private String          aliDeviceToken;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void initView() {
        super.initView();
        this.mFragmentManager = getSupportFragmentManager();
        this.mNavigateTabBar = findViewById(R.id.navigate_tabbar);
        this.mNavigateTabBar.onRestoreInstanceState(mSavedInstanceState);
        this.mNavigateTabBar.addTab(new NavigateTabBar.TabParam(R.drawable.tab_msg_btn_unselected, R.drawable.tab_msg_btn_selected, MAIN_PAGE_MESSAGE, 0));
        this.mNavigateTabBar.addTab(new NavigateTabBar.TabParam(R.drawable.tab_consert_btn_unselected, R.drawable.tab_consert_btn_selected, MAIN_PAGE_CONFERENCE, 1));
        this.mNavigateTabBar.addTab(new NavigateTabBar.TabParam(R.drawable.ic_tab_more, R.drawable.ic_tab_more, null, 2));
        this.mNavigateTabBar.addTab(new NavigateTabBar.TabParam(R.drawable.tab_contact_btn_unselected, R.drawable.tab_contact_btn_selected, MAIN_PAGE_CONTACTS, 3));
        this.mNavigateTabBar.addTab(new NavigateTabBar.TabParam(R.drawable.tab_person_btn_unselected, R.drawable.tab_person_btn_selected, MAIN_PAGE_MINE, 4));
    }

    @Override
    protected void initListener() {
        CubeEngine.getInstance().getAccountService().addAccountListener(this);
        super.initListener();
        // NavigateTabBar 切换监听
        this.mNavigateTabBar.setTabSelectListener(new NavigateTabBar.OnTabSelectedListener() {

            @Override
            public void onTabSelected(NavigateTabBar.ViewHolder holder) {
                switch (holder.tabIndex) {
                    case 0: // 消息
                        if (null == mRecentFragment) {
                            mRecentFragment = new RecentFragment();
                        }
                        selectFragment(mRecentFragment, MAIN_PAGE_MESSAGE);
                        //                        if (null == mRecentSessionFragment) {
                        //                            mRecentSessionFragment = new RecentSessionFragment();
                        //                        }
                        //                        selectFragment(mRecentSessionFragment, MAIN_PAGE_MESSAGE);
                        break;
                    case 1: // 发现
                        //if (null == mConferenceFragment) {
                        //    mConferenceFragment = new ConferenceListFragment();
                        //}
                        //selectFragment(mConferenceFragment, MAIN_PAGE_CONFERENCE);
                        break;
                    case 2: // + 扩展
                        AddDialogFragment addDialogFragment = AddDialogFragment.getInstance();
                        addDialogFragment.show(getSupportFragmentManager(), "");
                        break;
                    case 3: // 联系人
                        if (null == mContactMainFragment) {
                            mContactMainFragment = new ContactFragment();
                        }
                        selectFragment(mContactMainFragment, MAIN_PAGE_CONTACTS);
                        break;
                    case 4: // 我的
                        if (null == mPersonalFragment) {
                            mPersonalFragment = new MineFragment();
                        }
                        selectFragment(mPersonalFragment, MAIN_PAGE_MINE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected boolean openEventBus() {
        return true;
    }

    @Override
    public void onReceiveEvent(Event event) {
        switch (event.eventName) {
            case MessageConstants.Event.EVENT_UNREAD_MESSAGE_SUM:
                mNavigateTabBar.setRedNum(0, (Integer) event.data);
                break;

            default:
                break;
        }
    }

    @Override
    protected void initData() {
        // 打开默认Fragment
        mRecentFragment = new RecentFragment();
        selectFragment(mRecentFragment, MAIN_PAGE_MESSAGE);
    }

    /**
     * 切换 Fragment
     */
    private void selectFragment(Fragment fragment, String tag) {
        FragmentTransaction mTransaction = mFragmentManager.beginTransaction();
        if (fragment != mCurrentFragment) {
            if (!fragment.isAdded()) {
                mTransaction.add(R.id.main_container, fragment, tag);
            }
            if (mCurrentFragment != null) {
                mTransaction.hide(mCurrentFragment);
            }
            mTransaction.show(fragment).commitAllowingStateLoss();
        }
        mCurrentFragment = fragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        CubeEngine.getInstance().getAccountService().removeAccountListener(this);
        super.onDestroy();
    }

    @Override
    public void onLogin(Session session) {
        SpUtil.setCubeId(session.getCubeId());
        SpUtil.setUserName(session.getDisplayName());
        SpUtil.setUserAvator(AppManager.getAvatarUrl() + session.getCubeId());
        CubeCore.getInstance().setCubeId(session.getCubeId());
    }

    @Override
    public void onLogout(Session session) {
        //退出登录，清空SP
        SpUtil.clear();
        RouterUtil.navigation(this, AppConstants.Router.AppIdActivity);
        finish();
    }

    @Override
    public void onAccountFailed(CubeError cubeError) {

    }
}
