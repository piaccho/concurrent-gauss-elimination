package org.example;

public class Relation {
    Action firstAction;
    Action secondAction;

    Relation(Action firstAction, Action secondAction) {
        if (firstAction.getType() > secondAction.getType()) {
            this.firstAction = secondAction;
            this.secondAction = firstAction;
        } else {
            this.firstAction = firstAction;
            this.secondAction = secondAction;
        }
    }

    public Action getFirstAction() {
        return firstAction;
    }

    public Action getSecondAction() {
        return secondAction;
    }

    @Override
    public String toString() {
        return "<" + firstAction + ", " + secondAction + ">";
    }
}
