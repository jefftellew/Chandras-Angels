package edu.ucsb.munchease;

import java.util.ArrayList;
import java.util.Calendar;
import com.google.gson.*;

public class RestaurantSchedule {
    private boolean isOpen;
    private ArrayList<DaySchedule> daySchedules;

    // Helper fields for synchronicity purposes in private member functions
    // Updated by helper function updateCurrentTime()
    private String currentTime;
    private int currentDay; // 1 - 7, Sunday = 1

    /**
     * Constructor that sets all fields to parameters
     * @param isOpen Boolean indicating if the restaurant is open right now
     * @param daySchedules ArrayList of DaySchedule objects
     */
    public RestaurantSchedule (boolean isOpen, ArrayList<DaySchedule> daySchedules) {
        this.isOpen = isOpen;
        this.daySchedules = daySchedules;
        // Update isOpen, currentTime, currentDay
        updateIsOpen();
    }

    /** isOpen accessor
     * @return Boolean indicating if the restaurant is open right now
     */
    public boolean getIsOpen() { return isOpen; }

    /** Schedule size accessor
     * @return The number of DaySchedule objects schedule
     */
    public int getScheduleSize() { return daySchedules.size(); }

    /**
     *
     * @param index The index of the DaySchedule to be accessed
     * @return The DaySchedule at the specified index
     * Use getScheduleSize() to ensure index is in bounds
     */
    public DaySchedule getDayScheduleAtIndex(int index) {
        return daySchedules.get(index);
    }

    /**
     * Updates the isOpen field based on current system time
     * Also calls updateCurrentTime()
     * @return the updated isOpen field
     */
    public boolean updateIsOpen() {

        // Get the index of most recent open shift pre-time update
        int latestOpenIndex = getIndexOfLatestDaySchedule();
        // Update date and time for
        updateCurrentTime();

        if(isOpen) {
            // Get close time of shift in which it was currently open
            String endTime = daySchedules.get(latestOpenIndex).getEndTime();
            isOpen = (currentTime.compareTo(endTime) < 0);

            return isOpen;
        }
        else {
            // Get next schedule of open time (with outdated time)
            int nextOpenIndex = latestOpenIndex + 1;
            if(nextOpenIndex == daySchedules.size())
                nextOpenIndex = 0;
            DaySchedule nextOpenSchedule = daySchedules.get(nextOpenIndex);

            if(currentDay == nextOpenSchedule.getDay())
                isOpen = (currentTime.compareTo(nextOpenSchedule.getStartTime()) >= 0);
            else
                isOpen = false;

            return isOpen;
        }
    }

    // Call this if it is currently closed
    // format: "HH:HH(,d)" where HH:HH is the 24 hour format and d is the integer value of the day IF it is not today
    // Uses Calendar day format: 1 = Sunday, 7 = Saturday
    // Returns null if it is currently open
    public String getNextOpeningTime() {
        if(isOpen)
            return null;

        // Get index of the first DaySchedule that has NOT occurred
        int index = getIndexOfLatestDaySchedule() + 1;
        if(index == daySchedules.size())
            index = 0;

        DaySchedule daySchedule = daySchedules.get(index);

        String returnStr = daySchedule.getStartTime();
        if(daySchedule.getDay() != currentDay)
            returnStr += String.format(",%d", currentDay);

        return returnStr;
    }

    // Call this if it is currently open
    // format: "HH:HH,d" where HH:HH is the 24 hour format and d is the integer value of the day
    // Uses Calendar day format: 1 = Sunday, 7 = Saturday
    // Returns null if it is currently closed
    public String getNextClosingTime() {
        if(!isOpen)
            return null;

        // Get index of current schedule
        int index = getIndexOfLatestDaySchedule();
        return daySchedules.get(index).getEndTime();
    }

    // Helper function: Returns the index of daySchedules of the current or most recent window
    // Assumes daily schedules on yelp are properly ordered
    // TODO find out if this is actually the case when yelp API works
    private int getIndexOfLatestDaySchedule() {
        // Iterate with index because it is needed
        int index = 0;
        while(index < daySchedules.size()) {
            // Used for shorthand purposes
            DaySchedule current = daySchedules.get(index);

            // Increment and skip other checks if current index is earlier in the week than today
            if(current.getDay() < currentDay) {
                index++;
                continue;
            }

            // Stop if current index has passed current day
            // Decrement index
            if(current.getDay() > currentDay) {
                index--;
                break;
            }

            // The schedule being looked at is today
            // If the shift has not started yet, return the previous index
            // Else, increment by 1
            if(currentTime.compareTo(current.getStartTime()) < 0) {
                index--;
                break;
            }
            index++;
        }

        // If the loop was immediately exited, or the end was reached,
        // then the schedule at the last index was the most recent shift
        if(index == -1 || index == daySchedules.size())
            index--;

        return index;
    }

    // Updates/initializes currentTime and currentDate
    private void updateCurrentTime() {
        Calendar c = Calendar.getInstance();
        currentDay = c.get(Calendar.DAY_OF_WEEK);

        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);

        // Format as "HHmm" string - identical format to Yelp and DaySchedule data
        currentTime = String.format("%02d%02d", currentHour, currentMinute);
    }
}
