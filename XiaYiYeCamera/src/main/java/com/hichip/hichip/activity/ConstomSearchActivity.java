package com.hichip.hichip.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.hichip.R;
import com.hichip.hichip.widget.DatePickerView;
import com.hichip.thecamhi.activity.VideoOnlineActivity;
import com.hichip.thecamhi.base.HiToast;
import com.hichip.thecamhi.base.TitleView;
import com.hichip.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hichip.thecamhi.bean.HiDataValue;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
/**
 * 自定义搜索SD卡录像的界面
 * @author lt
 */
public class ConstomSearchActivity extends Activity implements View.OnClickListener {

	public enum SCROLL_TYPE {
		HOUR(1), MINUTE(2);

		SCROLL_TYPE(int value) {
			this.value = value;
		}

		public int value;
	}

	private int scrollUnits = SCROLL_TYPE.HOUR.value + SCROLL_TYPE.MINUTE.value;
	private DatePickerView year_pv, month_pv, day_pv, hour_pv, minute_pv;

	private DatePickerView year_pv_to, month_pv_to, day_pv_to, hour_pv_to, minute_pv_to;

	private static final int MAX_MINUTE = 59;
	private static final int MAX_HOUR = 23;
	private static final int MIN_MINUTE = 0;
	private static final int MIN_HOUR = 0;
	private static final int MAX_MONTH = 12;

	private ArrayList<String> year, month, day, hour, minute;
	private int startYear, startMonth, startDay, startHour, startMinute, endYear, endMonth, endDay, endHour, endMinute;
	private boolean spanYear, spanMon, spanDay, spanHour, spanMin;
	private Calendar startCalendar, endCalendar;
	private Calendar selectedCalenderFrom, selectedCalenderTo;
	private TextView tv_cancle, tv_select, hour_text, minute_text;

