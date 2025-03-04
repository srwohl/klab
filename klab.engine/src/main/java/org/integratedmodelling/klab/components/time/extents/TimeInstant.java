package org.integratedmodelling.klab.components.time.extents;

import org.integratedmodelling.klab.api.observations.scale.time.ITime;
import org.integratedmodelling.klab.api.observations.scale.time.ITime.Resolution;
import org.integratedmodelling.klab.api.observations.scale.time.ITimeInstant;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;

/**
 * TODO switch to Java8 dates
 * 
 * @author Ferd
 *
 */
public class TimeInstant implements ITimeInstant {
	
	DateTime time;

	public static ITimeInstant create(int year, int month, int day) {
		return new TimeInstant(year, month, day);
	}

	public static ITimeInstant create(int year) {
		return new TimeInstant(year);
	}

	public static ITimeInstant create(DateTime time) {
		return new TimeInstant(time);
	}

	public static ITimeInstant create(long milliseconds) {
		return new TimeInstant(milliseconds);
	}

	public static ITimeInstant create() {
		return new TimeInstant();
	}
	
	public TimeInstant(int year) {
		time = new DateTime(year, 1, 1, 0, 0, DateTimeZone.UTC);
	}
	
	public TimeInstant(int year, int month, int day) {
		time = new DateTime(year, month, day, 0, 0, DateTimeZone.UTC);
	}

	public TimeInstant(long milliseconds) {
		time = new DateTime(milliseconds, DateTimeZone.UTC);
	}

	public TimeInstant(DateTime time) {
		this.time = time;
	}
	
	public TimeInstant() {
		this.time = DateTime.now(DateTimeZone.UTC);
	}
	
	@Override
	public int compareTo(ITimeInstant arg0) {
		return time.compareTo(((TimeInstant)arg0).time);
	}

	@Override
	public long getMilliseconds() {
		return time.getMillis();
	}

	public DateTime asDate() {
		return time;
	}
	
	public String toString() {
		return DateTimeFormat.shortDateTime().print(time);
	}

	@Override
	public boolean isAfter(ITimeInstant t) {
		return this.time.isAfter(((TimeInstant)t).time);
	}

	@Override
	public int getDayOfYear() {
		return this.time.getDayOfYear() - 1;
	}
	
	@Override
	public boolean isBefore(ITimeInstant t) {
		return this.time.isBefore(((TimeInstant)t).time);
	}

	@Override
	public String getSpecification() {
		// TODO Auto-generated method stub
		return "todo";
	}

    @Override
    public int getYear() {
        return this.time.getYear();
    }

	@Override
    public ITimeInstant plus(int periods, ITime.Resolution resolution) {
    	switch (resolution.getType()) {
		case CENTURY:
			return new TimeInstant(time.plusYears((int)(resolution.getMultiplier() * 100 * periods)));
		case DAY:
			return new TimeInstant(time.plusDays((int)(resolution.getMultiplier() * periods)));
		case DECADE:
			return new TimeInstant(time.plusYears((int)(resolution.getMultiplier() * 10 * periods)));
		case HOUR:
			return new TimeInstant(time.plusHours((int)resolution.getMultiplier() * periods));
		case MILLENNIUM:
			return new TimeInstant(time.plusYears((int)(resolution.getMultiplier() * 1000 * periods)));
		case MILLISECOND:
			return new TimeInstant(time.plusMillis((int)resolution.getMultiplier() * periods));
		case MINUTE:
			return new TimeInstant(time.plusMinutes((int)resolution.getMultiplier() * periods));
		case MONTH:
			return new TimeInstant(time.plusMonths((int)resolution.getMultiplier() * periods));
		case SECOND:
			return new TimeInstant(time.plusSeconds((int)resolution.getMultiplier() * periods));
		case WEEK:
			return new TimeInstant(time.plusWeeks((int)resolution.getMultiplier() * periods));
		case YEAR:
			return new TimeInstant(time.plusYears((int)resolution.getMultiplier() * periods));
    	}
    	throw new KlabValidationException("wrong resolution passed to ITimeInstant::plus");
    }

