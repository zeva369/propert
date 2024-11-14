package com.seva.propert.model;

import com.seva.propert.model.entity.Task;

public interface TaskEventListener {
    public void onDependencyRemoved(Task dependency);
    public void onPredecessorRemoved(Task predecessor);
}
