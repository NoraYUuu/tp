package seedu.address.model.tuition;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import seedu.address.ui.ResultDisplay;
import seedu.address.ui.infopage.TimetableInfoPage;


public class Timetable {
    public static final String COLOR_ODD = "-fx-background-color: #3e7589; -fx-text-fill:WHITE; -fx-font-size:%1$spt;";
    public static final String COLOR_EVEN = "-fx-background-color: #515658; -fx-text-fill:WHITE;"
            + " -fx-font-size:%1$spt;";
    private static final int DEFAULT_FONT_SIZE = 8;
    private static final String NOT_SHOWN = "The following class details are not shown due to space limit: ";
    private static final HashMap<String, Integer> dates = Timeslot.getDays();
    private final ObservableList<TuitionClass> tuitionClasses;
    private int start;
    private int end;
    private int totalRows;
    private ArrayList<LocalTime> timeStart = new ArrayList<>();
    private ArrayList<LocalTime> timeEnd = new ArrayList<>();
    private ArrayList<String> notShown = new ArrayList<>();
    private TimetableInfoPage infoPage;
    private ResultDisplay resultDisplay;

    /**
     * Constructor of timetable.
     * @param tuitionClasses tuition classes to be shown on the timetable.
     * @param resultDisplay display tuition classes not shown, if any.
     * @param timetableInfoPage the UI component responsible for showing the timetable.
     */
    public Timetable(ObservableList<TuitionClass> tuitionClasses, ResultDisplay resultDisplay,
                     TimetableInfoPage timetableInfoPage) {
        this.tuitionClasses = tuitionClasses;
        this.infoPage = timetableInfoPage;
        this.resultDisplay = resultDisplay;
    }

    /**
     * Displays the timetable constructed from the tuition classes.
     */
    public void showTimetable() {
        parseTime(this.tuitionClasses);
        infoPage.setTableTime(start, end, totalRows);
        insertSlot();
        if (notShown.size() > 0) {
            String notShownClasses = "";
            for (String s: notShown) {
                notShownClasses += "\n" + s;
            }
            resultDisplay.setFeedbackToUser(NOT_SHOWN + notShownClasses);
        }
    }

    /**
     * Parses timeslot of tuition class list to determine the size of timetable.
     */
    public void parseTime(ObservableList<TuitionClass> tuitionClassesPresent) {
        LocalTime earliest = null;
        LocalTime latest = null;
        for (TuitionClass tc: tuitionClassesPresent) {
            LocalTime[] times = tc.getTimeslot().parseTime();
            LocalTime localStart = times[0];
            LocalTime localEnd = times[1];
            timeStart.add(localStart);
            timeEnd.add(localEnd);
            if (earliest == null) {
                earliest = localStart;
            }
            if (latest == null) {
                latest = localEnd;
            }
            if (localStart.compareTo(earliest) < 0) {
                earliest = localStart;
            }

            if (localEnd.compareTo(latest) > 0) {
                latest = localEnd;
            }
        }
        this.start = Integer.parseInt(earliest.toString().substring(0, 2));
        int end = Integer.parseInt(latest.toString().substring(0, 2));
        this.end = end + 1;
        this.totalRows = (this.end - this.start + 1) * 6;
    }

    /**
     * Inserts tuition classes into the timetable.
     */
    public void insertSlot() {
        if (tuitionClasses.size() == 0) {
            return;
        }
        LocalTime startTime;
        if ((this.start + "").length() == 1) {
            startTime = LocalTime.parse("0" + this.start + ":00");
        } else {
            startTime = LocalTime.parse(this.start + ":00");
        }
        for (int i = 0; i < tuitionClasses.size(); i++) {
            insertATuitionClass(tuitionClasses.get(i), startTime, i);
        }
    }

    /**
     * Inserts a tuition class into timetable.
     * @param tuitionClass the tuition class to be inserted.
     * @param startTime the starting time of the timetable.
     * @param i the position of the tuition class in the tuition class list
     */
    private void insertATuitionClass(TuitionClass tuitionClass, LocalTime startTime, int i) {
        String date = tuitionClass.getTimeslot().getDayString();
        int colInsert = dates.get(date);
        int rowStartInsert = 6 + (int) Math.floor(startTime.until(timeStart.get(i), ChronoUnit.MINUTES) / (float) 10.0);
        int rowFinishInsert;
        String message = tuitionClass.getNameString() + "\n" + tuitionClass.getTimeslot().getTime().substring(4);
        rowFinishInsert = 6 + Math.round(startTime.until(timeEnd.get(i), ChronoUnit.MINUTES) / (float) 10.0);
        int rowSpan = rowFinishInsert - rowStartInsert;
        Label lesson = getLabel(message, getFontSize(rowSpan),
                colInsert, tuitionClass);
        if (infoPage != null) {
            infoPage.addLesson(lesson, colInsert, rowStartInsert, 1,
                    rowSpan == 0 ? 1 : rowSpan);
            GridPane.setHalignment(lesson, HPos.CENTER);
            GridPane.setFillWidth(lesson, true);
        }
    }

    public String getColor(int start) {
        String color = start % 2 == 0 ? COLOR_EVEN : COLOR_ODD;
        return color;
    }

    /**
     * Produces a label of tuition class to be inserted into the timetable.
     * @param message message shown on the label.
     * @param fontSize size of text of label.
     * @param col column of timetable the label should be shown at.
     * @param tuitionClass the tuition class to be shown.
     * @return a label with information of tuition class on it.
     */
    public Label getLabel(String message, int fontSize, int col, TuitionClass tuitionClass) {
        Label lesson;
        if (fontSize == 1) {
            lesson = productLabel("");
            notShown.add(tuitionClass.getNameString() + " " + tuitionClass.getTimeslot());
        } else if (fontSize == 2) {
            lesson = productLabel(tuitionClass.getNameString());
            fontSize = 8;
            notShown.add(tuitionClass.getNameString() + " " + tuitionClass.getTimeslot());
        } else {
            lesson = productLabel(message);
        }
        if (lesson != null) {
            lesson.setStyle(String.format(getColor(col), fontSize));
            lesson.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            lesson.setAlignment(Pos.CENTER);
        }
        return lesson;
    }

    /**
     * Produces a Label with given message.
     * @param message message to shown on the label.
     * @return the label instance.
     */
    public Label productLabel(String message) {
        return new Label(message);
    }

    /**
     * Determines font size of each tuition class according to its time span.
     * @param span time range of the tuition class.
     * @return the font size to be used.
     */
    public int getFontSize(int span) {
        if (span < 3) {
            return 1;
        }
        if (span <= 4) {
            return 2;
        }
        if (span < 6) {
            return 6;
        }
        if (span < 9) {
            return 7;
        }
        return DEFAULT_FONT_SIZE;
    }

    /**
     * Gets starting hour.
     * @return the starting hour.
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets ending hour.
     * @return the ending hour.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gets total rows of the timetable.
     * @return the number of rows.
     */
    public int getTotalRows() {
        return totalRows;
    }

    /**
     * Returns a copy of the notshown arraylist.
     * @return an arraylist with same elements as notshown.
     */
    public ArrayList<String> getNotShown() {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(notShown);
        return result;
    }
}
