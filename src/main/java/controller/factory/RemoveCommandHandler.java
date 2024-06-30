package controller.factory;

import dto.ActionDTO;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import service.Store;

public class RemoveCommandHandler implements CommandHandler {
    @Override
    public RespDTO handle(ActionDTO dto, Store store) {
        store.rm(dto.getKey());
        return new RespDTO(RespStatusTypeEnum.SUCCESS, null);
    }
}