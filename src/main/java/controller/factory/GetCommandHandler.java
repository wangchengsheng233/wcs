package controller.factory;

import dto.ActionDTO;
import dto.RespDTO;
import dto.RespStatusTypeEnum;
import service.Store;

public class GetCommandHandler implements CommandHandler {
    @Override
    public RespDTO handle(ActionDTO dto, Store store) {
        String value = store.get(dto.getKey());
        return new RespDTO(RespStatusTypeEnum.SUCCESS, value);
    }
}