package model.command;

import dto.ActionDTO;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import service.Store;

import java.io.IOException;
import java.io.ObjectOutputStream;

public abstract class GetCommandHandler implements Command {
    private final Store store;

    public GetCommandHandler(Store store) {
        this.store = store;
    }

    @Override
    public void execute(ActionDTO dto, ObjectOutputStream oos) throws IOException {
        String value = store.get(dto.getKey());
        RespDTO resp = new RespDTO(RespStatusTypeEnum.SUCCESS, value);
        oos.writeObject(resp);
        oos.flush();
    }
}
