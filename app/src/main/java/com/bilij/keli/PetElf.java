package com.bilij.keli;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bilij.keli.Util.ACache;
import com.bilij.keli.Util.ElfUtil;

import java.io.IOException;
import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;

public class PetElf extends View{

	final Point size = new Point();
	private int petW = 0;
	public static final String OPERATION = "operation";
	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;
	private static final int HANDLE_CHECK_ACTIVITY = 200;

	public static final float g = 9800f;
	public static final float m = 1000f;
	public static final float k = 5f;
	public static final float fs = 4000f;

	private boolean isAdded = false;

	public static final int SPEECH_START = 10000;
	public static final int RECOGNIZE_RESULT = 10001;
	public static final int RECOGNIZE_START = 10002;
	public static final int RECOGNIZE_DISMISS = 10003;
	public static final int TIMER_START = 10004;
	public static final int TIMER_STOP = 10005;
	public static final int RUN_LEFT = 10006;
	public static final int RUN_RIGHT = 10007;
	public static final int BACK_LEFT = 10008;
	public static final int BACK_RIGHT = 10009;
	public static final int SLEEP = 10010;
	public static final int DOSOME = 10011;

	private View talkview ,elfView,jinbiView;
	private int lastRadomWalkI = -1;
	private int screenW,screenY;
	ImageView elfImView ,jinbiImView;
	private boolean isPushing=false;
	private Context context;
	private WindowManager wm;
	private WindowManager.LayoutParams params,talkParams,jinbiParams,sampleParams;
	private SharedPreferences preferences;
	private  Editor edit;

	private String
			talkAnimationPath,
			pushAnimationPath,
			flyAnimationPath,
			successAnimationPath;

	private TextView text;
	private String[]
			stayAnimationPath,
			walkToLeftAnimationPath,
			walkToRightAnimationPath,
			backToLeftAnimationPath,
			backToRightAnimationPath;

	private String speak;
	private float elasticX ,elasticY;
	private GifDrawable hangUpDrawable ,
			walkLeftGifDrawable,
			backLeftGifDrawable,
			stayAnimation ,
			walkRightGifDrawable ,
			backRightGifDrawable ,
			flyDrawable,
			successDrawable;
	private SoundPool soundPool;
	private int qiangVoiceId,qiangVoiceId1,laVoiceId,daVoiceId,liuVoiceId,liuVoiceId2,hVoiceId1,hVoiceId2,sleepVoiceId1,sleepVoiceId2 ;
	private boolean hasHAPPEND = false;

