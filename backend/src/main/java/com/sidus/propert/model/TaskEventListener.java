package com.sidus.propert.model;

import com.sidus.propert.model.entity.Task;

public interface TaskEventListener {
    public void onDependencyRemoved(Task dependency);
    public void onPredecessorRemoved(Task predecessor);
}
