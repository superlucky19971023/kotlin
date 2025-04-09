package com.ocr.myapplication.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ocr.myapplication.database.Reminder

class ReminderViewModel : ViewModel() {

    // Event to notify when a reminder is added
    private val _reminderAddedEvent = MutableLiveData<Event<Boolean>>()
    val reminderAddedEvent: LiveData<Event<Boolean>> = _reminderAddedEvent

    private val _reminderOpenEditPanelEvent = MutableLiveData<Event<Boolean>>()
    val reminderOpenEditPanelEvent: LiveData<Event<Boolean>> = _reminderOpenEditPanelEvent
    // Event for reminder clicks with the reminder object
    private val _reminderClickedEvent = MutableLiveData<Event<Reminder>>()
    val reminderClickedEvent: LiveData<Event<Reminder>> = _reminderClickedEvent

    // Event to notify when a reminder is deleted
    private val _reminderDeletedEvent = MutableLiveData<Event<Boolean>>()
    val reminderDeletedEvent: LiveData<Event<Boolean>> = _reminderDeletedEvent

    // Trigger the reminder added event
    fun reminderAdded() {
        _reminderAddedEvent.value = Event(true)
    }

    fun reminderOpenEditPanel(){
        _reminderOpenEditPanelEvent.value = Event(true)
    }

    // Trigger reminder clicked event with the reminder object
    fun onReminderClicked(reminder: Reminder) {
        _reminderClickedEvent.value = Event(reminder)
    }

    // Trigger the reminder deleted event
    fun reminderDeleted() {
        _reminderDeletedEvent.value = Event(true)
    }

    // Event wrapper class for one-time events
    class Event<T>(private val content: T) {
        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }
}