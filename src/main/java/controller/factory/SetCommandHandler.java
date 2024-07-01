package controller.factory;

import dto.ActionDTO;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import service.Store;

public class SetCommandHandler implements CommandHandler {
    @Override
    public RespDTO handle(ActionDTO dto, Store store) {
        store.set(dto.getKey(), dto.getValue());
        return new RespDTO(RespStatusTypeEnum.SUCCESS, null);
    }
}
