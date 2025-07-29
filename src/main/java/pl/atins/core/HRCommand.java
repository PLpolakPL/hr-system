package pl.atins.core;

public interface HRCommand {

    void execute();

    void undo();

    String getDescription();

    boolean canUndo();
}