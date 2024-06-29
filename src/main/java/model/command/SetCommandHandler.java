package model.command;

import dto.ActionDTO;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import service.Store;

import java.io.IOException;
import java.io.ObjectOutputStream;

public abstract class SetCommandHandler implements Command {
    private final Store store;

    public SetCommandHandler(Store store) {
        this.store = store;
    }

    @Override
    public void execute(ActionDTO dto, ObjectOutputStream oos) throws IOException {
        store.set(dto.getKey(), dto.getValue());
        RespDTO resp = new RespDTO(RespStatusTypeEnum.SUCCESS, null);
        oos.writeObject(resp);
        oos.flush();
    }
}
