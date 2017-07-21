package com.sargent.mark.todolist.data;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoItem {
    private String description;
    private String dueDate;
    private String SpinnerValue;

    public ToDoItem(String description, String dueDate, String SpinnerValue) {
        this.description = description;
        this.dueDate = dueDate;
        this.SpinnerValue = SpinnerValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getSpinnerValue() {
        return SpinnerValue;
    }

    public void setSpinnerValue(String SpinnerValue) {
        this.SpinnerValue = SpinnerValue;
    }

}
