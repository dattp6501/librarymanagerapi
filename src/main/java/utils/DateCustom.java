package utils;

public class DateCustom {
    public static int getDateNumberOfMonth(int month, int year) {
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            return 31;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        }
        if (month == 2) {
            if (checkYear(year)) {
                return 29;
            }
            return 28;
        }
        return -1;
    }

    public static boolean checkYear(int year) {// nhuan: true
        return (year%4==0 && year%100!=0) || year%400==0;
    }
}
