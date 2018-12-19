package ng.com.teddinsight.teddinsight_app.utils;

public class TimeUtils implements Comparable, java.io.Serializable, Cloneable {

    private long time = 0;

    /**
     * Constant for milliseconds unit and conversion
     */
    public static final int MILLISECONDS = 1;
    /**
     * Constant for seconds unit and conversion
     */
    public static final int SECONDS = MILLISECONDS * 1000;
    /**
     * Constant for minutes unit and conversion
     */
    public static final int MINUTES = SECONDS * 60;
    /**
     * Constant for hours unit and conversion
     */
    public static final int HOURS = MINUTES * 60;
    /**
     * Constant for days unit and conversion
     */
    public static final int DAYS = HOURS * 24;
    /**
     * Represents the Maximum TimeUtils value
     */
    public static final TimeUtils MAX_VALUE = new TimeUtils(Long.MAX_VALUE);
    /**
     * Represents the Minimum TimeUtils value
     */
    public static final TimeUtils MIN_VALUE = new TimeUtils(Long.MIN_VALUE);
    /**
     * Represents the TimeUtils with a value of zero
     */
    public static final TimeUtils ZERO = new TimeUtils(0L);


    public TimeUtils(long time) {
        this.time = time;
    }

    /**
     * Creates a new TimeUtils object based on the unit and value entered.
     *
     * @param units the type of unit to use to create a TimeUtils instance.
     * @param value the number of units to use to create a TimeUtils instance.
     */
    public TimeUtils(int units, long value) {
        this.time = this.toMilliseconds(units, value);
    }

    /**
     * Subtracts two Date objects creating a new TimeUtils object.
     *
     * @param date1 Date to use as the base value.
     * @param date2 Date to subtract from the base value.
     * @return a TimeUtils object representing the difference bewteen the
     * two Date objects.
     */
    public static TimeUtils subtract(java.util.Date date1, java.util.Date date2) {
        return new TimeUtils(date1.getTime() - date2.getTime());
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Comparison is
     * based on the number of milliseconds in this TimeUtils.
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo(Object o) {
        TimeUtils compare = (TimeUtils) o;
        if (this.time == compare.time) {
            return 0;
        }
        if (this.time > compare.time) {
            return +1;
        }
        return -1;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Comparison is based on the number of milliseconds in this TimeUtils.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if the obj argument is a TimeUtils object
     * with the exact same number of milliseconds.
     * <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof TimeUtils) {
            TimeUtils compare = (TimeUtils) obj;
            if (this.time == compare.time) {
                return true;
            }
        }
        return false;
    }

    private static long toMilliseconds(int units, long value) {
        long millis;
        switch (units) {
            case TimeUtils.MILLISECONDS:
            case TimeUtils.SECONDS:
            case TimeUtils.MINUTES:
            case TimeUtils.HOURS:
            case TimeUtils.DAYS:
                millis = value * units;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized units: " + units);
        }
        return millis;
    }

    public long getDays() {
        return (((this.time / 1000) / 60) / 60) / 24;
    }

    /**
     * Gets the number of days including fractional days.
     *
     * @return the number of days.
     */
    public double getTotalDays() {
        return (((this.time / 1000.0d) / 60.0d) / 60.0d) / 24.0d;
    }
}