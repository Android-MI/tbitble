package com.tbit.tbitblesdk.Bike.services.command;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Salmon on 2017/4/18 0018.
 */

public class CommandDispatcher implements CommandHolder {
    private Command currentCommand;
    private List<Command> commandList;

    public CommandDispatcher() {
        this.commandList = new LinkedList<>();
    }

    public void addCommand(Command command) {
        this.commandList.add(command);
        notifyCommandAdded();
    }

    private void notifyCommandAdded() {
        if (currentCommand != null && currentCommand.getState() != Command.FINISHED)
            return;
        if (commandList.size() == 0)
            return;
        Command nextCommand = commandList.remove(0);
        executeCommand(nextCommand);
    }

    private void executeCommand(Command command) {
        currentCommand = command;
        command.process(this);
    }

    public Command getCurrentCommand() {
        return currentCommand;
    }

    @Override
    public void onCommandCompleted() {
        notifyCommandAdded();
    }

    public void destroy() {
        if (currentCommand != null) {
            currentCommand.cancel();
            currentCommand = null;
        }

        for (Command command : commandList) {
            command.cancel();
        }

        commandList.clear();
    }
}
