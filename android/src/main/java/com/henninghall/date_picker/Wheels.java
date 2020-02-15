package com.henninghall.date_picker;

import com.henninghall.date_picker.models.Mode;
import com.henninghall.date_picker.wheelFunctions.AddOnChangeListener;
import com.henninghall.date_picker.wheelFunctions.WheelFunction;
import com.henninghall.date_picker.wheels.AmPmWheel;
import com.henninghall.date_picker.wheels.DateWheel;
import com.henninghall.date_picker.wheels.DayWheel;
import com.henninghall.date_picker.wheels.HourWheel;
import com.henninghall.date_picker.wheels.MinutesWheel;
import com.henninghall.date_picker.wheels.MonthWheel;
import com.henninghall.date_picker.wheels.Wheel;
import com.henninghall.date_picker.wheels.YearWheel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

class Wheels {

    private final State state;
    private WheelOrder wheelOrder;
    private WheelChangeListener onWheelChangeListener;
    HourWheel hourWheel;
    DayWheel dayWheel;
    MinutesWheel minutesWheel;
    AmPmWheel ampmWheel;
    DateWheel dateWheel;
    MonthWheel monthWheel;
    YearWheel yearWheel;


    EmptyWheelUpdater emptyWheelUpdater;

    Wheels(State state){
        this.state = state;
        yearWheel = new YearWheel(getPickerWithId(R.id.year), state);
        monthWheel = new MonthWheel(getPickerWithId(R.id.month), state);
        dateWheel = new DateWheel(getPickerWithId(R.id.date), state);
        dayWheel = new DayWheel(getPickerWithId(R.id.day), state);
        minutesWheel = new MinutesWheel(getPickerWithId(R.id.minutes), state);
        ampmWheel = new AmPmWheel(getPickerWithId(R.id.ampm), state);
        hourWheel = new HourWheel(getPickerWithId(R.id.hour), state);

        wheelOrder = new WheelOrder(this, pickerView);
        onWheelChangeListener = new WheelChangeListenerImpl(pickerView);
        emptyWheelUpdater = new EmptyWheelUpdater(pickerView);
        changeAmPmWhenPassingMidnightOrNoon();

        applyOnAllWheels(new AddOnChangeListener(onWheelChangeListener));
    }

    private NumberPickerView getPickerWithId(int id){
        return (NumberPickerView) pickerView.findViewById(id);
    }

    public Collection<Wheel> getVisibleWheels() {
        Collection<Wheel> visibleWheels = new ArrayList<>();
        for (Wheel wheel: getAll()) if (wheel.visible()) visibleWheels.add(wheel);
        return visibleWheels;
    }

    public void applyOnAllWheels(WheelFunction function) {
        for (Wheel wheel: getAll()) function.apply(wheel);
    }

    public void applyOnVisibleWheels(WheelFunction function) {
        for (Wheel wheel: getVisibleWheels()) function.apply(wheel);
    }


    private void changeAmPmWhenPassingMidnightOrNoon(){
        hourWheel.picker.setOnValueChangeListenerInScrolling(new NumberPickerView.OnValueChangeListenerInScrolling() {
            @Override
            public void onValueChangeInScrolling(NumberPickerView picker, int oldVal, int newVal) {
                if(Settings.usesAmPm()){
                    String oldValue = hourWheel.getValueAtIndex(oldVal);
                    String newValue = hourWheel.getValueAtIndex(newVal);
                    boolean passingNoonOrMidnight = (oldValue.equals("12") && newValue.equals("11")) || oldValue.equals("11") && newValue.equals("12");
                    if (passingNoonOrMidnight) ampmWheel.picker.smoothScrollToValue((ampmWheel.picker.getValue() + 1) % 2,false);
                }
            }
        });
    }

    public List<Wheel> getAll(){
        return new ArrayList<>(Arrays.asList(yearWheel, monthWheel, dateWheel, dayWheel, hourWheel, minutesWheel, ampmWheel));
    }

    public void updateWheelOrder(Locale locale){
        wheelOrder.update(locale);
    }

    Wheel getVisibleWheels(int index){
        return wheelOrder.getVisibleWheels().get(index);
    }

    private String getDateFormatPattern(){
        if(state.getMode() == Mode.date){
            return wheelOrder.getVisibleWheel(0).getFormatPattern() + " "
                    + wheelOrder.getVisibleWheel(1).getFormatPattern() + " "
                    + wheelOrder.getVisibleWheel(2).getFormatPattern();
        }
        return dayWheel.getFormatPattern();
    }

    public String getFormatPattern() {
        return this.getDateFormatPattern() + " "
                + hourWheel.getFormatPattern() + " "
                + minutesWheel.getFormatPattern()
                + ampmWheel.getFormatPattern();
    }


    String getDateString() {
        String dateString = (state.getMode() == Mode.date)
                ? wheelOrder.getVisibleWheel(0).getValue() + " "
                + wheelOrder.getVisibleWheel(1).getValue() + " "
                + wheelOrder.getVisibleWheel(2).getValue()
                : dayWheel.getValue();
        return dateString
                + " " + hourWheel.getValue()
                + " " + minutesWheel.getValue()
                + ampmWheel.getValue();
    }

}
