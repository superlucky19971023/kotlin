package com.ocr.myapplication.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ocr.myapplication.database.Note

class NoteViewModel : ViewModel() {

    // Event to notify when a note is added
    private val _noteAddedEvent = MutableLiveData<Event<Boolean>>()
    val noteAddedEvent: LiveData<Event<Boolean>> = _noteAddedEvent

    private val _noteOpenEditPanelEvent = MutableLiveData<Event<Boolean>>()
    val noteOpenEditPanelEvent: LiveData<Event<Boolean>> = _noteOpenEditPanelEvent
    // Event for note clicks with the Note object
    private val _noteClickedEvent = MutableLiveData<Event<Note>>()
    val noteClickedEvent: LiveData<Event<Note>> = _noteClickedEvent

    // Event to notify when a note is deleted
    private val _noteDeletedEvent = MutableLiveData<Event<Boolean>>()
    val noteDeletedEvent: LiveData<Event<Boolean>> = _noteDeletedEvent

    // Trigger the note added event
    fun noteAdded() {
        _noteAddedEvent.value = Event(true)
    }

    fun noteOpenEditPanel(){
        _noteOpenEditPanelEvent.value = Event(true)
    }

    // Trigger note clicked event with the Note object
    fun onNoteClicked(note: Note) {
        _noteClickedEvent.value = Event(note)
    }

    // Trigger the note deleted event
    fun noteDeleted() {
        _noteDeletedEvent.value = Event(true)
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