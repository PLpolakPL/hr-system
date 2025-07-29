package pl.atins.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
@Slf4j
public class HRCommandManager {

    private final Stack<HRCommand> commandHistory = new Stack<>();

    public void executeCommand(HRCommand command) {
        try {
            command.execute();
            commandHistory.push(command);
            log.info("Executed command: {}", command.getDescription());
        } catch (Exception e) {
            log.error("Failed to execute command: {}", command.getDescription(), e);
            throw e;
        }
    }

    public void undoLastCommand() {
        if (commandHistory.isEmpty()) {
            throw new IllegalStateException("No commands to undo");
        }

        var lastCommand = commandHistory.pop();
        if (!lastCommand.canUndo()) {
            throw new IllegalStateException("Last command cannot be undone");
        }

        try {
            lastCommand.undo();
            log.info("Undid command: {}", lastCommand.getDescription());
        } catch (Exception e) {
            commandHistory.push(lastCommand);
            log.error("Failed to undo command: {}", lastCommand.getDescription(), e);
            throw e;
        }
    }

    public boolean canUndo() {
        return !commandHistory.isEmpty() && commandHistory.peek().canUndo();
    }

    public int getHistorySize() {
        return commandHistory.size();
    }

    public void clearHistory() {
        commandHistory.clear();
        log.info("Command history cleared");
    }
}