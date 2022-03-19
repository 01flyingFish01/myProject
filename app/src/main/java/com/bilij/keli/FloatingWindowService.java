package com.bilij.keli;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bilij.keli.Util.ElfUtil;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class FloatingWindowService extends Service {
	private PetElf petElf;
	private View myElfView,talkview,jinbiView ;
	private List<String> homeList; // 桌面应用程序包名列表
	private Timer timer;
	private boolean isFrist = true;

	@Override
	public IBinder onBind(Intent intent) {

		return new ElfBinder();
	}
	//[1]定义中间人对象(IBinder)

	public class ElfBinder extends Binder {
		public void callElf(int i){
			//调用办证的方法
			if(i== PetElf.OPERATION_SHOW){
				petElf.Go();
			}else {
				petElf.Dismiss();
			}
		}
		public void setSound(boolean b){
			//调用办证的方法
			if(b){
				petElf.setB_VOICE("yes");
			}else {
				petElf.setB_VOICE("no");
			}
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		homeList = ElfUtil.getHomes(this);
		initPet();
	}

	private void initPet(){
		myElfView =LayoutInflater.from(this).inflate( R.layout.petelf, null);
		jinbiView =LayoutInflater.from(this).inflate( R.layout.jinbi, null);
		talkview = LayoutInflater.from(this).inflate( R.layout.talkwindow, null);
		TextView talkrighttop_tx = (TextView) talkview.findViewById(R.id.talkrighttop_tx);
		ImageView elfbody = (ImageView) myElfView.findViewById(R.id.elfbody);
		ImageView jinbiBody = (ImageView) jinbiView.findViewById(R.id.jinbi);
		petElf = new PetElf(getApplicationContext(),elfbody,myElfView,jinbiBody,jinbiView,talkview,talkrighttop_tx);
		petElf.setPushAnimationPath("drag2.gif");
		petElf.setStayAnimationPath(new String[]{"ss.gif","s1.gif","s2.gif"});
		petElf.setTalkAnimationPath("a80_3.gif");
		petElf.setWalkToLeftAnimationPath(new String[]{"runL1.gif","runL2.gif"});
		petElf.setWalkToRightAnimationPath(new String[]{"runR1.gif","runR2.gif"});
		petElf.setBackToLeftAnimationPath(new String[]{"backL1.gif","backL2.gif"});
		petElf.setBackToRightAnimationPath(new String[]{"backR1.gif","backR2.gif"});
		petElf.setFlyAnimationPath("drag1.gif");
		petElf.setSuccessAnimationPath("success.gif");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		petElf.Dismiss();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("TopAppService", "服务执行了");
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					//判断是否有use 查看使用情况的权限
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
						boolean useGranted = isUseGranted();
						Log.e("TopAppService", "use 权限 是否允许授权=" + useGranted);
						if (useGranted) {
							String topApp = getHigherPackageName();
							Log.e("TopAppService", "顶层app=" + topApp);
						} else {
							//开启应用授权界面
							if(isFrist){
								Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
								isFrist = false;
							}
						}
					} else {
						String topApp = getLowerVersionPackageName();
						Log.e("TopAppService", "顶层app=" + topApp);
					}
				}
			}, 0, 5000);//每隔5s 执行一次
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 判断  用户查看使用情况的权利是否给予app
	 *
	 * @return
	 */
	private boolean isUseGranted() {
		Context appContext = getApplication().getApplicationContext();
		AppOpsManager appOps = (AppOpsManager) appContext
				.getSystemService(Context.APP_OPS_SERVICE);
		int mode = -1;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			mode = appOps.checkOpNoThrow("android:get_usage_stats",
					android.os.Process.myUid(), appContext.getPackageName());
		}
		boolean granted = mode == AppOpsManager.MODE_ALLOWED;
		return granted;
	}

	/**
	 * 高版本：获取顶层的activity的包名
	 *
	 * @return
	 */
	private String getHigherPackageName() {
		String topPackageName = "";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
			long time = System.currentTimeMillis();
			//time - 1000 * 1000, time 开始时间和结束时间的设置，在这个时间范围内 获取栈顶Activity 有效
			List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
			// Sort the stats by the last time used
			if (stats != null) {
				SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
				for (UsageStats usageStats : stats) {
					mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
				}
				if (mySortedMap != null && !mySortedMap.isEmpty()) {
					topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
					Log.e("TopPackage Name", topPackageName);
				}
			}
		} else {
			ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName topActivity = activityManager.getRunningTasks(1).get(0).topActivity;
			topPackageName = topActivity.getPackageName();
		}
		return topPackageName;
	}

	/**
	 * 低版本：获取栈顶app的包名
	 *
	 * @return
	 */
	private String getLowerVersionPackageName() {
		String topPackageName;//低版本  直接获取getRunningTasks
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName topActivity = activityManager.getRunningTasks(1).get(0).topActivity;
		topPackageName = topActivity.getPackageName();
		return topPackageName;
	}






}





