	@Override
    public ITimeInstant minus(int periods, ITime.Resolution resolution) {
    	switch (resolution.getType()) {
		case CENTURY:
			return new TimeInstant(time.minusYears((int)(resolution.getMultiplier() * 100 * periods)));
		case DAY:
			return new TimeInstant(time.minusDays((int)(resolution.getMultiplier() * periods)));
		case DECADE:
			return new TimeInstant(time.minusYears((int)(resolution.getMultiplier() * 10 * periods)));
		case HOUR:
			return new TimeInstant(time.minusHours((int)resolution.getMultiplier() * periods));
		case MILLENNIUM:
			return new TimeInstant(time.minusYears((int)(resolution.getMultiplier() * 1000 * periods)));
		case MILLISECOND:
			return new TimeInstant(time.minusMillis((int)resolution.getMultiplier() * periods));
		case MINUTE:
			return new TimeInstant(time.minusMinutes((int)resolution.getMultiplier() * periods));
		case MONTH:
			return new TimeInstant(time.minusMonths((int)resolution.getMultiplier() * periods));
		case SECOND:
			return new TimeInstant(time.minusSeconds((int)resolution.getMultiplier() * periods));
		case WEEK:
			return new TimeInstant(time.minusWeeks((int)resolution.getMultiplier() * periods));
		case YEAR:
			return new TimeInstant(time.minusYears((int)resolution.getMultiplier() * periods));
    	}
    	throw new KlabValidationException("wrong resolution passed to ITimeInstant::plus");
    }
    
    @Override
    public long getPeriods(ITimeInstant other, ITime.Resolution resolution) {
    	
    	DateTime start = this.time;
    	DateTime end = ((TimeInstant)other).time;
    	
    	if (start.isAfter(end)) {
    		start = end;
    		end = this.time;
    	}
    	
    	switch (resolution.getType()) {
		case CENTURY:
			return Years.yearsBetween(start, end).getYears()/(int)(100 * resolution.getMultiplier());
		case DAY:
			return Days.daysBetween(start, end).getDays()/(int)resolution.getMultiplier();
		case DECADE:
			return Years.yearsBetween(start, end).getYears()/(int)(10 * resolution.getMultiplier());
		case HOUR:
			return Hours.hoursBetween(start, end).getHours()/(int)resolution.getMultiplier();
		case MILLENNIUM:
			return Years.yearsBetween(start, end).getYears()/(int)(1000 * resolution.getMultiplier());
		case MILLISECOND:
			return end.getMillis() - start.getMillis()/(int)resolution.getMultiplier();
		case MINUTE:
			return Minutes.minutesBetween(start, end).getMinutes()/(int)resolution.getMultiplier();
		case MONTH:
			return Months.monthsBetween(start, end).getMonths()/(int)resolution.getMultiplier();
		case SECOND:
			return Seconds.secondsBetween(start, end).getSeconds()/(int)resolution.getMultiplier();
		case WEEK:
			return Weeks.weeksBetween(start, end).getWeeks()/(int)resolution.getMultiplier();
		case YEAR:
			return Years.yearsBetween(start, end).getYears()/(int)resolution.getMultiplier();
    	}
    	
    	throw new KlabValidationException("wrong resolution passed to ITimeInstant::getPeriods");
    }

	@Override
	public boolean isAlignedWith(Resolution res) {
		
    	switch (res.getType()) {
		case CENTURY:
			return time.getYear() % 100 == 0 && time.getSecondOfDay() == 0 && time.getDayOfYear() == 1;
		case DAY:
			return time.getMinuteOfDay() == 0;
		case DECADE:
			return time.getYear() % 10 == 0 && time.getSecondOfDay() == 0 && time.getDayOfYear() == 1;
		case HOUR:
			return time.getMinuteOfHour() == 0;
		case MILLENNIUM:
			return time.getYear() % 1000 == 0 && time.getSecondOfDay() == 0 && time.getDayOfYear() == 1;
		case MILLISECOND:
			return true;
		case MINUTE:
			return time.getSecondOfMinute() == 0;
		case MONTH:
			return time.getDayOfMonth() == 1 && time.getSecondOfDay() == 0;
		case SECOND:
			return time.getMillis() % 1000 == 0;
		case WEEK:
			return time.getDayOfWeek() == 1 && time.getSecondOfDay() == 0;
		case YEAR:
			return time.getSecondOfDay() == 0 && time.getDayOfYear() == 1;
    	}
    	
		return false;
	}

	@Override
	public int getDay() {
		return time.getDayOfMonth();
	}

	@Override
	public int getMonth() {
		return time.getMonthOfYear();
	}

	@Override
	public int getHour() {
		return time.getHourOfDay();
	}

	@Override
	public int getMinute() {
		return time.getMinuteOfHour();
	}

}