	private SimpleDateFormat sdf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_constom);

		initTopView();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String now = sdf.format(new Date());
		

		year_pv = (DatePickerView) findViewById(R.id.year_pv);
		year_pv_to = (DatePickerView) findViewById(R.id.year_pv_to);
		month_pv = (DatePickerView) findViewById(R.id.month_pv);
		month_pv_to = (DatePickerView) findViewById(R.id.month_pv_to);
		day_pv = (DatePickerView) findViewById(R.id.day_pv);
		day_pv_to = (DatePickerView) findViewById(R.id.day_pv_to);
		hour_pv = (DatePickerView) findViewById(R.id.hour_pv);
		hour_pv_to = (DatePickerView) findViewById(R.id.hour_pv_to);
		minute_pv = (DatePickerView) findViewById(R.id.minute_pv);
		minute_pv_to = (DatePickerView) findViewById(R.id.minute_pv_to);
		// tv_cancle = (TextView) findViewById(R.id.tv_cancle);
		// tv_select = (TextView) findViewById(R.id.tv_select);
		hour_text = (TextView) findViewById(R.id.hour_text);
		minute_text = (TextView) findViewById(R.id.minute_text);

		boolean isLoop = true;
		this.year_pv.setIsLoop(isLoop);
		this.year_pv_to.setIsLoop(isLoop);
		this.month_pv.setIsLoop(isLoop);
		this.month_pv_to.setIsLoop(isLoop);
		this.minute_pv_to.setIsLoop(isLoop);
		this.day_pv.setIsLoop(isLoop);
		this.day_pv_to.setIsLoop(isLoop);
		this.hour_pv.setIsLoop(isLoop);
		this.hour_pv_to.setIsLoop(isLoop);
		this.minute_pv.setIsLoop(isLoop);

		String startDate = "2010-01-01 00:00";
		String endDate = "2030-01-01 00:00";

		selectedCalenderFrom = Calendar.getInstance();
		selectedCalenderTo = Calendar.getInstance();
		startCalendar = Calendar.getInstance();
		endCalendar = Calendar.getInstance();
		SimpleDateFormat sdff = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			startCalendar.setTime(sdff.parse(startDate));
			endCalendar.setTime(sdff.parse(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		show(now);

		// tv_cancle.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// }
		// });

		// tv_select.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
		//
		//
		// //LogUtils.e("", sdf.format(selectedCalender.getTime())); // huoqu
		// }
		// });
	}

	private void initTopView() {
		TitleView title = (TitleView) findViewById(R.id.title_top);
		title.setTitle(getString(R.string.search_video));
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setButton(TitleView.NAVIGATION_BUTTON_RIGHT);
		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					ConstomSearchActivity.this.finish();
					break;
				case TitleView.NAVIGATION_BUTTON_RIGHT:
					if(selectedCalenderFrom.getTimeInMillis()-selectedCalenderTo.getTimeInMillis()>=0){
						HiToast.showToast(ConstomSearchActivity.this,getString(R.string.tips_stanotend));
						return;
					}
					
					Intent intent = new Intent("searchend");
					Bundle bundle = new Bundle();
					bundle.putLong(VideoOnlineActivity.SEARCH_ACTIVITY_START_TIME,
							selectedCalenderFrom.getTimeInMillis());
					bundle.putLong(VideoOnlineActivity.SEARCH_ACTIVITY_END_TIME,
							selectedCalenderTo.getTimeInMillis());
					intent.putExtra(HiDataValue.EXTRAS_KEY_DATA, bundle);
					setResult(RESULT_OK, intent);
					finish();
					break;

				}
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.selectDate:
		// // ���ڸ�ʽΪyyyy-MM-dd
		// customDatePicker1.show(currentDate.getText().toString());
		// break;
		//
		// case R.id.selectTime:
		// // ���ڸ�ʽΪyyyy-MM-dd HH:mm
		// customDatePicker2.show(currentTime.getText().toString());
		// break;
		}
	}

	private void initParameter(Calendar selectedCalender) {
		startYear = startCalendar.get(Calendar.YEAR);
		startMonth = startCalendar.get(Calendar.MONTH) + 1;
		startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
		startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
		startMinute = startCalendar.get(Calendar.MINUTE);
		endYear = endCalendar.get(Calendar.YEAR);
		endMonth = endCalendar.get(Calendar.MONTH) + 1;
		endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
		endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
		endMinute = endCalendar.get(Calendar.MINUTE);
		spanYear = startYear != endYear;
		spanMon = (!spanYear) && (startMonth != endMonth);
		spanDay = (!spanMon) && (startDay != endDay);
		spanHour = (!spanDay) && (startHour != endHour);
		spanMin = (!spanHour) && (startMinute != endMinute);
		selectedCalender.setTime(startCalendar.getTime());
	}

	private void initTimer() {
		initArrayList();
		if (spanYear) {
			for (int i = startYear; i <= endYear; i++) {
				year.add(String.valueOf(i));
			}
			for (int i = startMonth; i <= MAX_MONTH; i++) {
				month.add(formatTimeUnit(i));
			}
			for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
				day.add(formatTimeUnit(i));
			}

			if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
				hour.add(formatTimeUnit(startHour));
			} else {
				for (int i = startHour; i <= MAX_HOUR; i++) {
					hour.add(formatTimeUnit(i));
				}
			}

			if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
				minute.add(formatTimeUnit(startMinute));
			} else {
				for (int i = startMinute; i <= MAX_MINUTE; i++) {
					minute.add(formatTimeUnit(i));
				}
			}
		} else if (spanMon) {
			year.add(String.valueOf(startYear));
			for (int i = startMonth; i <= endMonth; i++) {
				month.add(formatTimeUnit(i));
			}
			for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
				day.add(formatTimeUnit(i));
			}

			if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
				hour.add(formatTimeUnit(startHour));
			} else {
				for (int i = startHour; i <= MAX_HOUR; i++) {
					hour.add(formatTimeUnit(i));
				}
			}

			if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
				minute.add(formatTimeUnit(startMinute));
			} else {
				for (int i = startMinute; i <= MAX_MINUTE; i++) {
					minute.add(formatTimeUnit(i));
				}
			}
		} else if (spanDay) {
			year.add(String.valueOf(startYear));
			month.add(formatTimeUnit(startMonth));
			for (int i = startDay; i <= endDay; i++) {
				day.add(formatTimeUnit(i));
			}

			if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
				hour.add(formatTimeUnit(startHour));
			} else {
				for (int i = startHour; i <= MAX_HOUR; i++) {
					hour.add(formatTimeUnit(i));
				}
			}

			if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
				minute.add(formatTimeUnit(startMinute));
			} else {
				for (int i = startMinute; i <= MAX_MINUTE; i++) {
					minute.add(formatTimeUnit(i));
				}
			}
		} else if (spanHour) {
			year.add(String.valueOf(startYear));
			month.add(formatTimeUnit(startMonth));
			day.add(formatTimeUnit(startDay));

			if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
				hour.add(formatTimeUnit(startHour));
			} else {
				for (int i = startHour; i <= endHour; i++) {
					hour.add(formatTimeUnit(i));
				}
			}

			if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
				minute.add(formatTimeUnit(startMinute));
			} else {
				for (int i = startMinute; i <= MAX_MINUTE; i++) {
					minute.add(formatTimeUnit(i));
				}
			}
		} else if (spanMin) {
			year.add(String.valueOf(startYear));
			month.add(formatTimeUnit(startMonth));
			day.add(formatTimeUnit(startDay));
			hour.add(formatTimeUnit(startHour));

			if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
				minute.add(formatTimeUnit(startMinute));
			} else {
				for (int i = startMinute; i <= endMinute; i++) {
					minute.add(formatTimeUnit(i));
				}
			}
		}
		loadComponent();
	}

	/**
	 * 将“0-9”转换为“00-09”
	 */
	private String formatTimeUnit(int unit) {
		return unit < 10 ? "0" + String.valueOf(unit) : String.valueOf(unit);
	}

	private void initArrayList() {
		if (year == null)
			year = new ArrayList<String>();
		if (month == null)
			month = new ArrayList<String>();
		if (day == null)
			day = new ArrayList<String>();
		if (hour == null)
			hour = new ArrayList<String>();
		if (minute == null)
			minute = new ArrayList<String>();
		year.clear();
		month.clear();
		day.clear();
		hour.clear();
		minute.clear();
	}

	private void loadComponent() {
		year_pv.setData(year);
		year_pv_to.setData(year);
		month_pv.setData(month);
		month_pv_to.setData(month);
		day_pv.setData(day);
		day_pv_to.setData(day);
		hour_pv.setData(hour);
		hour_pv_to.setData(hour);
		minute_pv.setData(minute);
		minute_pv_to.setData(minute);
		year_pv.setSelected(0);
		year_pv_to.setSelected(0);
		month_pv.setSelected(0);
		month_pv_to.setSelected(0);
		day_pv.setSelected(0);
		day_pv_to.setSelected(0);
		hour_pv.setSelected(0);
		hour_pv_to.setSelected(0);
		minute_pv.setSelected(0);
		minute_pv_to.setSelected(0);
		executeScroll();
	}

	private void addListenerTo(final Calendar selectedCalender) {
		year_pv_to.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.YEAR, Integer.parseInt(text));
				// monthChange();
				executeAnimator(month_pv_to);
				executeAnimator(day_pv_to);
				executeAnimator(hour_pv_to);
				executeAnimator(minute_pv_to);
			}
		});
		month_pv_to.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.DAY_OF_MONTH, 1);
				selectedCalender.set(Calendar.MONTH, Integer.parseInt(text) - 1);
				dayChange(selectedCalender,1);
				executeAnimator(day_pv_to);
				executeAnimator(hour_pv_to);
				executeAnimator(minute_pv_to);
			}
		});

		day_pv_to.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
				// hourChange();
				executeAnimator(hour_pv_to);
				executeAnimator(minute_pv_to);
			}
		});
		hour_pv_to.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
				// minuteChange();
				executeAnimator(minute_pv_to);
			}
		});
		minute_pv_to.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.MINUTE, Integer.parseInt(text));
			}
		});
	}

	private void addListenerFrom(final Calendar selectedCalender) {
		year_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.YEAR, Integer.parseInt(text));
				// monthChange();
				executeAnimator(month_pv);
				executeAnimator(day_pv);
				executeAnimator(hour_pv);
				executeAnimator(minute_pv);
			}
		});

		month_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.DAY_OF_MONTH, 1);
				selectedCalender.set(Calendar.MONTH, Integer.parseInt(text) - 1);
				dayChange(selectedCalender,0);
				executeAnimator(day_pv);
				executeAnimator(hour_pv);
				executeAnimator(minute_pv);

			}
		});

		day_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
				// hourChange();
				executeAnimator(hour_pv);
				executeAnimator(minute_pv);
			}
		});

		hour_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
				// minuteChange();
				executeAnimator(minute_pv);
			}
		});

		minute_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
			@Override
			public void onSelect(String text) {
				selectedCalender.set(Calendar.MINUTE, Integer.parseInt(text));
			}
		});

	}
	
	private void dayChange(Calendar calendar,int type) {
		day.clear();
		for (int i = 1; i <= calendar.getActualMaximum(Calendar.DATE); i++) {
			day.add(formatTimeUnit(i));
		}
	    if (type==0) {
	    	day_pv.setData(day);
			day_pv.setSelected(0);
		}else {	
			day_pv_to.setData(day);		
			day_pv_to.setSelected(0);		
			
		}
	
		executeScroll();
		
	}

	private void executeAnimator(View view) {
		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f);
		PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f);
		PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f);
		ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ).setDuration(200).start();
	}

	private void executeScroll() {
		year_pv.setCanScroll(year.size() > 1);
		year_pv_to.setCanScroll(year.size() > 1);
		month_pv.setCanScroll(month.size() > 1);
		month_pv_to.setCanScroll(month.size() > 1);
		day_pv.setCanScroll(day.size() > 1);
		day_pv_to.setCanScroll(day.size() > 1);
		hour_pv.setCanScroll(hour.size() > 1 && (scrollUnits & SCROLL_TYPE.HOUR.value) == SCROLL_TYPE.HOUR.value);
		hour_pv_to.setCanScroll(hour.size() > 1 && (scrollUnits & SCROLL_TYPE.HOUR.value) == SCROLL_TYPE.HOUR.value);
		minute_pv.setCanScroll(
				minute.size() > 1 && (scrollUnits & SCROLL_TYPE.MINUTE.value) == SCROLL_TYPE.MINUTE.value);
		minute_pv_to.setCanScroll(
				minute.size() > 1 && (scrollUnits & SCROLL_TYPE.MINUTE.value) == SCROLL_TYPE.MINUTE.value);
	}

	private int disScrollUnit(SCROLL_TYPE... scroll_types) {
		if (scroll_types == null || scroll_types.length == 0) {
			scrollUnits = SCROLL_TYPE.HOUR.value + SCROLL_TYPE.MINUTE.value;
		} else {
			for (SCROLL_TYPE scroll_type : scroll_types) {
				scrollUnits ^= scroll_type.value;
			}
		}
		return scrollUnits;
	}

	public void show(String time) {
			if (isValidDate(time, "yyyy-MM-dd")) {
				if (startCalendar.getTime().getTime() < endCalendar.getTime().getTime()) {
					initParameter(selectedCalenderFrom);
					initParameter(selectedCalenderTo);
					initTimer();
					addListenerFrom(selectedCalenderFrom);
					addListenerTo(selectedCalenderTo);
					setSelectedTime(time, selectedCalenderFrom);
					setSelectedTime(time, selectedCalenderTo);

				}
			}
	}

	/**
	 * 设置日期控件是否显示时和分
	 */
	public void showSpecificTime(boolean show) {
		// if (canAccess)
		{
			if (show) {
				disScrollUnit();
				hour_pv.setVisibility(View.VISIBLE);
				hour_text.setVisibility(View.VISIBLE);
				minute_pv.setVisibility(View.VISIBLE);
				minute_text.setVisibility(View.VISIBLE);
			} else {
				disScrollUnit(SCROLL_TYPE.HOUR, SCROLL_TYPE.MINUTE);
				hour_pv.setVisibility(View.GONE);
				hour_text.setVisibility(View.GONE);
				minute_pv.setVisibility(View.GONE);
				minute_text.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 设置日期控件默认选中的时间
	 */
	public void setSelectedTime(String time, Calendar selectedCalender) {
		// if (canAccess)
		{
			String[] str = time.split(" ");
			String[] dateStr = str[0].split("-");

			year_pv.setSelected(dateStr[0]);
			year_pv_to.setSelected(dateStr[0]);
			selectedCalender.set(Calendar.YEAR, Integer.parseInt(dateStr[0]));

			month.clear();
			int selectedYear = selectedCalender.get(Calendar.YEAR);
			if (selectedYear == startYear) {
				for (int i = startMonth; i <= MAX_MONTH; i++) {
					month.add(formatTimeUnit(i));
				}
			} else if (selectedYear == endYear) {
				for (int i = 1; i <= endMonth; i++) {
					month.add(formatTimeUnit(i));
				}
			} else {
				for (int i = 1; i <= MAX_MONTH; i++) {
					month.add(formatTimeUnit(i));
				}
			}
			month_pv.setData(month);
			month_pv_to.setData(month);
			month_pv.setSelected(dateStr[1]);
			month_pv_to.setSelected(dateStr[1]);
			selectedCalender.set(Calendar.MONTH, Integer.parseInt(dateStr[1]) - 1);
			executeAnimator(month_pv);
			executeAnimator(month_pv_to);

			day.clear();
			int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
			if (selectedYear == startYear && selectedMonth == startMonth) {
				for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
					day.add(formatTimeUnit(i));
				}
			} else if (selectedYear == endYear && selectedMonth == endMonth) {
				for (int i = 1; i <= endDay; i++) {
					day.add(formatTimeUnit(i));
				}
			} else {
				for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
					day.add(formatTimeUnit(i));
				}
			}
			day_pv.setData(day);
			day_pv_to.setData(day);
			day_pv.setSelected(dateStr[2]);
			day_pv_to.setSelected(dateStr[2]);
			selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr[2]));
			executeAnimator(day_pv);
			executeAnimator(day_pv_to);

			if (str.length == 2) {
				String[] timeStr = str[1].split(":");

				if ((scrollUnits & SCROLL_TYPE.HOUR.value) == SCROLL_TYPE.HOUR.value) {
					hour.clear();
					int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
					if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay) {
						for (int i = startHour; i <= MAX_HOUR; i++) {
							hour.add(formatTimeUnit(i));
						}
					} else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay) {
						for (int i = MIN_HOUR; i <= endHour; i++) {
							hour.add(formatTimeUnit(i));
						}
					} else {
						for (int i = MIN_HOUR; i <= MAX_HOUR; i++) {
							hour.add(formatTimeUnit(i));
						}
					}
					hour_pv.setData(hour);
					hour_pv_to.setData(hour);
					hour_pv.setSelected(timeStr[0]);
					hour_pv_to.setSelected(timeStr[0]);

					selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr[0]));
					executeAnimator(hour_pv);
					executeAnimator(hour_pv_to);
				}

				if ((scrollUnits & SCROLL_TYPE.MINUTE.value) == SCROLL_TYPE.MINUTE.value) {
					minute.clear();
					int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
					int selectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
					if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay
							&& selectedHour == startHour) {
						for (int i = startMinute; i <= MAX_MINUTE; i++) {
							minute.add(formatTimeUnit(i));
						}
					} else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay
							&& selectedHour == endHour) {
						for (int i = MIN_MINUTE; i <= endMinute; i++) {
							minute.add(formatTimeUnit(i));
						}
					} else {
						for (int i = MIN_MINUTE; i <= MAX_MINUTE; i++) {
							minute.add(formatTimeUnit(i));
						}
					}
					minute_pv.setData(minute);
					minute_pv_to.setData(minute);
					minute_pv.setSelected(timeStr[1]);
					minute_pv_to.setSelected(timeStr[1]);
					selectedCalender.set(Calendar.MINUTE, Integer.parseInt(timeStr[1]));
					executeAnimator(minute_pv);
					executeAnimator(minute_pv_to);
				}
			}
			executeScroll();
		}
	}

	/**
	 * 验证字符串是否是一个合法的日期格式
	 */
	private boolean isValidDate(String date, String template) {
		boolean convertSuccess = true;
		// 指定日期格式
		SimpleDateFormat format = new SimpleDateFormat(template);
		try {
			// 设置lenient为false.
			// 否则SimpleDateFormat会比较宽松地验证日期，比如2015/02/29会被接受，并转换成2015/03/01
			format.setLenient(false);
			format.parse(date);
		} catch (Exception e) {
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}
}