	private int hangNum = 0;
	private int runNUM = 0;
	public PetElf(final Context context ,ImageView elfImView,View elfView,ImageView jinbiImView,View jinbiView,View talkview, TextView text){
		super(context);
		// TODO Auto-generated constructor stub
		this.context=context;

		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display defaultDisplay = wm.getDefaultDisplay();
		Point point = new Point();
		defaultDisplay.getSize(point);
		screenW = point.x;
		screenY = point.y;
		params = new WindowManager.LayoutParams();
		talkParams = new WindowManager.LayoutParams();
		jinbiParams = new WindowManager.LayoutParams();
		sampleParams = new WindowManager.LayoutParams();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0+
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
			talkParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
			jinbiParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
			sampleParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		}else {
			params.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			talkParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			jinbiParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			sampleParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}
		if (Build.VERSION.SDK_INT >= 21) {
			SoundPool.Builder builder = new SoundPool.Builder();
			//传入最多播放音频数量,
			builder.setMaxStreams(1);
			//AudioAttributes是一个封装音频各种属性的方法
			AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
			//设置音频流的合适的属性
			attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
			//加载一个AudioAttributes
			builder.setAudioAttributes(attrBuilder.build());
			soundPool = builder.build();
		} else {
			/**
			 * 第一个参数：int maxStreams：SoundPool对象的最大并发流数
			 * 第二个参数：int streamType：AudioManager中描述的音频流类型
			 *第三个参数：int srcQuality：采样率转换器的质量。 目前没有效果。 使用0作为默认值。
			 */
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		}
		qiangVoiceId = soundPool.load(context, R.raw.qiangqiang, 1);
		laVoiceId = soundPool.load(context, R.raw.lalala, 1);
		daVoiceId = soundPool.load(context, R.raw.dadada, 1);
		qiangVoiceId1 = soundPool.load(context, R.raw.qiangqiang1, 1);
		liuVoiceId = soundPool.load(context, R.raw.kailiu, 1);
		liuVoiceId2 = soundPool.load(context, R.raw.kailiu2, 1);
		hVoiceId1 = soundPool.load(context, R.raw.h_1, 1);
		hVoiceId2 = soundPool.load(context, R.raw.h_2, 1);
		sleepVoiceId1 = soundPool.load(context, R.raw.sleep1, 1);
		sleepVoiceId2 = soundPool.load(context, R.raw.sleep2, 1);
		wm.getDefaultDisplay().getSize(size);
		preferences = context.getSharedPreferences("pet", 0);
		edit = preferences.edit();
		this.elfView = elfView;
		this.elfImView = elfImView;
		this.jinbiImView = jinbiImView;
		this.jinbiView = jinbiView;
		this.text=text;
		this.talkview= talkview;

	}
	public void Go(){
		if(!hasHAPPEND){
			try {
				hangUpDrawable = new GifDrawable( context.getAssets(), getPushAnimationPath());
				flyDrawable = new GifDrawable( context.getAssets(), getFlyAnimationPath() );
				successDrawable= new GifDrawable( context.getAssets(), getSuccessAnimationPath() );
			} catch (IOException e) {
				e.printStackTrace();
			}
			touch();

			createJinbiView();
			createBodyView();

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Sleep();
					int radom = new Random().nextInt(2);
					if(isB_VOICE()){
						if(radom==0){
							soundPool.play(qiangVoiceId, 1, 1, 1, 0, 1);
						}else {
							soundPool.play(qiangVoiceId1, 1, 1, 1, 0, 1);
						}
					}

				}
			},500);


		}


	}
	private void touch(){
		elfView.setOnTouchListener(new OnTouchListener() {
			int lastX, lastY ,dx ,dy;
			int paramX, paramY;
			long downTime ,upTime;
			boolean isUpToLine = false;

			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						downTime=System.currentTimeMillis();
						lastX = (int) event.getRawX();
						lastY = (int) event.getRawY();
						paramX = params.x;
						paramY = params.y;
						hangNum++;
						HangUp();
						break;
					case MotionEvent.ACTION_MOVE:
						isPushing=true;
						dx = (int) event.getRawX() - lastX;
						dy = (int) event.getRawY() - lastY;
						params.x = paramX + dx;
						params.y = paramY + dy;
						fixXY();
						wm.updateViewLayout(elfView, params);
						break;
					case  MotionEvent.ACTION_UP:
						isPushing=false;
						upTime=System.currentTimeMillis();
						elasticX = params.x>0?(ElfUtil.getV(params.x)):(-ElfUtil.getV(params.x));
						elasticY = - ElfUtil.getV((params.y+params.height*1.3f)-sampleParams.y);
						jinbiView.setVisibility(GONE);
						if((params.x<(screenW*0.5-petW*0.1)&&params.x>(screenW*0.5-petW))&&(params.y<(screenY*0.5-petW*0.1)&&params.y>(screenY*0.5-petW))){
							if(isB_VOICE()){
								int radom = new Random().nextInt(2);
								if(radom==0){
									soundPool.play(liuVoiceId, 1, 1, 1, 0, 1);
								}else {
									soundPool.play(liuVoiceId2, 1, 1, 1, 0, 1);

								}
							}
							Dismiss();
						}else {
							Sleep();
						}
						break;
				}
				return true;
			}
		});
	}

	public void Happend(){
		mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
		mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
	}

	public void Dismiss(){
		mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
		mHandler.removeMessages(TIMER_START);
		mHandler.removeMessages(RUN_LEFT);
		mHandler.removeMessages(RUN_RIGHT);
		mHandler.removeMessages(BACK_LEFT);
		mHandler.removeMessages(BACK_RIGHT);
		mHandler.removeMessages(SLEEP);
		mHandler.removeMessages(DOSOME);
		elfView.setVisibility(GONE);
		talkview.setVisibility(GONE);
		jinbiView.setVisibility(GONE);
		wm.removeView(elfView);
		wm.removeView(jinbiView);
		try{
			wm.removeView(elfView);
		}catch (Exception e){

		}

		isAdded = false;
	}

	private void setImageHWbyGifDrawable(GifDrawable gifDrawable){
		elfImView.setImageDrawable(gifDrawable);
		float hangUpDrawableW = hangUpDrawable.getIntrinsicWidth();
		params.width = (int) (petW*(gifDrawable.getIntrinsicWidth()/hangUpDrawableW));
		params.height = params.width;
		elfView.setVisibility(VISIBLE);
		wm.updateViewLayout(elfView, params);
		RelativeLayout.LayoutParams linearParams =(RelativeLayout.LayoutParams) elfImView.getLayoutParams();
		linearParams.width = params.width;
		linearParams.height = params.height;
		elfImView.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
	}


	public void WalkToLeft(){
		int radom = new Random().nextInt(getWalkToLeftAnimationPath().length);
		if(isB_VOICE()){
			if(radom==0){
				soundPool.play(laVoiceId, 1, 1, 1, 0, 1);
			}else {
				soundPool.play(daVoiceId, 1, 1, 1, 0, 1);

			}
		}
		try {
			walkLeftGifDrawable = new GifDrawable( context.getAssets(), getWalkToLeftAnimationPath()[radom]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setImageHWbyGifDrawable(walkLeftGifDrawable);
		mHandler.sendMessage(getNowHangNum(PetElf.RUN_LEFT));


	}
	public void BackToLeft(){
		int radom = new Random().nextInt(getBackToLeftAnimationPath().length);
		if(isB_VOICE()){
			if(radom==0){
				soundPool.play(laVoiceId, 1, 1, 1, 0, 1);
			}else {
				soundPool.play(daVoiceId, 1, 1, 1, 0, 1);

			}
		}
		try {
			backLeftGifDrawable = new GifDrawable( context.getAssets(), getBackToLeftAnimationPath()[radom]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setImageHWbyGifDrawable(backLeftGifDrawable);
		mHandler.sendMessage(getNowHangNum(PetElf.BACK_LEFT));
	}
	public void WalkToRight(){
		int radom = new Random().nextInt(getWalkToRightAnimationPath().length);
		if(isB_VOICE()){
			if(radom==0){
				soundPool.play(laVoiceId, 1, 1, 1, 0, 1);
			}else {
				soundPool.play(daVoiceId, 1, 1, 1, 0, 1);

			}
		}
		try {
			walkRightGifDrawable = new GifDrawable( context.getAssets(), getWalkToRightAnimationPath()[radom]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setImageHWbyGifDrawable(walkRightGifDrawable);
		mHandler.sendMessage(getNowHangNum(PetElf.RUN_RIGHT));
	}
	public void BackToRight(){
		int radom = new Random().nextInt(getBackToRightAnimationPath().length);
		if(isB_VOICE()){
			if(radom==0){
				soundPool.play(laVoiceId, 1, 1, 1, 0, 1);
			}else {
				soundPool.play(daVoiceId, 1, 1, 1, 0, 1);

			}
		}
		try {
			backRightGifDrawable = new GifDrawable( context.getAssets(), getBackToRightAnimationPath()[radom]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setImageHWbyGifDrawable(backRightGifDrawable);
		mHandler.sendMessage(getNowHangNum(PetElf.BACK_RIGHT));
	}
	public void Sleep(){
		try {
			stayAnimation = new GifDrawable( context.getAssets(), getStayAnimationPath()[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setImageHWbyGifDrawable(stayAnimation);
		mHandler.sendMessage(getNowHangNum(PetElf.SLEEP));
		mHandler.sendMessageDelayed(getNowHangNum(PetElf.DOSOME) , 4000);
	}

	private Message getNowHangNum(int what) {
		int nowHangNum = hangNum;
		Message message = new Message();
		message.what = what;
		message.arg1 = nowHangNum;
		return message;
	}
	public void DoSome(){
		int radom = new Random().nextInt(getStayAnimationPath().length);
		try {
			stayAnimation = new GifDrawable( context.getAssets(), getStayAnimationPath()[radom]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(isB_VOICE()){
			if(radom==1){
				soundPool.play(sleepVoiceId1, 1, 1, 1, 0, 1);
			}else if(radom==2){
				soundPool.play(sleepVoiceId2, 1, 1, 1, 0, 1);

			}
		}
		setImageHWbyGifDrawable(stayAnimation);
		int duration =  stayAnimation.getDuration();
		mHandler.sendMessageDelayed(getNowHangNum(PetElf.TIMER_START) , duration);
	}

	public void HangUp(){
		if(!isPushing){
			setImageHWbyGifDrawable(hangUpDrawable);
		}
		jinbiView.setVisibility(VISIBLE);
		if(isB_VOICE()){
			int radom = new Random().nextInt(2);
			if(radom==0){
				soundPool.play(hVoiceId1, 1, 1, 1, 0, 1);
			}else {
				soundPool.play(hVoiceId2, 1, 1, 1, 0, 1);
			}
		}

		mHandler.sendEmptyMessage(PetElf.TIMER_STOP);
	}


	public void dismissTalk(){
		System.out.println("说完了");
		(talkview).setVisibility(View.GONE);
		Sleep();
	}


	public String getSpeak() {
		return speak;
	}

	public void setSpeak(String speak) {
		this.speak = speak;
	}

	@SuppressWarnings("static-access")
	@SuppressLint("NewApi") private void createBodyView() {
		params.format = PixelFormat.RGBA_8888; // 设置图片
		// 格式，效果为背景透明
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		petW = screenY/4;
		params.width = petW;
		params.height = petW;
		params.x = 0;
		params.y = 0;
		elfView.setVisibility(GONE);
		try {
			wm.addView(elfView, params);
		}catch (Exception e){


		}
		isAdded = true;
	}

	@SuppressWarnings("static-access")
	@SuppressLint("NewApi") private void createJinbiView() {
		jinbiParams.format = PixelFormat.RGBA_8888; // 设置图片
		// 格式，效果为背景透明
		jinbiParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		petW =screenY/4;
		jinbiParams.width = petW;
		jinbiParams.height = (int) (petW*0.71);
		jinbiParams.x = (int) (screenW*0.5-petW*0.35);
		jinbiParams.y = (int) (screenY*0.5-petW*0.5);
		jinbiView.setVisibility(GONE);
		try {
			wm.addView(jinbiView, jinbiParams);
		}catch (Exception e){

		}

		isAdded = true;
	}

	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case HANDLE_CHECK_ACTIVITY:
					if(ElfUtil.isHome(context)) {
						if(!isAdded) {
							wm.addView(elfView, params);
							wm.addView(talkview, talkParams);
							wm.addView(jinbiView, jinbiParams);
							isAdded = true;
						}
					} else {
						if(isAdded) {
							wm.removeView(elfView);
							wm.removeView(talkview);
							wm.removeView(jinbiView);
							isAdded = false;
						}
					}
					mHandler.sendMessageDelayed(getNowHangNum(PetElf.HANDLE_CHECK_ACTIVITY) , 1000);
					break;
				case PetElf.SPEECH_START:
					break;
				case PetElf.RECOGNIZE_RESULT:

					break;
				case PetElf.RECOGNIZE_START:

					break;
				case PetElf.RECOGNIZE_DISMISS:
					dismissTalk();
					break;
				case PetElf.TIMER_START:
					mHandler.removeMessages(PetElf.TIMER_START);
					if(msg.arg1<hangNum){
						break;
					}
					int i = (int)(Math.random()*(3));
//					int i =0;
					if(i==0){
						int j = checkLastRadomWalkI();
						switch (j){
							case 0:
								WalkToRight();
								break;
							case 1:
								WalkToLeft();
								break;
							case 2:
								BackToLeft();
								break;
							case 3:
								BackToRight();
								break;
						}
					}else {
						Sleep();
					}


					break;
				case PetElf.TIMER_STOP:
					mHandler.removeMessages(PetElf.TIMER_START);
					mHandler.removeMessages(PetElf.RUN_LEFT);
					mHandler.removeMessages(PetElf.RUN_RIGHT);
					mHandler.removeMessages(PetElf.BACK_LEFT);
					mHandler.removeMessages(PetElf.BACK_RIGHT);
					break;
				case PetElf.BACK_LEFT:
					mHandler.removeMessages(PetElf.RUN_LEFT);
					mHandler.removeMessages(PetElf.BACK_LEFT);
					mHandler.removeMessages(PetElf.RUN_RIGHT);
					mHandler.removeMessages(PetElf.BACK_RIGHT);
					if(msg.arg1<hangNum){
						break;
					}
					params.x = params.x - 2;
					params.y = params.y - 1;
					fixXY();
					wm.updateViewLayout(elfView, params);
					if(runNUM<100){
						mHandler.sendMessageDelayed(getNowHangNum(PetElf.BACK_LEFT) , 50);
						runNUM++;
					}else {
						mHandler.sendMessage(getNowHangNum(PetElf.TIMER_START) );
						runNUM = 0;
					}

					break;
				case PetElf.BACK_RIGHT:
					mHandler.removeMessages(PetElf.RUN_LEFT);
					mHandler.removeMessages(PetElf.BACK_LEFT);
					mHandler.removeMessages(PetElf.RUN_RIGHT);
					mHandler.removeMessages(PetElf.BACK_RIGHT);
					if(msg.arg1<hangNum){
						break;
					}
					params.x = params.x + 2;
					params.y = params.y - 1;
					fixXY();
					wm.updateViewLayout(elfView, params);
					if(runNUM<100){
						mHandler.sendMessageDelayed(getNowHangNum(PetElf.BACK_RIGHT) , 50);
						runNUM++;
					}else {
						mHandler.sendMessage(getNowHangNum(PetElf.TIMER_START) );
						runNUM = 0;
					}
					break;
				case PetElf.RUN_LEFT:
					mHandler.removeMessages(PetElf.RUN_LEFT);
					mHandler.removeMessages(PetElf.BACK_LEFT);
					mHandler.removeMessages(PetElf.RUN_RIGHT);
					mHandler.removeMessages(PetElf.BACK_RIGHT);
					if(msg.arg1<hangNum){
						break;
					}
					params.x = params.x - 2;
					params.y = params.y + 1;
					fixXY();
					wm.updateViewLayout(elfView, params);
					if(runNUM<100){
						mHandler.sendMessageDelayed(getNowHangNum(PetElf.RUN_LEFT) , 50);
						runNUM++;
					}else {
						mHandler.sendMessage(getNowHangNum(PetElf.TIMER_START) );
						runNUM = 0;
					}
					break;
				case PetElf.RUN_RIGHT:
					mHandler.removeMessages(PetElf.RUN_LEFT);
					mHandler.removeMessages(PetElf.BACK_LEFT);
					mHandler.removeMessages(PetElf.RUN_RIGHT);
					mHandler.removeMessages(PetElf.BACK_RIGHT);
					if(msg.arg1<hangNum){
						break;
					}
					params.x = params.x + 2;
					params.y = params.y + 1;
					fixXY();
					wm.updateViewLayout(elfView, params);
					if(runNUM<100){
						mHandler.sendMessageDelayed(getNowHangNum(PetElf.RUN_RIGHT) , 50);
						runNUM++;
					}else {
						mHandler.sendMessage(getNowHangNum(PetElf.TIMER_START) );
						runNUM = 0;
					}
					break;
				case PetElf.SLEEP:
					mHandler.removeMessages(PetElf.TIMER_START);
					mHandler.removeMessages(PetElf.RUN_LEFT);
					mHandler.removeMessages(PetElf.RUN_RIGHT);
					mHandler.removeMessages(PetElf.BACK_LEFT);
					mHandler.removeMessages(PetElf.BACK_RIGHT);
					break;
				case PetElf.DOSOME:
					if(msg.arg1<hangNum){
						break;
					}
					DoSome();
					break;
			}
		}
	};


	private void fixXY(){
		Log.e("","params.x = "+params.x +", params.y = "+params.y +", screenW = "+screenW +", screenY = "+screenY);
		if(params.x<(-(screenW/2-petW/2))){
			params.x = -(screenW/2-petW/2);
		}
		if(params.x>(screenW/2-petW/2)){
			params.x = screenW/2-petW/2;
		}
		if(params.y<(-(screenY/2-petW/2))){
			params.y = -(screenY/2-petW/2);
		}
		if(params.y>(screenY/2-petW/2)){
			params.y = screenY/2-petW/2;
		}
	}
	private int checkLastRadomWalkI() {
		int j = (int)(Math.random()*(4));
		if(j == lastRadomWalkI){
			j = checkLastRadomWalkI();
		}
		return j;
	}

	public static String getOPERATION() {
		return OPERATION;
	}



	public String getTalkAnimationPath() {
		return talkAnimationPath;
	}

	public void setTalkAnimationPath(String talkAnimationPath) {
		this.talkAnimationPath = talkAnimationPath;
	}

	public String getPushAnimationPath() {
		return pushAnimationPath;
	}

	public void setPushAnimationPath(String pushAnimationPath) {
		this.pushAnimationPath = pushAnimationPath;
	}

	public String getFlyAnimationPath() {
		return flyAnimationPath;
	}

	public void setFlyAnimationPath(String flyAnimationPath) {
		this.flyAnimationPath = flyAnimationPath;
	}

	public String[] getStayAnimationPath() {
		return stayAnimationPath;
	}

	public void setStayAnimationPath(String[] stayAnimationPath) {
		this.stayAnimationPath = stayAnimationPath;
	}
	public String getSuccessAnimationPath() {
		return successAnimationPath;
	}

	public void setSuccessAnimationPath(String successAnimationPath) {
		this.successAnimationPath = successAnimationPath;
	}


	public String[] getWalkToLeftAnimationPath() {
		return walkToLeftAnimationPath;
	}

	public void setWalkToLeftAnimationPath(String[] walkToLeftAnimationPath) {
		this.walkToLeftAnimationPath = walkToLeftAnimationPath;
	}

	public String[] getWalkToRightAnimationPath() {
		return walkToRightAnimationPath;
	}

	public void setWalkToRightAnimationPath(String[] walkToRightAnimationPath) {
		this.walkToRightAnimationPath = walkToRightAnimationPath;
	}

	public String[] getBackToLeftAnimationPath() {
		return backToLeftAnimationPath;
	}

	public void setBackToLeftAnimationPath(String[] backToLeftAnimationPath) {
		this.backToLeftAnimationPath = backToLeftAnimationPath;
	}

	public String[] getBackToRightAnimationPath() {
		return backToRightAnimationPath;
	}

	public void setBackToRightAnimationPath(String[] backToRightAnimationPath) {
		this.backToRightAnimationPath = backToRightAnimationPath;
	}

	public boolean isB_VOICE() {
		boolean b = ACache.get(context).getAsString("sound").equals("yes");
		return b;
	}

	public void setB_VOICE(String b) {
		ACache.get(context).put("sound",b);
	}
}
