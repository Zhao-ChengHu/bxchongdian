package com.bxchongdian.app.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bxchongdian.app.views.activities.ChargingfeeActivity;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.bxchongdian.app.LvAppUtils;
import com.bxchongdian.app.R;
import com.bxchongdian.app.event.ShowChargingInfoEvent;
import com.bxchongdian.app.event.ShowNavListEvent;
import com.bxchongdian.app.event.StartBrotherEvent;
import com.bxchongdian.app.utils.FeeUtils;
import com.bxchongdian.app.views.activities.FeeActivity;
import com.bxchongdian.app.views.activities.LoginActivity;
import com.bxchongdian.app.views.activities.PileListActivity;
import com.bxchongdian.app.views.base.LvBaseMainFragment;
import com.bxchongdian.app.wigets.MapNavListView;
import com.bxchongdian.app.wigets.OrderView;
import com.bxchongdian.app.wigets.SimpleTabEntity;
import com.bxchongdian.model.bean.FeeBean;
import com.bxchongdian.model.bean.SubstationBean;
import com.bxchongdian.presenter.fee.FeeContract;
import com.bxchongdian.presenter.fee.FeePresenter;
import com.bxchongdian.presenter.main.MainContract;
import com.bxchongdian.presenter.main.MainPresenter;
import com.trello.rxlifecycle.FragmentEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.leanvision.baseframe.rx.RxBus;
import cn.com.leanvision.baseframe.rx.transformers.SchedulersCompat;
import cn.com.leanvision.baseframe.util.LvTextUtil;
import cn.com.leanvision.fragmentation.SupportFragment;
import rx.functions.Action1;

/********************************
 * Created by lvshicheng on 2017/2/13.
 * modify by 赵成虎 on 2018/5/4
 * <p>
 * 首页
 ********************************/
public class MainFragment extends LvBaseMainFragment implements MainContract.View, FeeContract.View {

    public static final int FIRST  = 0;
    public static final int SECOND = 1;
    public static final int THIRD  = 2;

    @BindView(R.id.bottomNavigation)
    CommonTabLayout mBottomNavigation;
    @BindView(R.id.view_order)
    OrderView       orderView;

    private MapNavListView mapNavListView;

    private int prePos = -1;

    private SupportFragment[] mFragments = new SupportFragment[3];

    private MainPresenter  mainPresenter;
    private FeePresenter   feePresenter;
    private SubstationBean substationBean;
//    private PopupWindow       popWindow;
    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frgm_main, container, false);
    }

    @Override
    protected void initPresenter() {
        mainPresenter = new MainPresenter();
        feePresenter = new FeePresenter();
        mainPresenter.attachView(this);
       feePresenter.attachView(this);
    }

    @Override
    protected void destroyPresenter() {
        mainPresenter.detachView();
        feePresenter.detachView();
    }

    @Override
    protected void initView(@Nullable Bundle savedInstanceState) {
//        initPopWindow();
        if (savedInstanceState == null) {
            mFragments[FIRST] = OrderFragment.newInstance();
            mFragments[SECOND] = ChargingFragment.newInstance();
            mFragments[THIRD] = MimeFragment.newInstance();

            loadMultipleRootFragment(R.id.sub_fragment_container, FIRST,
                mFragments[FIRST],
                mFragments[SECOND],
                mFragments[THIRD]);
            prePos = FIRST;
        } else {
            mFragments[FIRST] = findChildFragment(OrderFragment.class);
            mFragments[SECOND] = findChildFragment(ChargingFragment.class);
            mFragments[THIRD] = findChildFragment(MimeFragment.class);

            prePos = savedInstanceState.getInt("PrePos", FIRST);
        }
        initBottomNavigation();
    }
    /**
     * 初始化底部弹窗
     */
