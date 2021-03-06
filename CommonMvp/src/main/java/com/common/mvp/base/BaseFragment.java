package com.common.mvp.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import com.common.utils.utils.log.LogUtil;

/**
 * Fragment基类
 *
 * @author LiuFeng
 * @date 2017-11-01
 */
public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements View.OnClickListener,BaseView {

    protected View     mRootView;
    protected Activity mActivity;

    protected P mPresenter;

    private int containerId;  //fragment布局资源id

    private boolean destroyed;  //是否销毁
    private boolean mIsViewPrepared;    // 标识fragment视图已经初始化完毕
    private boolean mHasLoadData;   // 标识已经触发过懒加载数据

    protected final boolean isDestroyed() {
        return destroyed;
    }

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        destroyed = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.i("onCreateView");
        if (this.mRootView == null) {
            this.mRootView = inflater.inflate(this.getContentViewId(), container, false);
            this.initView();
            this.initListener();
        }
        return this.mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LogUtil.i("onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        this.mPresenter = this.createPresenter();
        this.mIsViewPrepared = true;
        this.lazyLoadData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            LogUtil.i("isVisibleToUser=" + isVisibleToUser);
            this.lazyLoadData();
        }
    }

    @Override
    public void onResume() {
        LogUtil.i("onResume");
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        LogUtil.i("onDestroyView");
        super.onDestroyView();
        if (this.mRootView != null && this.mRootView.getParent() != null) {
            ((ViewGroup) this.mRootView.getParent()).removeView(this.mRootView);
        }
        this.mIsViewPrepared = false;
    }

    @Override
    public void onDestroy() {
        LogUtil.i("onDestroy");
        super.onDestroy();
        destroyed = true;
        if (this.mPresenter != null) {
            this.mPresenter.onDestroy();
        }
    }

    /**
     * 懒加载数据
     */
    private void lazyLoadData() {
        LogUtil.i("lazyLoadData");
        if (super.getUserVisibleHint() && !this.mHasLoadData && this.mIsViewPrepared) {
            this.mHasLoadData = true;
            LogUtil.i("initData");
            this.initData();
        }
    }

    /**
     * 获取布局文件id
     *
     * @return
     */
    protected abstract int getContentViewId();

    /**
     * 创建presenter
     *
     * @return
     */
    protected abstract P createPresenter();

    /**
     * 初始化组件
     */
    protected void initView() {}

    /**
     * 初始化监听器
     */
    protected void initListener() {}



    /**
     * 初始化数据
     */
    protected void initData() {}

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {}

    protected void showKeyboard(boolean isShow) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        if (isShow) {
            if (activity.getCurrentFocus() == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            else {
                imm.showSoftInput(activity.getCurrentFocus(), 0);
            }
        }
        else {
            if (activity.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    protected void hideKeyboard(View view) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected void setTitle(int titleId) {
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            getActivity().setTitle(titleId);
        }
    }

    @Override
    public void showLoading() {}

    @Override
    public void hideLoading() {}

    @Override
    public void showMessage(String message) {}

    @Override
    public void onError(int code, String message) {
        LogUtil.e("code:" + code + " message:" + message);
    }
}