//    private void initPopWindow() {
//        View popView = LayoutInflater.from(this.getActivity()).inflate(R.layout.view_pop_fee, null);
//        TextView tvOrder = (TextView) popView.findViewById(R.id.tv_one);
//        tvOrder.setText("查看充电费");
//        TextView tvScan = (TextView) popView.findViewById(R.id.tv_two);
//        tvScan.setText("查看服务费");
//        TextView tvCancel = (TextView) popView.findViewById(R.id.tv_cancel);
//        tvCancel.setOnClickListener(popListener);
//        tvOrder.setOnClickListener(popListener);
//        tvScan.setOnClickListener(popListener);
//        popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        popWindow.setFocusable(true);
//        popWindow.setTouchable(true);
//        popWindow.setOutsideTouchable(false);
//        popWindow.setAnimationStyle(R.style.AnimBottom);
//        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
////                setBackgroundAlpha(1);
//            }
//        });
//    }

    /**
     * 弹窗点击监听
     */
//    View.OnClickListener popListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.tv_one:     //跳转查看充电费
//                    ChargingfeeActivity.navigation(substationBean.areaId, substationBean.areaName);
//                    break;
//                case R.id.tv_two://跳转查看查看服务费
//                    FeeActivity.navigation(substationBean.areaId, substationBean.areaName);
//                    break;
//                case R.id.tv_cancel:
//                    break;
//            }
//            popWindow.dismiss();
//        }
//    };

    /**
     * 初始化底部导航
     */
    private void initBottomNavigation() {
        ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
        tabEntities.add(new SimpleTabEntity(getString(R.string.bt_nav_order), R.drawable.bt_nav_1_selected, R.drawable.bt_nav_1));
        tabEntities.add(new SimpleTabEntity(getString(R.string.bt_nav_charging), R.drawable.bt_nav_2_selected, R.drawable.bt_nav_2));
        tabEntities.add(new SimpleTabEntity(getString(R.string.bt_nav_mine), R.drawable.bt_nav_3_selected, R.drawable.bt_nav_3));
        mBottomNavigation.setTabData(tabEntities);

        mBottomNavigation.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                showHideFragment(mFragments[position], mFragments[prePos]);
                prePos = position;
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
//        start(SplashFragment.newInstance()); // 引导页面
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //发生异常时保存prePos
        outState.putInt("PrePos", prePos);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 监听页面跳转事件 - 主要负责fragment切换
        RxBus.getInstance()
            .toObservable(StartBrotherEvent.class)
            .compose(this.<StartBrotherEvent>bindUntilEvent(FragmentEvent.PAUSE))
            .compose(SchedulersCompat.<StartBrotherEvent>observeOnMainThread())
            .subscribe(new Action1<StartBrotherEvent>() {
                @Override
                public void call(StartBrotherEvent startBrotherEvent) {
                    start(startBrotherEvent.targetFragment);
                }
            });

        // 注册ShowChargingInfoEvent在OrderMapFragment的initMapListener() 中发送事件
        // 用于显示底部充电站信息弹窗
        RxBus.getInstance()
            .toObservable(ShowChargingInfoEvent.class)
            .compose(this.<ShowChargingInfoEvent>bindUntilEvent(FragmentEvent.PAUSE))
            .compose(SchedulersCompat.<ShowChargingInfoEvent>observeOnMainThread())
            .subscribe(new Action1<ShowChargingInfoEvent>() {
                @Override
                public void call(ShowChargingInfoEvent showChargingInfoEvent) {
                    if (showChargingInfoEvent.isShow) {
                        substationBean = showChargingInfoEvent.substationBean;
                        feePresenter.queryFees(substationBean.areaId);
                        orderView.refreshData(substationBean);
                        orderView.show();
//              mainPresenter.getSubstationSummary(substationBean.s_id);
                    } else {
                        orderView.hide();
                    }
                }
            });
        //注册ShowNavListEvent事件在OrderListFragment的onBindViewHolder()中发送事件
        //用于显示导航选择列表
        RxBus.getInstance()
            .toObservable(ShowNavListEvent.class)
            .compose(this.<ShowNavListEvent>bindUntilEvent(FragmentEvent.PAUSE))
            .compose(SchedulersCompat.<ShowNavListEvent>observeOnMainThread())
            .subscribe(new Action1<ShowNavListEvent>() {
                @Override
                public void call(ShowNavListEvent event) {
                    showMapNavListView(event.lat, event.lng);
                }
            });
    }

    @Override
    protected boolean beforeBack() {
        if (mapNavListView != null && mapNavListView.getParent() != null && mapNavListView.isShown()) {
            mapNavListView.hide();
            return true;
        }
        return orderView.hide();
    }

    public int getOrderViewHeight() {
        return orderView.getBottomDialogHeight();
    }

    /**
     * OrderView中的导航按钮点击事件
     */
    @OnClick(R.id.tv_distance)
    public void clickDistance() { // 显示导航选择
        showMapNavListView(substationBean.getLat(), substationBean.getLng());
    }

    /**
     * 显示导航View
     * @param lat 纬度
     * @param lng 经度
     */
    private void showMapNavListView(double lat, double lng) {
        if (mapNavListView == null) {
            mapNavListView = new MapNavListView(_mActivity);
        }
        mapNavListView.setLatlng(lat, lng);
        mapNavListView.show(_mActivity);
    }

    /**
     * OrderView中的收藏按钮点击事件
     */
    @OnClick(R.id.ivb_favor)
    public void clickFavor() { // 收藏
        if (!LvAppUtils.isLogin()) {
            LoginActivity.navigation(false);
        } else {
            if (orderView.isFavor()) { // 已收藏
                showLoadingDialog();
                mainPresenter.clearFavor(substationBean.areaId);
            } else { //收藏电站
                showLoadingDialog();
                mainPresenter.saveFavor(substationBean.areaId);
            }
        }
    }

    /**
     * OrderView中的详情按钮点击事件
     */
    @OnClick(R.id.btn_detail)
    public void clickDetail() {
//        StationDetailActivity.navigation(substationBean.substationId, substationBean.name,
//            substationBean.getLat(), substationBean.getLng());
        PileListActivity.navigation(substationBean.areaId, substationBean.areaName,
                substationBean.getLat(), substationBean.getLng());
    }

    /**
     * OrderView中的费用点击事件
     */
    @OnClick(R.id.view_divider_4)
    public void chargingFee() {
        //跳转到FeeActivity
        ChargingfeeActivity.navigation(substationBean.areaId, substationBean.areaName);
    }

    @OnClick(R.id.view_divider_5)
    public void serviceFee() {
        //跳转到FeeActivity
        FeeActivity.navigation(substationBean.areaId, substationBean.areaName);
    }

//    @OnClick(R.id.tv_service)
//    public void selectMode() {tv_service
//        if (!popWindow.isShowing()) {
//            popWindow.showAtLocation(popWindow.getContentView(), Gravity.BOTTOM, 0, 0);
//        }
//    }

    /**
     * --------------------
     * MainContract.View impl
     * --------------------
     */
    @Override
    public void saveFavorSuccess() {
        orderView.setFavor(true);
        // 更新List页面的收藏状态
        dismissLoadingDialog();
        showToast(R.string.favor_success);
    }

    @Override
    public void clearFavorSuccess() {
        orderView.setFavor(false);
        dismissLoadingDialog();
        showToast(R.string.unfavor_success);
    }

    @Override
    public void showLoading(String msg) {
        showLoadingDialog();
    }

    @Override
    public void showNormal() {
        dismissLoadingDialog();
    }

    @Override
    public void requestFailed(String msg) {
        dismissLoadingDialog();
        if (LvTextUtil.isEmpty(msg)) {
            showToast(R.string.network_not_available);
        } else {
            showToast(msg);
        }
    }

    @Override
    public void getSummarySuccess() { // 获取站点详细信息成功,刷新弹窗内容
        // hasRestAC
        // hasTotalAC
        // hasRestDC
        // hasTotalDC
    }

    @Override
    public void getFees(List<FeeBean> list) {  //刷新费用信息
        float[] fee = FeeUtils.getCurrentFree(list);
        if (!orderView.isHiding()) {
            orderView.refreshFee(fee[0], fee[1], 0f);
        }
    }
}
